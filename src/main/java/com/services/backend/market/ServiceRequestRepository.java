package com.services.backend.market;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ServiceRequestRepository
  extends JpaRepository<ServiceRequest, Long>
{
  Optional<ServiceRequest> findByCustomerIdAndRequestKey(
    Long customerId,
    String key
  );

  @EntityGraph(attributePaths = { "customer", "professional", "review" })
  Page<ServiceRequest> findByCustomerIdAndStatusIn(
    Long id,
    java.util.Collection<RequestStatus> status,
    Pageable page
  );

  @EntityGraph(attributePaths = { "customer", "professional", "review" })
  Page<ServiceRequest> findByProfessionalUserEmailAndStatusIn(
    String email,
    java.util.Collection<RequestStatus> status,
    Pageable page
  );

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select r from ServiceRequest r where r.id=:id")
  Optional<ServiceRequest> lockById(@Param("id") Long id);

  long countByStatus(RequestStatus status);
}
