package com.example.clichepiggybankdb.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "accounts") // Maps this class to a "users" table in MySQL
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Automatically generates clean UUIDs!
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
