package com.example.clichepiggybankdb.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "accounts") // Maps this class to a "users" table in MySQL
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Automatically generates clean UUIDs!
    @Column(name = "id", length = 16)
    @JdbcTypeCode(SqlTypes.BINARY) // Forces Hibernate to use MySQL's BINARY(16)
    private UUID id;

    @Column(nullable = false, unique = true, name = "owner_id")
    private UUID ownerGuid;

    @Column(nullable = true, unique = false, name = "balance")
    private double balance;

    public Account() {
        this.balance = 0;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwnerGuid() {
        return ownerGuid;
    }

    public void setOwnerGuid(UUID ownerGuid) {
        this.ownerGuid = ownerGuid;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
