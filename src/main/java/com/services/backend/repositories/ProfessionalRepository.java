package com.services.backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.services.backend.entities.Professional;

@Repository
public interface ProfessionalRepository extends JpaRepository<Professional, Long> {
    List<Professional> findByActiveTrue();
    List<Professional> findByCategoryIdAndActiveTrue(Long categoryId);
    List<Professional> findByUserEmailAndActiveTrue(String userEmail);
    Optional<Professional> findByIdAndUserEmail(Long id, String userEmail);

    @Query(value = "SELECT * FROM professional p " +
           "WHERE p.active = true AND (6371 * acos(LEAST(1.0, GREATEST(-1.0, " +
           "cos(radians(:lat)) * cos(radians(p.latitude)) * " +
           "cos(radians(p.longitude) - radians(:lon)) + " +
           "sin(radians(:lat)) * sin(radians(p.latitude)))))) <= :radius " +
           "ORDER BY (6371 * acos(LEAST(1.0, GREATEST(-1.0, " +
           "cos(radians(:lat)) * cos(radians(p.latitude)) * " +
           "cos(radians(p.longitude) - radians(:lon)) + " +
           "sin(radians(:lat)) * sin(radians(p.latitude)))))) ASC",
           nativeQuery = true)
    List<Professional> findNearby(@Param("lat") Double lat,
                                  @Param("lon") Double lon,
                                  @Param("radius") Double radius);
}