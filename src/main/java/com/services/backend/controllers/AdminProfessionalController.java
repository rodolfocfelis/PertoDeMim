package com.services.backend.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.services.backend.entities.Professional;
import com.services.backend.services.ProfessionalService;

@RestController
@RequestMapping("/admin/professionals")
public class AdminProfessionalController {
    private final ProfessionalService professionalService;

    public AdminProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @GetMapping
    public ResponseEntity<List<Professional>> getAllProfessionals() {
        return ResponseEntity.ok(professionalService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Professional> getProfessionalById(@PathVariable Long id) {
        return ResponseEntity.ok(professionalService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Professional> createProfessional(@RequestBody Professional professional) {
        Professional saved = professionalService.insert(professional);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Professional> updateProfessional(
            @PathVariable Long id, @RequestBody Professional updatedData) {
        Professional existing = professionalService.findById(id);
        updatedData.setId(id);
        updatedData.setUserEmail(existing.getUserEmail());
        updatedData.setActive(existing.isActive());
        return ResponseEntity.ok(professionalService.insert(updatedData));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateProfessional(@PathVariable Long id) {
        professionalService.softDeleteAdmin(id);
        return ResponseEntity.noContent().build();
    }
}