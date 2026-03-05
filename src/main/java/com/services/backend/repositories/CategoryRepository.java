package com.services.backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.services.backend.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Só de estender o JpaRepository, você já ganha métodos como:
    // save(), findAll(), findById(), deleteById()

    Optional<Category> findByNameIgnoreCase(String name);
}
