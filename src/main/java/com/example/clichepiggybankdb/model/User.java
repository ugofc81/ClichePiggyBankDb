package com.example.clichepiggybankdb.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users") // Maps this class to a "users" table in MySQL
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Automatically generates clean UUIDs!
    @Column(name = "id", length = 16)
    @JdbcTypeCode(SqlTypes.BINARY) // Forces Hibernate to use MySQL's BINARY(16)
    private UUID id;

    @Column(nullable = false, unique = false, name = "name") //
    private String name;

    private List<String> roles;

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public User() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
