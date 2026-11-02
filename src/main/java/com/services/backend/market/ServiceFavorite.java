package com.services.backend.market;

import com.services.backend.entities.Professional;
import com.services.backend.entities.User;
import jakarta.persistence.*;

@Entity
@Table(
  name = "service_favorite",
  uniqueConstraints = @UniqueConstraint(
    columnNames = { "customer_id", "professional_id" }
  )
)
public class ServiceFavorite {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id")
  User customer;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "professional_id")
  Professional professional;
}
