package com.services.backend.market;

import com.services.backend.entities.Professional;
import com.services.backend.entities.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
  name = "service_request",
  uniqueConstraints = @UniqueConstraint(
    columnNames = { "customer_id", "request_key" }
  )
)
public class ServiceRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id")
  User customer;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "professional_id")
  Professional professional;

  @Column(nullable = false, length = 2000)
  String description;

  LocalDate preferredDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  RequestStatus status;

  @Column(nullable = false)
  Instant createdAt;

  @Column(nullable = false)
  Instant updatedAt;

  @Column(name = "request_key", nullable = false, length = 36)
  String requestKey;

  @OneToOne(mappedBy = "request", fetch = FetchType.LAZY)
  ServiceReview review;

  public User getCustomer() {
    return customer;
  }

  public Professional getProfessional() {
    return professional;
  }
}
