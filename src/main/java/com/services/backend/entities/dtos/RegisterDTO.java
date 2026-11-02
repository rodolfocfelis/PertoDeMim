package com.services.backend.entities.dtos;

import com.services.backend.entities.enums.UserRole;

public class RegisterDTO {
	private Long id;
	private String name;
	private String email;
	private String password;
	private UserRole role;

	public RegisterDTO() {
	}

	public RegisterDTO(Long id, String name, String email, UserRole role, String password) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.setPassword(password);
		this.role = role;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

    private String bio;
    public String getBio() { return bio; }
    public void setBio(String value) { this.bio = value; }
}
