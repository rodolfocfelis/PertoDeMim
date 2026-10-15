package com.services.backend.controllers;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.services.backend.entities.Review;
import com.services.backend.entities.User;
import com.services.backend.repositories.ProfessionalRepository;
import com.services.backend.repositories.ReviewRepository;

@RestController
@RequestMapping("/professionals/{professionalId}/reviews")
public class ReviewController {
    private final ReviewRepository reviews;
    private final ProfessionalRepository professionals;

    public ReviewController(ReviewRepository reviews, ProfessionalRepository professionals) {
        this.reviews = reviews;
        this.professionals = professionals;
    }

    public record ReviewInput(Integer rating, String comment) {}
    public record ReviewView(Long id, Long userId, int rating, String comment) {}
    public record ReviewSummary(Long professionalId, long count, double averageRating) {}

    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@PathVariable Long professionalId, @RequestBody ReviewInput input,
                                    @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (input.rating() == null || input.rating() < 1 || input.rating() > 5
                || (input.comment() != null && input.comment().length() > 2000)) {
            return ResponseEntity.badRequest().body(Map.of("error", "A nota deve ser de 1 a 5 e o comentário ter até 2000 caracteres."));
        }
        var professional = professionals.findById(professionalId)
                .orElseThrow(() -> new NoSuchElementException("Profissional não encontrado."));
        if (reviews.existsByUserIdAndProfessionalId(user.getId(), professionalId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Você já avaliou este profissional."));
        }
        Review review = new Review();
        review.setUser(user);
        review.setProfessional(professional);
        review.setRating(input.rating());
        review.setComment(input.comment());
        try {
            Review saved = reviews.saveAndFlush(review);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ReviewView(saved.getId(), user.getId(), saved.getRating(), saved.getComment()));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Você já avaliou este profissional."));
        }
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ReviewView> list(@PathVariable Long professionalId) {
        if (!professionals.existsById(professionalId)) throw new NoSuchElementException("Profissional não encontrado.");
        return reviews.findByProfessionalIdOrderByIdDesc(professionalId).stream()
                .map(r -> new ReviewView(r.getId(), r.getUser().getId(), r.getRating(), r.getComment()))
                .toList();
    }

    @GetMapping("/summary")
    @Transactional(readOnly = true)
    public ReviewSummary summary(@PathVariable Long professionalId) {
        if (!professionals.existsById(professionalId)) throw new NoSuchElementException("Profissional não encontrado.");
        var ratings = reviews.findByProfessionalIdOrderByIdDesc(professionalId);
        double average = ratings.stream().mapToInt(Review::getRating).average().orElse(0.0);
        return new ReviewSummary(professionalId, ratings.size(), average);
    }
}
