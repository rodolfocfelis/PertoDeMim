package com.services.backend.services;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.services.backend.repositories.ProfessionalRepository;

// Habilita os superpoderes do Mockito nesta classe
@ExtendWith(MockitoExtension.class)
class ProfessionalServiceTest {

    // 1. O Dublê: Criamos um repositório falso para não sujar o banco de dados
    @Mock
    private ProfessionalRepository repository;

    // 2. O Alvo: Injetamos o repositório falso dentro do serviço real que queremos testar
    @InjectMocks
    private ProfessionalService service;

    // --- TESTE 1: Caminho de Erro ---
    @Test
    @DisplayName("Deve lançar NoSuchElementException ao tentar deletar ID inexistente")
    void delete_WhenIdDoesNotExist_ShouldThrowException() {
        // GIVEN (Dado que...)
        Long idInexistente = 99L;
        when(repository.existsById(idInexistente)).thenReturn(false);

        // WHEN & THEN (Quando agir, Então espero que...)
        assertThrows(NoSuchElementException.class, () -> {
            service.delete(idInexistente);
        });

        // Verifica se o método delete do banco NUNCA foi chamado por acidente
        verify(repository, never()).deleteById(anyLong());
    }

    // --- TESTE 2: Caminho Feliz ---
    @Test
    @DisplayName("Deve deletar com sucesso quando o ID existe")
    void delete_WhenIdExists_ShouldDeleteSuccessfully() {
        // GIVEN (Dado que...)
        Long idExistente = 1L;
        when(repository.existsById(idExistente)).thenReturn(true);
        // Observação: O método deleteById do repositório é void, então não precisamos fazer um 'when' para ele.

        // WHEN (Quando eu chamar o método...)
        assertDoesNotThrow(() -> {
            service.delete(idExistente);
        });

        // THEN (Então o repositório deve ter sido chamado exatamente 1 vez com esse ID)
        verify(repository, times(1)).deleteById(idExistente);
    }
}