package com.matching.cloth.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Admin {

    @Id
	@Column(name = "adminUsername", nullable = false, unique = true, length = 50)
    private String adminUsername;
    
	@Column(name = "adminPassword", nullable = false, length = 255)
    private String adminPassword;

	public Admin() {
		super();
	}

	public Admin(String adminUsername, String adminPassword) {
		this.adminUsername = adminUsername;
		this.adminPassword = adminPassword;
	}

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}