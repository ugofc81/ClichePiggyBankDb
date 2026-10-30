package com.example.clichepiggybankdb.service;

import com.example.clichepiggybankdb.model.Sanction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SanctionRepository extends JpaRepository<Sanction, UUID> {
    // Inherits: save(), findAll(), findById(), deleteById() automatically!
}