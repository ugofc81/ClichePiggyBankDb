package com.example.clichepiggybank.service;

import com.example.clichepiggybank.model.Sanction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SanctionRepository extends JpaRepository<Sanction, UUID> {
    // Inherits: save(), findAll(), findById(), deleteById() automatically!
}