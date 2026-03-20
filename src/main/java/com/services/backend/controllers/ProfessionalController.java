package com.services.backend.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Professional> createProfessional(@RequestBody Professional newProfessional) {
        
        // 1. Descobre quem está logado mandando a requisição
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailLogado = authentication.getName(); 

        // 2. Carimba o e-mail do usuário no perfil do profissional ANTES de salvar no banco
        newProfessional.setUserEmail(emailLogado);

        // 3. Salva no banco (substitua pela sua linha de save atual)
        Professional savedProfessional = service.insert(newProfessional);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProfessional);
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

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateProfessional(@PathVariable Long id, @RequestBody Professional updatedData) {
        
        // 1. Identifica QUEM está fazendo a requisição lendo o Token JWT
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // O getName() geralmente traz o 'subject' do JWT, que configuramos como sendo o E-mail do usuário no AuthController
        String emailLogado = authentication.getName(); 

        // 2. Busca o profissional no banco de dados
        Professional professional = service.findById(id);
        
        if (professional == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profissional não encontrado.");
        }

        if (professional.getUserEmail() == null || !professional.getUserEmail().equals(emailLogado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso negado: Você só tem permissão para editar o seu próprio perfil.");
        }

        // 4. Atualiza apenas os dados permitidos (nunca atualize o ID ou dados sensíveis aqui)
        professional.setPhone(updatedData.getPhone());
        professional.setCity(updatedData.getCity());
        professional.setState(updatedData.getState());
        professional.setStreet(updatedData.getStreet());
        professional.setAddressNumber(updatedData.getAddressNumber());
        professional.setComplement(updatedData.getComplement());
        
        // Se a categoria puder ser alterada:
        if (updatedData.getCategory() != null) {
            professional.setCategory(updatedData.getCategory());
        }

        // Salva as alterações
        service.insert(professional);

        return ResponseEntity.ok(professional);
    }

    @GetMapping("/me")
    public ResponseEntity<Professional> getMyProfile() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailLogado = authentication.getName();

        List<Professional> todos = service.findAll();
        Optional<Professional> meuPerfil = todos.stream()
                .filter(p -> p.getUserEmail() != null && p.getUserEmail().equals(emailLogado))
                .findFirst();

        if (meuPerfil.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(meuPerfil.get());
    }
}