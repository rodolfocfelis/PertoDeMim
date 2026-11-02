package com.services.backend.market;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface ServiceFavoriteRepository
  extends JpaRepository<ServiceFavorite, Long>
{
  Optional<ServiceFavorite> findByCustomerIdAndProfessionalId(
    Long customerId,
    Long professionalId
  );

  @EntityGraph(attributePaths = { "professional", "professional.category" })
  List<ServiceFavorite> findByCustomerIdOrderByIdDesc(Long customerId);
}
