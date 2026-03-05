package com.services.backend.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.services.backend.entities.Category;
import com.services.backend.repositories.CategoryRepository;

@Service
public class CategoryService {

    // Injeção de dependência: o Spring instancia o repositório automaticamente para nós
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<Category> findAll() {
        return repository.findAll();
    }

    public Category findById(Long id) {
        Optional<Category> obj = repository.findById(id);
        return obj.orElseThrow(() -> new RuntimeException("Category not found!"));
    }

    public Category insert(Category obj) {
        // 1. Remove espaços acidentais do início e do fim que o usuário possa ter digitado
        String nomeFormatado = obj.getName().trim();
        
        // 2. Vai no banco e pergunta: "Já tem alguma categoria com esse nome?"
        Optional<Category> existente = repository.findByNameIgnoreCase(nomeFormatado);
        
        // 3. Se já existir, aborta a criação e devolve a categoria antiga intacta!
        if (existente.isPresent()) {
            return existente.get();
        }
        
        // 4. Se não existir, nós garantimos que ela seja salva sem espaços extras
        obj.setName(nomeFormatado);
        return repository.save(obj);
    }
}