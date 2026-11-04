package com.services.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.services.backend.entities.User;
import com.services.backend.entities.dtos.AuthenticationDTO;
import com.services.backend.entities.dtos.LoginResponseDTO;
import com.services.backend.entities.dtos.RegisterDTO;
import com.services.backend.entities.dtos.ProfileDTO;
import com.services.backend.repositories.UserRepository;
import com.services.backend.services.TokenService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository repository;

    @Autowired
    private TokenService tokenService;

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

    @Autowired
    private com.services.backend.services.AccountDetailsService accountDetails;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDTO data) {
        if (data.getName() == null || data.getName().isBlank() || data.getName().length() > 150
                || data.getEmail() == null || !data.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || data.getPassword() == null || data.getPassword().length() < 8 || data.getPassword().length() > 72 || data.getRole() == null) {
            throw new IllegalArgumentException("Informe nome, e-mail e senha de 8 a 72 caracteres.");
        }
        if (this.repository.findByEmail(data.getEmail()) != null) {
            return ResponseEntity.badRequest().body("E-mail já cadastrado!");
        }

        if (data.getRole() == com.services.backend.entities.enums.UserRole.ADMIN) {
            var auth = SecurityContextHolder.getContext().getAuthentication();

            // Se não tiver ninguém logado, ou se quem estiver logado NÃO for ADMIN, bloqueia!
            if (auth == null || auth.getPrincipal().equals("anonymousUser") ||
                !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Acesso negado: Apenas um Administrador pode cadastrar outro Administrador.");
            }
        }

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.getPassword());
        User newUser = new User(null, data.getName(), data.getEmail(), encryptedPassword, data.getRole());
        accountDetails.apply(newUser, new com.services.backend.entities.dtos.AccountDetails(data.getPhone(), data.getZipCode(), data.getStreet(), data.getAddressNumber(), data.getComplement(), data.getNeighborhood(), data.getCity(), data.getState(), data.getBio()));
        this.repository.save(newUser);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();

        User user = this.repository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(404).build();
        }

        // Criar um DTO simples sem senha e com role como string
        ProfileDTO response = new ProfileDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null
        );

        return ResponseEntity.ok(response);
    }
}
