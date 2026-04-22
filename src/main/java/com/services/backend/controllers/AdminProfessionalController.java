package com.services.backend.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

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
        Professional professional = professionalService.findById(id);
        if (professional == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(professional);
    }

    @PostMapping
    public ResponseEntity<Professional> createProfessional(@RequestBody Professional professional) {
        Professional saved = professionalService.insert(professional);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Professional> updateProfessional(@PathVariable Long id, @RequestBody Professional updatedData) {
        Professional existing = professionalService.findById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        updatedData.setId(id);
        Professional saved = professionalService.insert(updatedData);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProfessional(@PathVariable Long id) {
    
        professionalService.delete(id);
        return ResponseEntity.ok("Profissional deletado com sucesso!");

    }
    

}