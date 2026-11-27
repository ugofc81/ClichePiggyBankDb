package com.example.clichepiggybankdb.service;

import com.example.clichepiggybankdb.controller.exceptions.AccountNotFoundException;
import com.example.clichepiggybankdb.controller.exceptions.ForbiddenException;
import com.example.clichepiggybankdb.controller.exceptions.InquirerNotFoundException;
import com.example.clichepiggybankdb.controller.exceptions.UserNotFoundException;
import com.example.clichepiggybankdb.model.Account;
import com.example.clichepiggybankdb.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AccountService(
            AccountRepository accountRepository,
            UserRepository userRepository,
            RoleRepository roleRepository
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public List<Account> getAll() throws InquirerNotFoundException, ForbiddenException {
        return accountRepository.findAll();
    }

    @Transactional
    public Account get(UUID accountId) throws AccountNotFoundException {
        return accountRepository
                .findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
    @Transactional
    public Account reset(UUID accountId, UUID inquirerId) throws UserNotFoundException, ForbiddenException, AccountNotFoundException {
        if(!userRepository.existsById(inquirerId)) {
        throw new UserNotFoundException(accountId);
    }
        if (!UserService.isAdmin(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        Account current = accountRepository
                .findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        current.setBalance(0);
        return accountRepository.save(current);
    }

//    public Account updateAccount(UUID id, Account updatedAccount) {
//        if(!accountRepository.existsById(id)) {
//            throw new AccountNotFoundException(id);
//        }
//        return accountRepository.save(updatedAccount);
//    }
//
//    public Account deleteAccount(UUID id) {
//        Account removed = accountRepository
//                .findById(id)
//                .orElseThrow(() -> new AccountNotFoundException(id));
//        accountRepository.deleteById(id);
//        return removed;
//    }


}
