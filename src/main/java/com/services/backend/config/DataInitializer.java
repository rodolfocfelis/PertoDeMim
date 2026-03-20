package com.services.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.services.backend.entities.User;
import com.services.backend.entities.enums.UserRole;
import com.services.backend.repositories.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        
        String adminEmail = "admin@pertodemim.com";
        
        if (userRepository.findByEmail(adminEmail) == null) {
            
            System.out.println("🌱 Semeando banco de dados: Criando usuário ADMIN padrão...");
            
            String senhaCriptografada = new BCryptPasswordEncoder().encode("admin123");
            
            User defaultAdmin = new User(
                null,                  
                "Administrador Geral",
                adminEmail,          
                senhaCriptografada,
                UserRole.ADMIN 
            );
            
            userRepository.save(defaultAdmin);
            
            System.out.println("✅ ADMIN criado com sucesso!");
            System.out.println("📧 E-mail: " + adminEmail);
            System.out.println("🔑 Senha: admin123");
            System.out.println("⚠️ Lembre-se de alterar essa senha em produção!");
        }
    }
}
