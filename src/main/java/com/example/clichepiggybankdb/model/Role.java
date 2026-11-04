package com.example.clichepiggybankdb.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "roles") // Maps this class to a "users" table in MySQL
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Automatically generates clean UUIDs!
    @Column(name = "id", length = 16)
    @JdbcTypeCode(SqlTypes.BINARY) // Forces Hibernate to use MySQL's BINARY(16)
    private UUID id;

    @Column(nullable = false, unique = false, name = "user_guid", length = 16) // Database constraint: cannot be empty or duplicate
    @JdbcTypeCode(SqlTypes.BINARY) // Forces Hibernate to use MySQL's BINARY(16)
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
