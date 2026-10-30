package com.example.clichepiggybankdb.controller;

import com.example.clichepiggybankdb.model.Account;
import com.example.clichepiggybankdb.model.User;
import com.example.clichepiggybankdb.service.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AccountRepository accountRepository;

    public static Account produceAccount(final String accountGuidString, final String ownerGuidString, final double balance) {
        UUID accountGuid = UUID.fromString(accountGuidString);
        UUID ownerGuid = UUID.fromString(ownerGuidString);
        Account toBeReturned = new Account();

        toBeReturned.setId(accountGuid);
        toBeReturned.setOwnerGuid(ownerGuid);
        toBeReturned.setBalance(balance);
        return toBeReturned;
    }

    @Test
    void shouldReturnAllAccounts() throws Exception {
        Account account = produceAccount("c46b72b7-0382-4a73-b92e-be7580029e56", "55c1dc52-fe0d-4447-a53a-5566f60c5113", 5);
        List<Account> accountList = new ArrayList<>();
        accountList.add(account);
        Mockito.when(accountRepository.findAll()).thenReturn(accountList);

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..id").value(account.getId().toString()))
                .andExpect(jsonPath("$..ownerGuid").value(account.getOwnerGuid().toString()))
                .andExpect(jsonPath("$..balance").value(account.getBalance()));
    }

    @Test
    void shouldReturnAccountById() throws Exception {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID ownerGuid = UUID.randomUUID();
        Account account = new Account();
        account.setId(accountId);
        account.setOwnerGuid(ownerGuid);
        account.setBalance(100.50);

        List<Account> accountList = new ArrayList<>();
        accountList.add(account);

        Mockito.when(accountRepository.findById(accountId).get()).thenReturn(account);

        // Act & Assert
        mockMvc.perform(get("/api/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.balance").value(100.50));
    }

    @Test
    void shouldReturn404WhenAccountDoesNotExist() throws Exception {
        // Arrange
        UUID missingAccountId = UUID.randomUUID();
        Mockito.when(accountRepository.findById(missingAccountId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/accounts/" + missingAccountId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldResetAccountWhenInquirerIsAdmin() throws Exception {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID ownerGuid = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Account account = new Account();
        account.setId(accountId);
        account.setOwnerGuid(ownerGuid);
        account.setBalance(500.0);

        List<Account> accountList = new ArrayList<>();
        accountList.add(account);

        List<User> userList = new ArrayList<>();
        // Assuming your User object can handle an admin setup or your UserController mock checks this
        List<String> roles = new ArrayList<>();
        roles.add("admin");
        User adminUser = new User();
        adminUser.setId(adminId);
        adminUser.setName("admin_user");
        adminUser.setRoles(roles);

        userList.add(adminUser);

        Mockito.when(accountRepository.findById(accountId).get()).thenReturn(account);
        Mockito.when(userRepository.findById(adminId).get()).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(put("/api/accounts/"+ accountId + "/reset")
                        .param("inquirerid", adminId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void shouldReturn404WhenResettingAccountThatDoesNotExist() throws Exception {
        UUID missingAccountId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        List<String> roles = new ArrayList<>();
        roles.add("admin");
        User adminUser = new User();
        adminUser.setId(adminId);
        adminUser.setName("admin_user");
        adminUser.setRoles(roles);

        List<User> userList = new ArrayList<>();
        userList.add(adminUser);

        Mockito.when(accountRepository.findById(missingAccountId).get()).thenReturn(null);
        Mockito.when(userRepository.findById(adminId).get()).thenReturn(adminUser);

        mockMvc.perform(put("/api/accounts/" + missingAccountId + "/reset")
                        .param("inquirerid", adminId.toString()))
                .andExpect(status().isNotFound());

        Mockito.verify(accountRepository, Mockito.never()).save(Mockito.any());
    }


    @Test
    void shouldReturn403WhenInquirerIsNotAdmin() throws Exception {
        String regularUserId = "55c1dc52-fe0d-4447-a53a-5566f60c5113";
        String accountId = "c46b72b7-0382-4a73-b92e-be7580029e56";
        Account account = new Account();
        account.setId(UUID.fromString(accountId));
        account.setOwnerGuid(UUID.randomUUID());
        account.setBalance(15);

        List<User> userList = new ArrayList<>();
        User regularUser = UserControllerTest.produceUser(regularUserId, "alice", "user");
        userList.add(regularUser);

        Mockito.when(accountRepository.findById(UUID.fromString(accountId)).get()).thenReturn(account);
        Mockito.when(userRepository.findById(UUID.fromString(regularUserId)).get()).thenReturn(regularUser);

        // Act & Assert
        mockMvc.perform(put("/api/accounts/" + accountId + "/reset")
                        .param("inquirerid", regularUserId))
                .andExpect(status().isForbidden());
    }
}