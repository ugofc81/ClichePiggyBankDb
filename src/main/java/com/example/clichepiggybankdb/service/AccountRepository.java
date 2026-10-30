package com.example.clichepiggybankdb.service;

import com.example.clichepiggybankdb.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    // Inherits: save(), findAll(), findById(), deleteById() automatically!
    List<Account> findByOwnerId(UUID ownerId);
}