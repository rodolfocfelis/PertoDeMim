package com.services.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.services.backend.entities.Professional;

@Repository
public interface ProfessionalRepository extends JpaRepository<Professional, Long> {
    
    List<Professional> findByCategoryId(Long categoryId);

    // O valor 6371 é o raio da Terra em quilômetros. 
    // Essa query calcula a distância, filtra pelo raio máximo e ordena do mais perto pro mais longe.
    @Query(value = "SELECT * FROM professional p " +
           "WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * " +
           "cos(radians(p.longitude) - radians(:lon)) + " +
           "sin(radians(:lat)) * sin(radians(p.latitude)))) <= :radius " +
           "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * " +
           "cos(radians(p.longitude) - radians(:lon)) + " +
           "sin(radians(:lat)) * sin(radians(p.latitude)))) ASC", 
           nativeQuery = true)
    List<Professional> findNearby(@Param("lat") Double lat, 
                                  @Param("lon") Double lon, 
                                  @Param("radius") Double radius);
}
