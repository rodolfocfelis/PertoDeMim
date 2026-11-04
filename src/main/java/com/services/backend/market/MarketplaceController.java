package com.services.backend.market;

import com.services.backend.entities.*;
import com.services.backend.entities.enums.UserRole;
import com.services.backend.repositories.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/market")
@Transactional(readOnly = true)
public class MarketplaceController {

  private final UserRepository users;
  private final ProfessionalRepository services;
  private final ServiceRequestRepository requests;
  private final ServiceReviewRepository reviews;
  private final ServiceFavoriteRepository favorites;
  private final CategoryRepository categories;
  private final RequestWorkflow workflow;

  public MarketplaceController(
    UserRepository users,
    ProfessionalRepository services,
    ServiceRequestRepository requests,
    ServiceReviewRepository reviews,
    ServiceFavoriteRepository favorites,
    CategoryRepository categories,
    RequestWorkflow workflow
  ) {
    this.users = users;
    this.services = services;
    this.requests = requests;
    this.reviews = reviews;
    this.favorites = favorites;
    this.categories = categories;
    this.workflow = workflow;
  }

  public record NewRequest(
    Long serviceId,
    String description,
    LocalDate preferredDate,
    String requestKey
  ) {}

  public record StatusChange(RequestStatus status) {}

  public record NewReview(int rating, String comment) {}

  public record RequestView(
    Long id,
    Long serviceId,
    String serviceName,
    String customerName,
    String customerPhone,
    String professionalPhone,
    String description,
    LocalDate preferredDate,
    RequestStatus status,
    Instant createdAt,
    Instant updatedAt,
    Integer reviewRating
  ) {}

  public record ReviewView(
    Long id,
    Long serviceId,
    String serviceName,
    String customerName,
    int rating,
    String comment,
    Instant createdAt,
    boolean visible
  ) {}

  public record Summary(Long serviceId, double average, long count) {}

  public record FavoriteView(
    Long id,
    String name,
    String description,
    BigDecimal startingPrice,
    boolean acceptingRequests,
    String category,
    String city,
    String state,
    String phone
  ) {}

  private User user(Authentication authentication) {
    return users.findByEmail(authentication.getName());
  }

  private User require(Authentication authentication, UserRole role) {
    User user = user(authentication);
    if (
      user == null || user.getRole() != role
    ) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    return user;
  }

