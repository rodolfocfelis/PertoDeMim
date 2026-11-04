package com.services.backend.services;
import com.services.backend.entities.Professional;
import com.services.backend.repositories.ProfessionalRepository;
import java.util.Optional;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ProfessionalServiceTest {
    @Mock ProfessionalRepository repository;
    @InjectMocks ProfessionalService service;
    @Test void softDeletePreservesTheRecord() {
        Professional professional = new Professional();
        professional.setId(1L);
        professional.setActive(true);
        when(repository.findByIdAndUserEmail(1L, "owner@test.local")).thenReturn(Optional.of(professional));
        service.softDelete(1L, "owner@test.local");
        assertFalse(professional.isActive());
        verify(repository).save(professional);
        verify(repository, never()).deleteById(anyLong());
    }
    @Test void anotherOwnerCannotDeactivateTheRecord() {
        when(repository.findByIdAndUserEmail(1L, "other@test.local")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.softDelete(1L, "other@test.local"));
        verify(repository, never()).save(any());
    }
    @Test void invalidCoordinatesNeverReachDatabase() {
        assertThrows(IllegalArgumentException.class, () -> service.findNearby(Double.NaN, 0.0, 10.0));
        assertThrows(IllegalArgumentException.class, () -> service.findNearby(0.0, Double.POSITIVE_INFINITY, 10.0));
        assertThrows(IllegalArgumentException.class, () -> service.findNearby(0.0, 0.0, -1.0));
        verifyNoInteractions(repository);
    }
}
