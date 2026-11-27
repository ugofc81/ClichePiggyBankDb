package com.example.clichepiggybankdb.controller;

import com.example.clichepiggybankdb.model.User;
import com.example.clichepiggybankdb.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class UserController {
    private final UserService userService;

    // Inject the Repository bean directly
    public UserController(
            UserService userService
    ) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAll());// SQL: SELECT * FROM users;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User newUser, @RequestParam("inquirerid") UUID inquirerId) {
        return ResponseEntity.ok(userService.create(newUser, inquirerId));
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User newUser) {
        return ResponseEntity.ok(userService.register(newUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable UUID id, @RequestParam("inquirerid") UUID inquirerId) {
        return ResponseEntity.ok(userService.delete(id, inquirerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.get(id));
    }
}
