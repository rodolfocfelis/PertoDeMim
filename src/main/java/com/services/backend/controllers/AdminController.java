package com.services.backend.controllers;

import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.services.backend.entities.Category;
import com.services.backend.entities.User;
import com.services.backend.entities.enums.UserRole;
import com.services.backend.repositories.CategoryRepository;
import com.services.backend.repositories.UserRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository users;
    private final CategoryRepository categories;
    private final com.services.backend.services.AccountDetailsService details;
    private final com.services.backend.repositories.ProfessionalRepository professionals;
    private final com.services.backend.services.TokenService tokens;

    public AdminController(UserRepository users, CategoryRepository categories, com.services.backend.services.AccountDetailsService details, com.services.backend.repositories.ProfessionalRepository professionals, com.services.backend.services.TokenService tokens) {
        this.details=details; this.professionals=professionals; this.tokens=tokens;
        this.users = users;
        this.categories = categories;
    }

    @GetMapping("/users")
    public Page<User> listUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        String query = name == null ? "" : name.trim();
        return users.search(query, role, active,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "name")));
    }

    @PatchMapping("/users/{id}/status")
    @Transactional
    public ResponseEntity<User> setUserStatus(@PathVariable Long id, @RequestParam boolean active,
            org.springframework.security.core.Authentication authentication) {
        User user = users.findById(id).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado."));
        if (!active && user.getEmail().equals(authentication.getName())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "Sua conta não pode ser desativada por esta ação.");
        }
        user.setActive(active);
        return ResponseEntity.ok(users.save(user));
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return users.findById(id).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado."));
    }

    @PutMapping("/users/{id}")
    @Transactional
    public java.util.Map<String, Object> updateUser(@PathVariable Long id,
            @RequestBody MeController.ProfileUpdate update,
            org.springframework.security.core.Authentication authentication) {
        User user=getUser(id);
        if (update.name()==null || update.name().isBlank() || update.name().length()>150
                || update.email()==null || update.email().length()>254
                || !update.email().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Nome ou e-mail inválidos.");
        User existing=users.findByEmail(update.email().trim());
        if(existing!=null && !existing.getId().equals(id)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT);
        boolean own=user.getEmail().equals(authentication.getName());
        String oldEmail=user.getEmail();
        details.apply(user,new com.services.backend.entities.dtos.AccountDetails(update.phone(),update.zipCode(),update.street(),update.addressNumber(),update.complement(),update.neighborhood(),update.city(),update.state(),update.bio()));
        user.setName(update.name().trim()); user.setEmail(update.email().trim()); users.saveAndFlush(user);
        for(var professional:professionals.findByUserEmail(oldEmail)) professional.setUserEmail(user.getEmail());
        if(own) return java.util.Map.of("user",user,"token",tokens.generateToken(user));
        return java.util.Map.of("user",user);
    }

    @GetMapping("/categories")
    public Page<Category> listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        return categories.findAll(PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Direction.ASC, "name")));
    }

    @PostMapping("/categories")
    @Transactional
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        category.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(categories.save(category));
    }

    @PutMapping("/categories/{id}")
    @Transactional
    public Category updateCategory(@PathVariable Long id, @RequestBody Category changes) {
        Category category = categories.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Categoria não encontrada."));
        category.setName(changes.getName());
        category.setDescription(changes.getDescription());
        return categories.save(category);
    }

    @DeleteMapping("/categories/{id}")
    @Transactional
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        Category category = categories.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Categoria não encontrada."));
        categories.delete(category);
        return ResponseEntity.noContent().build();
    }
}