  private Professional activeService(Long id) {
    return services
      .findById(id)
      .filter(Professional::isActive)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  private Pageable page(int page, int size) {
    return PageRequest.of(
      Math.max(0, page),
      Math.max(1, Math.min(50, size)),
      Sort.by(Sort.Direction.DESC, "createdAt", "id")
    );
  }

  private RequestView view(ServiceRequest request) {
    return new RequestView(
      request.id,
      request.professional.getId(),
      request.professional.getName(),
      request.customer.getName(),
      request.customer.getPhone(),
      request.professional.getPhone(),
      request.description,
      request.preferredDate,
      request.status,
      request.createdAt,
      request.updatedAt,
      request.review == null ? null : request.review.getRating()
    );
  }

  private ReviewView view(ServiceReview review) {
    return new ReviewView(
      review.id,
      review.request.getProfessional().getId(),
      review.request.getProfessional().getName(),
      review.request.getCustomer().getName(),
      review.rating,
      review.comment,
      review.createdAt,
      review.visible
    );
  }

  @GetMapping("/requests")
  public Page<RequestView> list(
    Authentication authentication,
    @RequestParam(required = false) RequestStatus status,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    User user = user(authentication);
    List<RequestStatus> statuses =
      status == null ? List.of(RequestStatus.values()) : List.of(status);
    if (user.getRole() == UserRole.USER) return requests
      .findByCustomerIdAndStatusIn(user.getId(), statuses, page(page, size))
      .map(this::view);
    if (user.getRole() == UserRole.PROFESSIONAL) return requests
      .findByProfessionalUserEmailAndStatusIn(
        user.getEmail(),
        statuses,
        page(page, size)
      )
      .map(this::view);
    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  }

  @PostMapping("/requests")
  @Transactional
  public RequestView create(
    Authentication authentication,
    @RequestBody NewRequest input
  ) {
    User customer = require(authentication, UserRole.USER);
    if (
      input.serviceId() == null ||
      input.description() == null ||
      input.description().trim().length() < 10 ||
      input.description().length() > 2000 ||
      input.requestKey() == null
    ) throw new IllegalArgumentException(
      "Descreva sua necessidade em 10 a 2000 caracteres."
    );
    try {
      UUID.fromString(input.requestKey());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(
        "Identificador da solicitação inválido."
      );
    }
    var existing = requests.findByCustomerIdAndRequestKey(
      customer.getId(),
      input.requestKey()
    );
    if (existing.isPresent()) {
      if (
        !existing.get().professional.getId().equals(input.serviceId())
      ) throw new ResponseStatusException(HttpStatus.CONFLICT);
      return view(existing.get());
    }
    if (
      input.preferredDate() != null &&
      input
        .preferredDate()
        .isBefore(LocalDate.now(ZoneId.of("America/Sao_Paulo")))
    ) throw new IllegalArgumentException("Data desejada no passado.");
    Professional professional = activeService(input.serviceId());
    User owner = users.findByEmail(professional.getUserEmail());
    if (
      !professional.isAcceptingRequests() || owner == null || !owner.isActive()
    ) throw new ResponseStatusException(
      HttpStatus.CONFLICT,
      "Profissional indisponível."
    );
    if (
      professional.getUserEmail().equals(customer.getEmail())
    ) throw new ResponseStatusException(HttpStatus.CONFLICT);
    ServiceRequest request = new ServiceRequest();
    request.customer = customer;
    request.professional = professional;
    request.description = input.description().trim();
    request.preferredDate = input.preferredDate();
    request.requestKey = input.requestKey();
    request.status = RequestStatus.REQUESTED;
    request.createdAt = Instant.now();
    request.updatedAt = request.createdAt;
    return view(requests.save(request));
  }

  @PatchMapping("/requests/{id}/status")
  @Transactional
  public RequestView transition(
    Authentication authentication,
    @PathVariable Long id,
    @RequestBody StatusChange input
  ) {
    User user = user(authentication);
    ServiceRequest request = requests
      .lockById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    boolean customer =
      user.getRole() == UserRole.USER &&
      request.customer.getId().equals(user.getId());
    boolean professional =
      user.getRole() == UserRole.PROFESSIONAL &&
      request.professional.getUserEmail().equals(user.getEmail());
    if (!customer && !professional) throw new ResponseStatusException(
      HttpStatus.NOT_FOUND
    );
    request.status = workflow.next(
      request.status,
      input.status(),
      customer,
      professional
    );
    request.updatedAt = Instant.now();
    return view(request);
  }

  @PostMapping("/requests/{id}/review")
  @Transactional
  public ReviewView review(
    Authentication authentication,
    @PathVariable Long id,
    @RequestBody NewReview input
  ) {
    User customer = require(authentication, UserRole.USER);
    ServiceRequest request = requests
      .lockById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (
      !request.customer.getId().equals(customer.getId())
    ) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    if (
      request.status != RequestStatus.COMPLETED || request.review != null
    ) throw new ResponseStatusException(
      HttpStatus.CONFLICT,
      "Atendimento sem avaliação disponível."
    );
    if (
      input.rating() < 1 ||
      input.rating() > 5 ||
      input.comment() == null ||
      input.comment().trim().length() < 10 ||
      input.comment().length() > 1000
    ) throw new IllegalArgumentException(
      "Informe nota de 1 a 5 e comentário de 10 a 1000 caracteres."
    );
    ServiceReview review = new ServiceReview();
    review.request = request;
    review.rating = input.rating();
    review.comment = input.comment().trim();
    review.createdAt = Instant.now();
    reviews.save(review);
    request.review = review;
    return view(review);
  }

  @GetMapping("/services/{id}/reviews")
  public Page<ReviewView> publicReviews(
    @PathVariable Long id,
    @RequestParam(defaultValue = "0") int page
  ) {
    activeService(id);
    return reviews
      .findByRequestProfessionalIdAndVisibleTrue(id, page(page, 10))
      .map(this::view);
  }

  @GetMapping("/summaries")
  public List<Summary> summaries(@RequestParam List<Long> ids) {
    if (ids.size() > 100) throw new IllegalArgumentException(
      "Limite de 100 serviços por consulta."
    );
    return reviews
      .summarize(ids)
      .stream()
      .map(row ->
        new Summary(
          (Long) row[0],
          ((Number) row[1]).doubleValue(),
          ((Number) row[2]).longValue()
        )
      )
      .toList();
  }

  @GetMapping("/favorites")
  public List<FavoriteView> favorites(Authentication authentication) {
    User user = require(authentication, UserRole.USER);
    return favorites
      .findByCustomerIdOrderByIdDesc(user.getId())
      .stream()
      .map(f -> f.professional)
      .filter(Professional::isActive)
      .map(p ->
        new FavoriteView(
          p.getId(),
          p.getName(),
          p.getDescription(),
          p.getStartingPrice(),
          p.isAcceptingRequests(),
          p.getCategory() == null
            ? "Serviços gerais"
            : p.getCategory().getName(),
          p.getCity(),
          p.getState(),
          p.getPhone()
        )
      )
      .toList();
  }

  @PutMapping("/favorites/{id}")
  @Transactional
  public void favorite(Authentication authentication, @PathVariable Long id) {
    User user = require(authentication, UserRole.USER);
    Professional professional = activeService(id);
    if (
      favorites.findByCustomerIdAndProfessionalId(user.getId(), id).isEmpty()
    ) {
      ServiceFavorite favorite = new ServiceFavorite();
      favorite.customer = user;
      favorite.professional = professional;
      favorites.save(favorite);
    }
  }

  @DeleteMapping("/favorites/{id}")
  @Transactional
  public void unfavorite(Authentication authentication, @PathVariable Long id) {
    User user = require(authentication, UserRole.USER);
    favorites
      .findByCustomerIdAndProfessionalId(user.getId(), id)
      .ifPresent(favorites::delete);
  }

  @GetMapping("/admin/dashboard")
  public Map<String, Long> dashboard(Authentication authentication) {
    require(authentication, UserRole.ADMIN);
    return Map.of(
      "users",
      users.count(),
      "activeServices",
      services.countByActiveTrue(),
      "categories",
      categories.count(),
      "requests",
      requests.count(),
      "pending",
      requests.countByStatus(RequestStatus.REQUESTED),
      "completed",
      requests.countByStatus(RequestStatus.COMPLETED),
      "reviews",
      reviews.count(),
      "hiddenReviews",
      reviews.countByVisibleFalse()
    );
  }

  @GetMapping("/admin/reviews")
  public Page<ReviewView> moderation(
    Authentication authentication,
    @RequestParam(defaultValue = "0") int page
  ) {
    require(authentication, UserRole.ADMIN);
    return reviews.findAll(page(page, 10)).map(this::view);
  }

  @PatchMapping("/admin/reviews/{id}")
  @Transactional
  public ReviewView moderate(
    Authentication authentication,
    @PathVariable Long id,
    @RequestParam boolean visible
  ) {
    require(authentication, UserRole.ADMIN);
    ServiceReview review = reviews
      .findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    review.visible = visible;
    return view(review);
  }
}
