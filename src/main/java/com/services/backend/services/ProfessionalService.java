package com.services.backend.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.services.backend.entities.Professional;
import com.services.backend.repositories.ProfessionalRepository;

@Service
public class ProfessionalService {

    private final ProfessionalRepository repository;

    public ProfessionalService(ProfessionalRepository repository) {
        this.repository = repository;
    }

    public List<Professional> findAll() {
        return repository.findAll();
    }

    public Professional findById(Long id) {
        Optional<Professional> obj = repository.findById(id);
        return obj.orElseThrow(() -> new RuntimeException("Professional not found!"));
    }

    public Professional insert(Professional obj) {
        // Aqui no futuro podemos colocar regras, como verificar se o telefone é válido,
        // antes de mandar o repository salvar no banco.
        return repository.save(obj);
    }
    
    public List<Professional> findByCategory(Long categoryId) {
        return repository.findByCategoryId(categoryId);
    }

    public List<Professional> findNearby(Double lat, Double lon, Double radius) {
        return repository.findNearby(lat, lon, radius);
    }
}