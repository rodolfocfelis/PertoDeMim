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

    public AdminController(UserRepository users, CategoryRepository categories) {
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
        String query = name == null || name.isBlank() ? null : name.trim();
        return users.search(query, role, active,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "name")));
    }

    @PatchMapping("/users/{id}/status")
    @Transactional
    public ResponseEntity<User> setUserStatus(@PathVariable Long id, @RequestParam boolean active) {
        User user = users.findById(id).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado."));
        user.setActive(active);
        return ResponseEntity.ok(users.save(user));
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