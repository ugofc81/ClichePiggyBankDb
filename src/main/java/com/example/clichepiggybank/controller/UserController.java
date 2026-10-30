package com.example.clichepiggybank.controller;

import com.example.clichepiggybank.controller.exceptions.AccountNotEmptyException;
import com.example.clichepiggybank.controller.exceptions.ForbiddenException;
import com.example.clichepiggybank.controller.exceptions.UserNotFoundException;
import com.example.clichepiggybank.model.Account;
import com.example.clichepiggybank.model.Role;
import com.example.clichepiggybank.model.User;
import com.example.clichepiggybank.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    private final UserRepository userRepository;
    private final RoleRepository rolesRepository;
    private final AccountRepository accountRepository;

    // Inject the Repository bean directly
    public UserController(UserRepository userRepository, RoleRepository rolesRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.rolesRepository = rolesRepository;
        this.accountRepository = accountRepository;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());// SQL: SELECT * FROM users;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User newUser, @RequestParam("inquirerid") UUID inquirerId) {
        if (userRepository.existsById(inquirerId)) {
            userRepository.findById(inquirerId);
            if (!isAdmin(rolesRepository.findByUserGuid(inquirerId))) {
                throw new ForbiddenException(inquirerId);
            }
        } else {
            throw new UserNotFoundException(inquirerId);
        }

        User u = userRepository.save(newUser);

        newUser.getRoles().forEach(role -> {
            Role newRole = new Role();
            newRole.setRole(role);
            newRole.setUserGuid(u.getId());
            rolesRepository.save(newRole);
        });

        return ResponseEntity.ok(newUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable UUID id, @RequestParam("inquirerid") UUID inquirerId) {
        if (userRepository.existsById(inquirerId)) {
            userRepository.findById(inquirerId);
            if (!isAdmin(rolesRepository.findByUserGuid(inquirerId))) {
                throw new ForbiddenException(inquirerId);
            }
        } else {
            throw new UserNotFoundException(inquirerId);
        }

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        Account toBeVerified = accountRepository.findByOwnerId(id).getFirst();
        if (toBeVerified != null) {
            if (toBeVerified.getBalance() > 0) {
                throw new AccountNotEmptyException(toBeVerified.getId());
            } else {
                accountRepository.deleteById(toBeVerified.getId());
            }
        }
        if (userRepository.findById(id).isEmpty()){
            throw new UserNotFoundException(id);
        };
        List<Role> rolesToBeDeleted = rolesRepository.findByUserGuid(id);
        User toBeDeleted = userRepository.findById(id).get();
        userRepository.deleteById(id);
        return ResponseEntity.ok(toBeDeleted);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        ;
        if (userRepository.findById(id).isEmpty()) {
            throw new UserNotFoundException(id);
        }
        return ResponseEntity.ok(userRepository.findById(id).get());
    }

    public static boolean isAdmin(List<Role> roles) {
        return roles.stream().anyMatch(role -> role.getRole().equals("admin"));
    }

    public static boolean isUser(List<Role> roles) {
        return roles.stream().anyMatch(role -> role.getRole().equals("user"));

    }
}
