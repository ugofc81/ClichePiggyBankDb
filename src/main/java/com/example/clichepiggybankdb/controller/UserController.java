package com.example.clichepiggybankdb.controller;

import com.example.clichepiggybankdb.controller.exceptions.*;
import com.example.clichepiggybankdb.model.Account;
import com.example.clichepiggybankdb.model.Role;
import com.example.clichepiggybankdb.model.User;
import com.example.clichepiggybankdb.service.*;
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
        if (!userRepository.existsById(inquirerId)) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!isAdmin(rolesRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
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

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User newUser) {
        List<User> users = userRepository.findAll();
        if (users.size() > 0) {
            throw new GoneException("first registering");
        }

        //the first user is user and admin.
        List<String> roles = new ArrayList<>();
        roles.add("user");
        roles.add("admin");
        newUser.setRoles(roles);
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
        if (!userRepository.existsById(inquirerId)) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!isAdmin(rolesRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        if (id.equals(inquirerId)) {
            throw new ForbiddenException(inquirerId);
        }
        User toBeDeleted = userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        List<Account> toBeVerified = accountRepository.findByOwnerGuid(id);
        if (!toBeVerified.isEmpty()) {
            if (toBeVerified.get(0).getBalance() > 0) {
                throw new AccountNotEmptyException(toBeVerified.get(0).getId());
            } else {
                accountRepository.deleteById(toBeVerified.get(0).getId());
            }
        }
        List<Role> rolesToBeDeleted = rolesRepository.findByUserGuid(id);
        rolesToBeDeleted.forEach(rolesRepository::delete);
        userRepository.deleteById(id);
        return ResponseEntity.ok(toBeDeleted);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
    }

    public static boolean isAdmin(List<Role> roles) {
        return roles.stream().anyMatch(role -> role.getRole().equals("admin"));
    }

    public static boolean isUser(List<Role> roles) {
        return roles.stream().anyMatch(role -> role.getRole().equals("user"));
    }
}
