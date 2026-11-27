package com.example.clichepiggybankdb.controller;

import com.example.clichepiggybankdb.model.Account;
import com.example.clichepiggybankdb.repository.*;
import com.example.clichepiggybankdb.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class AccountController {
    private final AccountService accountService;

    public AccountController(
            AccountService accountService
    ) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(new ArrayList<>(accountService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.get(id));
    }

    @PutMapping("/{id}/reset")
    public ResponseEntity<Account> resetAccount(@PathVariable UUID id, @RequestParam("inquirerid") UUID inquirerId) {
        return ResponseEntity.ok(accountService.reset(id, inquirerId));
    }
}
