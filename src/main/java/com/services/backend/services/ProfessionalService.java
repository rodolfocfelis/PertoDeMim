package com.services.backend.services;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.services.backend.entities.Professional;
import com.services.backend.repositories.ProfessionalRepository;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProfessionalService {

    private final ProfessionalRepository repository;
    private final RestTemplate restTemplate;

    public ProfessionalService(ProfessionalRepository repository) {
        this.repository = repository;
        this.restTemplate = new RestTemplate();
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
        enrichWithCoordinates(obj);
        return repository.save(obj);
    }

    private void enrichWithCoordinates(Professional obj) {
        try {
            String address = String.join(", ",
                    obj.getStreet() != null ? obj.getStreet() + ", " + obj.getAddressNumber() : "",
                    obj.getCity() != null ? obj.getCity() : "",
                    obj.getState() != null ? obj.getState() : "",
                    "Brazil"
            );

            String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?q=" + encoded + "&format=json&limit=1";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "PertoDeMim/1.0");
            HttpEntity<String> entity = new HttpEntity<String>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    URI.create(url),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            if (root.isArray() && root.size() > 0) {
                JsonNode first = root.get(0);
                obj.setLatitude(new BigDecimal(first.get("lat").asText()));
                obj.setLongitude(new BigDecimal(first.get("lon").asText()));
            }
        } catch (Exception e) {
            throw new ResourceAccessException(
                    "Failed to reach Nominatim geocoding service: " + e.getMessage()
            );
        }
    }
    
    public List<Professional> findByCategory(Long categoryId) {
        return repository.findByCategoryId(categoryId);
    }

    public List<Professional> findNearby(Double lat, Double lon, Double radius) {
        return repository.findNearby(lat, lon, radius);
    }
}