package com.services.backend.services;
import com.services.backend.entities.User;
import com.services.backend.entities.dtos.AccountDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
public class AccountDetailsService {
    private final RestTemplate http;
    public AccountDetailsService() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(4000);
        factory.setReadTimeout(6000);
        http = new RestTemplate(factory);
    }
    public void apply(User user, AccountDetails details) {
        String phone = clean(details.phone()).replaceAll("[^0-9]", "");
        String zip = clean(details.zipCode()).replaceAll("[^0-9]", "");
        String state = clean(details.state()).toUpperCase();
        if (!phone.matches("[0-9]{10,11}") || !zip.matches("[0-9]{8}")
                || !state.matches("[A-Z]{2}") || clean(details.street()).isEmpty()
                || clean(details.city()).isEmpty() || clean(details.addressNumber()).isEmpty()
                || clean(details.street()).length() > 150 || clean(details.city()).length() > 100
                || clean(details.addressNumber()).length() > 20 || clean(details.complement()).length() > 100
                || clean(details.neighborhood()).length() > 100 || clean(details.bio()).length() > 2000) {
            throw new IllegalArgumentException("Confira telefone com DDD, CEP e endereço completo.");
        }
        boolean changed = user.getLatitude() == null || user.getLongitude() == null
                || !Objects.equals(user.getStreet(), clean(details.street()))
                || !Objects.equals(user.getAddressNumber(), clean(details.addressNumber()))
                || !Objects.equals(user.getCity(), clean(details.city()))
                || !Objects.equals(user.getState(), state) || !Objects.equals(user.getZipCode(), zip);
        if (changed) {
            String address = clean(details.street()) + ", " + clean(details.addressNumber()) + ", "
                    + clean(details.city()) + ", " + state + ", " + zip + ", Brasil";
            try {
                String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
                var headers = new HttpHeaders(); headers.set("User-Agent", "PertoDeMim/1.0");
                var response = http.exchange(URI.create("https://nominatim.openstreetmap.org/search?format=json&countrycodes=br&limit=1&q=" + encoded), HttpMethod.GET, new HttpEntity<>(headers), String.class);
                var results = new ObjectMapper().readTree(response.getBody());
                if (!results.isArray() || results.isEmpty()) throw new IllegalArgumentException("Endereço não localizado. Confira rua, número, CEP e cidade.");
                double lat = Double.parseDouble(results.get(0).get("lat").asText());
                double lon = Double.parseDouble(results.get(0).get("lon").asText());
                if (!Double.isFinite(lat) || !Double.isFinite(lon) || Math.abs(lat)>90 || Math.abs(lon)>180) throw new IllegalArgumentException("Endereço inválido.");
                user.setLatitude(lat); user.setLongitude(lon);
            } catch (IllegalArgumentException ex) { throw ex; }
            catch (Exception ex) { throw new org.springframework.web.server.ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Não foi possível validar o endereço. Tente novamente."); }
        }
        user.setPhone(phone); user.setZipCode(zip); user.setState(state);
        user.setStreet(clean(details.street())); user.setAddressNumber(clean(details.addressNumber()));
        user.setCity(clean(details.city())); user.setNeighborhood(clean(details.neighborhood()));
        user.setComplement(clean(details.complement())); user.setBio(clean(details.bio()));
    }
    private String clean(String value) { return value == null ? "" : value.trim(); }
}
