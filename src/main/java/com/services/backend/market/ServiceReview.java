package com.services.backend.market;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "service_review")
public class ServiceReview {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "request_id", unique = true, nullable = false)
  ServiceRequest request;

  @Column(nullable = false)
  int rating;

  @Column(nullable = false, length = 1000)
  String comment;

  @Column(nullable = false)
  Instant createdAt;

  @Column(nullable = false, columnDefinition = "boolean default true")
  boolean visible = true;

  public int getRating() {
    return rating;
  }
}
