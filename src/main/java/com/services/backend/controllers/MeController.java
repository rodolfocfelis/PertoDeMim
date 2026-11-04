package com.services.backend.controllers;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.services.backend.entities.User;
import com.services.backend.repositories.UserRepository;
import com.services.backend.repositories.ProfessionalRepository;
import com.services.backend.services.TokenService;

@RestController
@RequestMapping("/api/me")
public class MeController {
    private final UserRepository users;
    private final ProfessionalRepository professionals;
    private final TokenService tokens;
    private final com.services.backend.services.AccountDetailsService details;
    public MeController(UserRepository users, ProfessionalRepository professionals, TokenService tokens, com.services.backend.services.AccountDetailsService details) {
        this.details = details;
        this.users = users;
        this.professionals = professionals;
        this.tokens = tokens;
    }
    public record ProfileData(Long id, String name, String email, String role, boolean active, String phone, String zipCode, String street, String addressNumber, String complement, String neighborhood, String city, String state, String bio, Double latitude, Double longitude) {}
    public record ProfileUpdate(String name, String email, String phone, String zipCode, String street, String addressNumber, String complement, String neighborhood, String city, String state, String bio) {}
    private ProfileData profile(User user) {
        return new ProfileData(user.getId(), user.getName(), user.getEmail(), user.getRole().name(), user.isActive(), user.getPhone(), user.getZipCode(), user.getStreet(), user.getAddressNumber(), user.getComplement(), user.getNeighborhood(), user.getCity(), user.getState(), user.getBio(), user.getLatitude(), user.getLongitude());
    }
    @GetMapping
    public ProfileData get(Authentication authentication) {
        return profile((User) users.findByEmail(authentication.getName()));
    }
    @PutMapping
    @Transactional
    public Map<String, Object> update(Authentication authentication, @RequestBody ProfileUpdate update) {
        if (update.name() == null || update.name().isBlank() || update.name().trim().length() > 150
                || update.email() == null || update.email().length() > 254
                || !update.email().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Nome ou e-mail inválidos.");
        }
        User user = (User) users.findByEmail(authentication.getName());
        String email = update.email().trim();
        User existing = (User) users.findByEmail(email);
        if (existing != null && !existing.getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
        }
        details.apply(user, new com.services.backend.entities.dtos.AccountDetails(update.phone(), update.zipCode(), update.street(), update.addressNumber(), update.complement(), update.neighborhood(), update.city(), update.state(), update.bio()));
        String previousEmail = user.getEmail();
        user.setName(update.name().trim());
        user.setEmail(email);
        users.saveAndFlush(user);
        for (var professional : professionals.findByUserEmail(previousEmail)) {
            professional.setUserEmail(email);
        }
        return Map.of("user", profile(user), "token", tokens.generateToken(user));
    }
}
