package com.services.backend.services;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.services.backend.entities.Professional;
import com.services.backend.repositories.ProfessionalRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalServiceTest {
    @Mock
    private ProfessionalRepository repository;
    @InjectMocks
    private ProfessionalService service;

    @Test
    void ownerCanDeactivateWithoutDeletingTheRecord() {
        Professional professional = new Professional();
        when(repository.findByIdAndUserEmail(1L, "owner@test.local"))
                .thenReturn(Optional.of(professional));
        service.softDelete(1L, "owner@test.local");
        assertFalse(professional.isActive());
        verify(repository).save(professional);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void anotherOwnerCannotDeactivateTheRecord() {
        when(repository.findByIdAndUserEmail(1L, "other@test.local"))
                .thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> service.softDelete(1L, "other@test.local"));
        verify(repository, never()).save(any());
    }

    @Test
    void adminCanDeactivateWithoutDeletingTheRecord() {
        Professional professional = new Professional();
        when(repository.findById(1L)).thenReturn(Optional.of(professional));
        service.softDeleteAdmin(1L);
        assertFalse(professional.isActive());
        verify(repository).save(professional);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void adminCannotDeactivateMissingRecord() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.softDeleteAdmin(99L));
        verify(repository, never()).save(any());
    }
}
