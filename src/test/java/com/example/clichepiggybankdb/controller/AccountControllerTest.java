package com.example.clichepiggybankdb.controller;

import com.example.clichepiggybankdb.model.Account;
import com.example.clichepiggybankdb.model.User;
import com.example.clichepiggybankdb.service.AccountStorageService;
import com.example.clichepiggybankdb.service.UserStorageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
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
    private UserStorageService userStorageService;

    @MockitoBean
    private AccountStorageService accountStorageService;

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
        HashMap<UUID, Account> map = new HashMap<>();
        map.put(account.getId(), account);
        Mockito.when(accountStorageService.loadAccounts()).thenReturn(map);

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..id").value(account.getId().toString()))
                .andExpect(jsonPath("$..ownerId").value(account.getOwnerGuid().toString()))
                .andExpect(jsonPath("$..balance").value(account.getBalance()));
    }

    @Test
    void shouldReturnAccountById() throws Exception {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Account account = new Account();
        account.setId(accountId);
        account.setOwnerGuid(ownerId);
        account.setBalance(100.50);

        HashMap<UUID, Account> map = new HashMap<>();
        map.put(accountId, account);

        Mockito.when(accountStorageService.loadAccounts()).thenReturn(map);

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
        Mockito.when(accountStorageService.loadAccounts()).thenReturn(new HashMap<>());

        // Act & Assert
        mockMvc.perform(get("/api/accounts/" + missingAccountId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldResetAccountWhenInquirerIsAdmin() throws Exception {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Account account = new Account();
        account.setId(accountId);
        account.setOwnerGuid(ownerId);
        account.setBalance(500.0);

        HashMap<UUID, Account> accountMap = new HashMap<>();
        accountMap.put(accountId, account);

        HashMap<UUID, User> userMap = new HashMap<>();
        // Assuming your User object can handle an admin setup or your UserController mock checks this
        List<String> roles = new ArrayList<>();
        roles.add("admin");
        User adminUser = new User();
        adminUser.setId(adminId);
        adminUser.setName("admin_user");
        adminUser.setRoles(roles);

        userMap.put(adminId, adminUser);

        Mockito.when(accountStorageService.loadAccounts()).thenReturn(accountMap);
        Mockito.when(userStorageService.loadUsers()).thenReturn(userMap);

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

        HashMap<UUID, User> userMap = new HashMap<>();
        userMap.put(adminId, adminUser);

        Mockito.when(accountStorageService.loadAccounts()).thenReturn(new HashMap<>());
        Mockito.when(userStorageService.loadUsers()).thenReturn(userMap);

        mockMvc.perform(put("/api/accounts/" + missingAccountId + "/reset")
                        .param("inquirerid", adminId.toString()))
                .andExpect(status().isNotFound());

        Mockito.verify(accountStorageService, Mockito.never()).saveAccounts(Mockito.any());
    }


    @Test
    void shouldReturn403WhenInquirerIsNotAdmin() throws Exception {
        String regularUserId = "55c1dc52-fe0d-4447-a53a-5566f60c5113";
        String accountId = "c46b72b7-0382-4a73-b92e-be7580029e56";

        HashMap<UUID, User> userMap = new HashMap<>();
        User regularUser = UserControllerTest.produceUser(regularUserId, "alice", "user");
        userMap.put(UUID.fromString(regularUserId), regularUser);

        Mockito.when(userStorageService.loadUsers()).thenReturn(userMap);

        // Act & Assert
        mockMvc.perform(put("/api/accounts/" + accountId + "/reset")
                        .param("inquirerid", regularUserId))
                .andExpect(status().isForbidden());
    }
}