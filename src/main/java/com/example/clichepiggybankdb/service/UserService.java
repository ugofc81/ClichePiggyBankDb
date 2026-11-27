package com.example.clichepiggybankdb.service;

import com.example.clichepiggybankdb.controller.exceptions.*;
import com.example.clichepiggybankdb.model.Account;
import com.example.clichepiggybankdb.model.Role;
import com.example.clichepiggybankdb.model.User;
import com.example.clichepiggybankdb.repository.AccountRepository;
import com.example.clichepiggybankdb.repository.RoleRepository;
import com.example.clichepiggybankdb.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository rolesRepository;
    private final AccountRepository accountRepository;

    // Inject the Repository bean directly
    public UserService(
            UserRepository userRepository,
            RoleRepository rolesRepository,
            AccountRepository accountRepository
    ) {
        this.userRepository = userRepository;
        this.rolesRepository = rolesRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public User get(UUID userId) throws UserNotFoundException{
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User create(User newUser, UUID inquirerId) throws InquirerNotFoundException, ForbiddenException {
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

        return newUser;
    }

    @Transactional
    public User delete(UUID userId, UUID inquirerId) throws InquirerNotFoundException, ForbiddenException {
        if (!userRepository.existsById(inquirerId)) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!isAdmin(rolesRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        if (userId.equals(inquirerId)) {
            throw new ForbiddenException(inquirerId);
        }
        User toBeDeleted = userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<Account> toBeVerified = accountRepository.findByOwnerGuid(userId);
        if (!toBeVerified.isEmpty()) {
            if (toBeVerified.get(0).getBalance() > 0) {
                throw new AccountNotEmptyException(toBeVerified.get(0).getId());
            } else {
                accountRepository.deleteById(toBeVerified.get(0).getId());
            }
        }
        List<Role> rolesToBeDeleted = rolesRepository.findByUserGuid(userId);
        rolesToBeDeleted.forEach(rolesRepository::delete);
        userRepository.deleteById(userId);
        return toBeDeleted;
    }

    @Transactional
    public User register(User newUser) throws InquirerNotFoundException, ForbiddenException {
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

        return newUser;
    }

    public static boolean isAdmin(List<Role> roles) {
        return roles.stream().anyMatch(role -> role.getRole().equals("admin"));
    }

    public static boolean isUser(List<Role> roles) {
        return roles.stream().anyMatch(role -> role.getRole().equals("user"));
    }
}
