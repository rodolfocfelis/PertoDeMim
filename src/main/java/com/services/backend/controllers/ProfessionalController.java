package com.services.backend.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.services.backend.entities.Professional;
import com.services.backend.services.ProfessionalService;

@RestController
@RequestMapping(value = "/professionals")
public class ProfessionalController {

    private final ProfessionalService service;

    public ProfessionalController(ProfessionalService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Professional>> findAll() {
        List<Professional> list = service.findAll();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Professional> findById(@PathVariable Long id) {
        Professional obj = service.findById(id);
        return ResponseEntity.ok().body(obj);
    }

    @GetMapping(value = "/category/{categoryId}")
    public ResponseEntity<List<Professional>> findByCategory(@PathVariable Long categoryId) {
        List<Professional> list = service.findByCategory(categoryId);
        return ResponseEntity.ok().body(list);
    }

    @PostMapping
    public ResponseEntity<Professional> insert(@RequestBody Professional obj) {
        obj = service.insert(obj);
        // Boa prática em APIs REST: Retornar o status 201 (Created) e o endereço do novo recurso criado
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(obj.getId()).toUri();
        return ResponseEntity.created(uri).body(obj);
    }

    // A rota será algo como: /professionals/search?lat=-23.5&lon=-46.6&radius=15.0
    @GetMapping(value = "/search")
    public ResponseEntity<List<Professional>> findNearby(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "10.0") Double radius) {
                
        List<Professional> professionals = service.findNearby(lat, lon, radius);
        
        if (categoryId != null) {
            professionals = professionals.stream()
                    .filter(p -> p.getCategory().getId().equals(categoryId))
                    .toList();
        }

        return ResponseEntity.ok().body(professionals);
    }
}