package com.services.backend.repositories;

import com.services.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // O Spring Security vai usar este método para encontrar o utilizador na hora do login
    UserDetails findByEmail(String email);
}