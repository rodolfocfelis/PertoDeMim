package com.services.backend.repositories;

import com.services.backend.entities.User;
import com.services.backend.entities.enums.UserRole;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    UserDetails findByEmail(String email);

    @Query("""
           select u from User u
           where (:name is null or lower(u.name) like lower(concat('%', :name, '%'))
                  or lower(u.email) like lower(concat('%', :name, '%')))
             and (:role is null or u.role = :role)
             and (:active is null or u.active = :active)
           """)
    Page<User> search(@Param("name") String name, @Param("role") UserRole role,
                      @Param("active") Boolean active, Pageable pageable);
}