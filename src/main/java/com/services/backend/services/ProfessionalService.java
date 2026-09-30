package com.services.backend.services;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.services.backend.entities.Professional;
import com.services.backend.repositories.ProfessionalRepository;

@Service
public class ProfessionalService {
    private final ProfessionalRepository repository;
    private final RestTemplate restTemplate;

    public ProfessionalService(ProfessionalRepository repository) {
        this.repository = repository;
        this.restTemplate = new RestTemplate();
    }

    public List<Professional> findAll() {
        return repository.findByActiveTrue();
    }

    public Professional findById(Long id) {
        return repository.findById(id)
                .filter(Professional::isActive)
                .orElseThrow(() -> new NoSuchElementException("Profissional não encontrado."));
    }

    public Professional findOwnedById(Long id, String email) {
        return repository.findByIdAndUserEmail(id, email)
                .filter(Professional::isActive)
                .orElseThrow(() -> new NoSuchElementException("Profissional não encontrado."));
    }

    public List<Professional> findOwned(String email) {
        return repository.findByUserEmailAndActiveTrue(email);
    }

    public Professional insert(Professional obj) {
        enrichWithCoordinates(obj);
        return repository.save(obj);
    }

    private void enrichWithCoordinates(Professional obj) {
        try {
            String address = String.join(", ",
                    obj.getStreet() != null ? obj.getStreet() + ", " + obj.getAddressNumber() : "",
                    obj.getCity() != null ? obj.getCity() : "",
                    obj.getState() != null ? obj.getState() : "",
                    "Brazil");
            String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?q=" + encoded + "&format=json&limit=1";
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "PertoDeMim/1.0");
            ResponseEntity<String> response = restTemplate.exchange(
                    URI.create(url), HttpMethod.GET, new HttpEntity<>(headers), String.class);
            JsonNode root = new ObjectMapper().readTree(response.getBody());
            if (root.isArray() && !root.isEmpty()) {
                JsonNode first = root.get(0);
                obj.setLatitude(new BigDecimal(first.get("lat").asText()));
                obj.setLongitude(new BigDecimal(first.get("lon").asText()));
            }
        } catch (Exception e) {
            throw new ResourceAccessException("Falha no serviço de geocodificação.", e);
        }
    }

    public List<Professional> findByCategory(Long categoryId) {
        return repository.findByCategoryIdAndActiveTrue(categoryId);
    }

    public List<Professional> findNearby(Double lat, Double lon, Double radius) {
        if (lat == null || lat < -90 || lat > 90 || lon == null || lon < -180 || lon > 180
                || radius == null || radius <= 0 || radius > 500) {
            throw new IllegalArgumentException("Coordenadas ou raio inválidos.");
        }
        return repository.findNearby(lat, lon, radius);
    }

    @Transactional
    public void softDelete(Long id, String email) {
        Professional professional = findOwnedById(id, email);
        professional.setActive(false);
        repository.save(professional);
    }
}