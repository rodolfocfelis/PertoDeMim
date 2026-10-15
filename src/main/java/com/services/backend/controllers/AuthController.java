package com.services.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.services.backend.entities.User;
import com.services.backend.entities.dtos.AuthenticationDTO;
import com.services.backend.entities.dtos.LoginResponseDTO;
import com.services.backend.entities.dtos.RegisterDTO;
import com.services.backend.repositories.UserRepository;
import com.services.backend.services.TokenService;
import com.services.backend.services.UserGeocodingService;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository repository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserGeocodingService geocodingService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody AuthenticationDTO data) {
        // Pega o e-mail e senha digitados e tenta autenticar
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        // Se a senha estiver correta, gera o Token JWT
        var user = (User) auth.getPrincipal();
        var token = tokenService.generateToken(user);

        // Devolve o Token e a Role (ADMIN, USER, PROFESSIONAL) para o JavaScript
        return ResponseEntity.ok(new LoginResponseDTO(token, user.getRole().getRole()));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDTO data) {
        if (this.repository.findByEmail(data.email()) != null) {
            return ResponseEntity.badRequest().body("E-mail já cadastrado!");
        }

        if (data.role() == com.services.backend.entities.enums.UserRole.ADMIN) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            
            // Se não tiver ninguém logado, ou se quem estiver logado NÃO for ADMIN, bloqueia!
            if (auth == null || auth.getPrincipal().equals("anonymousUser") || 
                !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Acesso negado: Apenas um Administrador pode cadastrar outro Administrador.");
            }
        }
        
        if (data.zipCode() == null || !data.zipCode().replaceAll("\\D", "").matches("\\d{8}")
                || data.street() == null || data.street().isBlank()
                || data.addressNumber() == null || data.addressNumber().isBlank()
                || data.city() == null || data.city().isBlank()
                || data.state() == null || !data.state().matches("(?i)[A-Z]{2}")) {
            return ResponseEntity.badRequest().body("Endereço obrigatório ou inválido.");
        }
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(null, data.name(), data.email(), encryptedPassword, data.role());
        newUser.setZipCode(data.zipCode().replaceAll("\\D", ""));
        newUser.setStreet(data.street().trim());
        newUser.setAddressNumber(data.addressNumber().trim());
        newUser.setComplement(data.complement());
        newUser.setCity(data.city().trim());
        newUser.setState(data.state().toUpperCase());
        var coordinates = geocodingService.geocode(newUser);
        newUser.setLatitude(coordinates.latitude());
        newUser.setLongitude(coordinates.longitude());
        this.repository.save(newUser);

        return ResponseEntity.ok().build();
    }
}
