package com.services.backend.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.services.backend.entities.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUserIdAndProfessionalId(Long userId, Long professionalId);
    List<Review> findByProfessionalIdOrderByIdDesc(Long professionalId);
}
