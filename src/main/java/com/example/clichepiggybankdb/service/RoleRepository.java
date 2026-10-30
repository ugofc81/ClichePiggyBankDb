package com.example.clichepiggybankdb.service;

import com.example.clichepiggybankdb.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    // Inherits: save(), findAll(), findById(), deleteById() automatically!
    List<Role> findByUserGuid(UUID userId);
}