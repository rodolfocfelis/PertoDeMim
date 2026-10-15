package com.services.backend.services;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.services.backend.entities.User;

@Service
public class UserGeocodingService {
    private final RestTemplate client = new RestTemplate();

    public record Coordinates(BigDecimal latitude, BigDecimal longitude) {}

    public Coordinates geocode(User user) {
        try {
            String address = String.join(", ", user.getStreet(), user.getAddressNumber(),
                    user.getCity(), user.getState(), user.getZipCode(), "Brazil");
            String url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q="
                    + URLEncoder.encode(address, StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "PertoDeMim/1.0 (user address registration)");
            var response = client.exchange(URI.create(url), HttpMethod.GET,
                    new HttpEntity<>(headers), String.class);
            JsonNode data = new ObjectMapper().readTree(response.getBody());
            if (!data.isArray() || data.isEmpty()) {
                throw new IllegalArgumentException("Não foi possível localizar o endereço informado.");
            }
            return new Coordinates(new BigDecimal(data.get(0).get("lat").asText()),
                    new BigDecimal(data.get(0).get("lon").asText()));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceAccessException("Serviço de geocodificação indisponível.", e);
        }
    }
}
