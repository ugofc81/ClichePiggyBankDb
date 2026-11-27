package com.example.clichepiggybankdb.repository;

import com.example.clichepiggybankdb.model.Sanction;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.UUID;

@Repository
public interface SanctionRepository extends JpaRepository<Sanction, UUID> {
    @Override
    Page<Sanction> findAll(@NonNull Pageable pageable);
    // Inherits: save(), findAll(), findById(), deleteById() automatically!
}