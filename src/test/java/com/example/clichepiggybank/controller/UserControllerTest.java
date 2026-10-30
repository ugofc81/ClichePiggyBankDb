package com.example.clichepiggybank.controller;

import com.example.clichepiggybank.model.Account;
import com.example.clichepiggybank.model.User;
import com.example.clichepiggybank.service.AccountStorageService;
import com.example.clichepiggybank.service.UserStorageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserStorageService userStorageService;

    @MockitoBean
    private AccountStorageService accountStorageService;

    public static User produceUser(final String guid, final String name, final String role) {
        UUID newGuid = UUID.fromString(guid);
        String[] roles = {role};
        return new User(newGuid, name, roles);
    }

    @Test
    void shouldReturnAllUsers() throws Exception {
        User user = produceUser("55c1dc52-fe0d-4447-a53a-5566f60c5113", "alice", "user");
        HashMap<UUID, User> map = new HashMap<>();
        map.put(user.getId(), user);
        Mockito.when(userStorageService.loadUsers()).thenReturn(map);

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..id").value(user.getId().toString()))
                .andExpect(jsonPath("$..name").value(user.getName()))
                .andExpect(jsonPath("$..roles[0]").value("user"));
    }

    @Test
    void shouldCreateUser() throws Exception {
        User admin = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "admin");
        String[] userRoles = {"user"};
        User user = new User(null, "alice", userRoles);
        HashMap<UUID, User> map = new HashMap<>();
        map.put(admin.getId(), admin);
        Mockito.when(userStorageService.loadUsers()).thenReturn(map);

        mockMvc.perform(post("/api/users")
                        .param("inquirerid", admin.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").exists())
                .andExpect(jsonPath(".name").value(user.getName()));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        User admin = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "admin");
        User user = produceUser("990b0301-9262-4048-a4d6-4f7ad19eeb46", "alice", "user");
        HashMap<UUID, User> userMap = new HashMap<>();
        userMap.put(admin.getId(), admin);
        userMap.put(user.getId(), user);
        Account account =  AccountControllerTest.produceAccount("c46b72b7-0382-4a73-b92e-be7580029e56", user.getId().toString(), 0);
        HashMap<UUID, Account> accountMap = new HashMap<>();
        accountMap.put(account.getId(), account);
        Mockito.when(userStorageService.loadUsers()).thenReturn(userMap);
        Mockito.when(accountStorageService.loadAccounts()).thenReturn(accountMap);

        mockMvc.perform(delete("/api/users/{id}", "990b0301-9262-4048-a4d6-4f7ad19eeb46")
                        .param("inquirerid", "c0a6d648-d769-4ebb-a0f0-7d44a0b0462e")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value("990b0301-9262-4048-a4d6-4f7ad19eeb46"))
                .andExpect(jsonPath(".name").value("alice"));
    }

    @Test
    void shouldGetUser() throws Exception {
        User admin = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "admin");
        User user = produceUser("990b0301-9262-4048-a4d6-4f7ad19eeb46", "alice", "user");
        HashMap<UUID, User> userMap = new HashMap<>();
        userMap.put(admin.getId(), admin);
        userMap.put(user.getId(), user);
        Mockito.when(userStorageService.loadUsers()).thenReturn(userMap);

        mockMvc.perform(get("/api/users/{id}", user.getId().toString())
                        .param("inquirerid", admin.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value(user.getId().toString()))
                .andExpect(jsonPath(".name").value(user.getName()));
    }

    @Test
    void shouldReturn404WhenUserNotFoundExceptionIsThrown() throws Exception {
        User admin = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "admin");
        User user = produceUser("990b0301-9262-4048-a4d6-4f7ad19eeb46", "alice", "user");
        HashMap<UUID, User> userMap = new HashMap<>();
        userMap.put(admin.getId(), admin);
        Mockito.when(userStorageService.loadUsers()).thenReturn(userMap);

        mockMvc.perform(get("/api/users/{id}", user.getId().toString())
                .param("inquirerid", admin.getId().toString()))
                .andExpect(status().isNotFound()); // Verifies the HTTP status is 404
    }

    @Test
    void shouldReturn403WhenForbiddenExceptionIsThrownForDeletion() throws Exception {
        User notAdmin = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "user");
        User user = produceUser("990b0301-9262-4048-a4d6-4f7ad19eeb46", "alice", "user");
        HashMap<UUID, User> userMap = new HashMap<>();
        userMap.put(notAdmin.getId(), notAdmin);
        userMap.put(user.getId(), user);
        Mockito.when(userStorageService.loadUsers()).thenReturn(userMap);

        mockMvc.perform(delete("/api/users/{id}", user.getId().toString())
                .param("inquirerid", notAdmin.getId().toString()))
                .andExpect(status().isForbidden()); // Verifies the HTTP status is 403
    }

    @Test
    void shouldReturn404WhenNotFoundExceptionIsThrownForDeletion() throws Exception {
        User notAdmin = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "admin");
        User user = produceUser("990b0301-9262-4048-a4d6-4f7ad19eeb46", "alice", "user");
        HashMap<UUID, User> userMap = new HashMap<>();
        userMap.put(notAdmin.getId(), notAdmin);
        Mockito.when(userStorageService.loadUsers()).thenReturn(userMap);

        mockMvc.perform(delete("/api/users/{id}", user.getId().toString())
                .param("inquirerid", notAdmin.getId().toString()))
                .andExpect(status().isNotFound()); // Verifies the HTTP status is 404
    }

    @Test
    void shouldReturn403WhenForbiddenExceptionIsThrownForAccountNotEmpty() throws Exception {
        User notAdmin = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "admin");
        User user = produceUser("990b0301-9262-4048-a4d6-4f7ad19eeb46", "alice", "user");
        HashMap<UUID, User> userMap = new HashMap<>();
        userMap.put(notAdmin.getId(), notAdmin);
        userMap.put(user.getId(), user);
        Mockito.when(userStorageService.loadUsers()).thenReturn(userMap);
        Account account = AccountControllerTest.produceAccount("c46b72b7-0382-4a73-b92e-be7580029e56", user.getId().toString(), 5);
        HashMap<UUID, Account> accountMap = new HashMap<>();
        accountMap.put(account.getId(), account);
        Mockito.when(accountStorageService.loadAccounts()).thenReturn(accountMap);

        mockMvc.perform(delete("/api/users/{id}", user.getId().toString())
                .param("inquirerid", notAdmin.getId().toString()))
                .andExpect(status().isForbidden()); // Verifies the HTTP status is 403
    }

    @Test
    void shouldReturn403WhenForbiddenExceptionIsThrownForCreatorWithNoRights() throws Exception {
        User admin = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "user");
        String[] userRoles = {"user"};
        User user = new User(null, "alice", userRoles);
        HashMap<UUID, User> map = new HashMap<>();
        map.put(admin.getId(), admin);
        Mockito.when(userStorageService.loadUsers()).thenReturn(map);

        mockMvc.perform(post("/api/users")
                        .param("inquirerid", admin.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isForbidden()); // Verifies the HTTP status is 403
    }

    @Test
    void shouldReturn403WhenForbiddenExceptionIsThrownForCreatorNotExistent() throws Exception {
        User admin = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "user");
        String[] userRoles = {"user"};
        User user = new User(null, "alice", userRoles);
        HashMap<UUID, User> map = new HashMap<>();
        Mockito.when(userStorageService.loadUsers()).thenReturn(map);

        mockMvc.perform(post("/api/users")
                        .param("inquirerid", admin.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isForbidden()); // Verifies the HTTP status is 403
    }
}
