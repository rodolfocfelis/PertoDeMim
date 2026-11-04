package com.services.backend.market;

import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ServiceReviewRepository
  extends JpaRepository<ServiceReview, Long>
{
  @EntityGraph(
    attributePaths = { "request", "request.customer", "request.professional" }
  )
  Page<ServiceReview> findByRequestProfessionalIdAndVisibleTrue(
    Long id,
    Pageable page
  );

  @Override
  @EntityGraph(
    attributePaths = { "request", "request.customer", "request.professional" }
  )
  Page<ServiceReview> findAll(Pageable page);

  @Query(
    "select v.request.professional.id, avg(v.rating), count(v) from ServiceReview v where v.visible=true and v.request.professional.id in :ids group by v.request.professional.id"
  )
  List<Object[]> summarize(@Param("ids") List<Long> ids);

  long countByVisibleFalse();
}
