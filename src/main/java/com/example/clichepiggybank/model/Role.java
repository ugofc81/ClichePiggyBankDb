package com.example.clichepiggybank.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "roles") // Maps this class to a "users" table in MySQL
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Automatically generates clean UUIDs!
    private UUID id;

    @Column(nullable = false, unique = false, name = "user_guid") // Database constraint: cannot be empty or duplicate
    private UUID userGuid;

    @Column(nullable = false, unique = false, name = "role") //
    private String role;

    public Role() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserGuid() {
        return userGuid;
    }

    public void setUserGuid(UUID userGuid) {
        this.userGuid = userGuid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
