package com.example.clichepiggybankdb.controller;

import com.example.clichepiggybankdb.controller.exceptions.AccountNotFoundException;
import com.example.clichepiggybankdb.controller.exceptions.ForbiddenException;
import com.example.clichepiggybankdb.controller.exceptions.UserNotFoundException;
import com.example.clichepiggybankdb.model.Account;
import com.example.clichepiggybankdb.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
        return ResponseEntity.ok(accountRepository
                .findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id)));
    }

//    public ResponseEntity<Account> updateAccount(@PathVariable UUID id, @RequestBody Account updatedAccount) {
//        if(!accountRepository.existsById(id)) {
//            throw new AccountNotFoundException(id);
//        }
//        return ResponseEntity.ok(accountRepository.save(updatedAccount));
//    }
//
//    public ResponseEntity<Account> deleteAccount(@PathVariable UUID id) {
//        Account removed = accountRepository
//                .findById(id)
//                .orElseThrow(() -> new AccountNotFoundException(id));
//        accountRepository.deleteById(id);
//        return ResponseEntity.ok(removed);
//    }

    @PutMapping("/{id}/reset")
    public ResponseEntity<Account> resetAccount(@PathVariable UUID id, @RequestParam("inquirerid") UUID inquirerId) {
        if(!userRepository.existsById(inquirerId)) {
            throw new UserNotFoundException(id);
        }
        if (!UserController.isAdmin(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        Account current = accountRepository
                .findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        current.setBalance(0);
        return ResponseEntity.ok(accountRepository.save(current));

    }
}
