package com.services.backend.entities;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.services.backend.entities.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    public User() {}

    public User(Long id, String name, String email, String password, UserRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() { return email; }

    @JsonIgnore
    @Override
    public String getPassword() { return password; }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return active; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override public int hashCode() { return Objects.hash(id); }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User other = (User) obj;
        return Objects.equals(id, other.id);
    }

    private String phone;
    public String getPhone() { return phone; }
    public void setPhone(String value) { this.phone = value; }

    private String zipCode;
    public String getZipCode() { return zipCode; }
    public void setZipCode(String value) { this.zipCode = value; }

    private String street;
    public String getStreet() { return street; }
    public void setStreet(String value) { this.street = value; }

    private String addressNumber;
    public String getAddressNumber() { return addressNumber; }
    public void setAddressNumber(String value) { this.addressNumber = value; }

    private String complement;
    public String getComplement() { return complement; }
    public void setComplement(String value) { this.complement = value; }

    private String neighborhood;
    public String getNeighborhood() { return neighborhood; }
    public void setNeighborhood(String value) { this.neighborhood = value; }

    private String city;
    public String getCity() { return city; }
    public void setCity(String value) { this.city = value; }

    private String state;
    public String getState() { return state; }
    public void setState(String value) { this.state = value; }

    @Column(length = 2000)
    private String bio;
    public String getBio() { return bio; }
    public void setBio(String value) { this.bio = value; }

    private Double latitude;
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double value) { this.latitude = value; }

    private Double longitude;
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double value) { this.longitude = value; }
}
