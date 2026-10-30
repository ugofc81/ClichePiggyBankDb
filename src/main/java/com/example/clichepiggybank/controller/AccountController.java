package com.example.clichepiggybank.controller;

import com.example.clichepiggybank.controller.exceptions.AccountNotFoundException;
import com.example.clichepiggybank.controller.exceptions.ForbiddenException;
import com.example.clichepiggybank.controller.exceptions.UserNotFoundException;
import com.example.clichepiggybank.model.Account;
import com.example.clichepiggybank.model.User;
import com.example.clichepiggybank.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "http://localhost:5173")
public class AccountController {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AccountController(
            AccountRepository accountRepository,
            UserRepository userRepository,
            RoleRepository roleRepository
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(new ArrayList<>(accountRepository.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable UUID id) {
        if(accountRepository.findById(id).isEmpty()) {
            throw new AccountNotFoundException(id);
        }
        return ResponseEntity.ok(accountRepository.findById(id).get());
    }

    public ResponseEntity<Account> updateAccount(@PathVariable UUID id, @RequestBody Account updatedAccount) {
        if(accountRepository.findById(id).isEmpty()) {
            throw new AccountNotFoundException(id);
        }
        return ResponseEntity.ok(accountRepository.save(updatedAccount));
    }

    public ResponseEntity<Account> deleteAccount(@PathVariable UUID id) {
        if(accountRepository.findById(id).isEmpty()) {
            throw new AccountNotFoundException(id);
        }
        Account removed = accountRepository.findById(id).get();
        accountRepository.deleteById(id);
        return ResponseEntity.ok(removed);
    }

    @PutMapping("/{id}/reset")
    public ResponseEntity<Account> resetAccount(@PathVariable UUID id, @RequestParam("inquirerid") UUID inquirerId) {
        if(accountRepository.findById(id).isEmpty()) {
            throw new AccountNotFoundException(id);
        }
        if(userRepository.findById(inquirerId).isEmpty()) {
            throw new UserNotFoundException(id);
        }
        if (!UserController.isAdmin(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        Account current = accountRepository.findById(id).get();
        current.setBalance(0);
        return ResponseEntity.ok(accountRepository.save(current));
    }
}
