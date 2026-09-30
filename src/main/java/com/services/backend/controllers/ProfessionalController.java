package com.services.backend.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.services.backend.entities.Professional;
import com.services.backend.services.ProfessionalService;

@RestController
@RequestMapping("/professionals")
public class ProfessionalController {
    private final ProfessionalService service;

    public ProfessionalController(ProfessionalService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Professional>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Professional> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Professional>> findByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(service.findByCategory(categoryId));
    }

    @PostMapping
    public ResponseEntity<Professional> createProfessional(@RequestBody Professional professional, Authentication authentication) {
        professional.setUserEmail(authentication.getName());
        professional.setActive(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.insert(professional));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Professional>> findNearby(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "10.0") Double radius) {
        List<Professional> professionals = service.findNearby(lat, lon, radius);
        if (categoryId != null) {
            professionals = professionals.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                    .toList();
        }
        return ResponseEntity.ok(professionals);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Professional> updateProfessional(
            @PathVariable Long id, @RequestBody Professional updatedData, Authentication authentication) {
        Professional professional = service.findOwnedById(id, authentication.getName());
        professional.setName(updatedData.getName() != null ? updatedData.getName() : professional.getName());
        professional.setPhone(updatedData.getPhone());
        professional.setCity(updatedData.getCity());
        professional.setState(updatedData.getState());
        professional.setStreet(updatedData.getStreet());
        professional.setAddressNumber(updatedData.getAddressNumber());
        professional.setComplement(updatedData.getComplement());
        professional.setZipCode(updatedData.getZipCode());
        if (updatedData.getCategory() != null) {
            professional.setCategory(updatedData.getCategory());
        }
        return ResponseEntity.ok(service.insert(professional));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id, Authentication authentication) {
        service.softDelete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Professional> getMyProfile(Authentication authentication) {
        return service.findOwned(authentication.getName()).stream().findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}