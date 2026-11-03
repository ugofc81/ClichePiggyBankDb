package com.example.clichepiggybankdb.controller;

import com.example.clichepiggybankdb.model.Account;
import com.example.clichepiggybankdb.model.Role;
import com.example.clichepiggybankdb.model.User;
import com.example.clichepiggybankdb.service.AccountRepository;
import com.example.clichepiggybankdb.service.RoleRepository;
import com.example.clichepiggybankdb.service.UserRepository;
import org.hibernate.annotations.ManyToAny;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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
    private UserRepository userRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private AccountRepository accountRepository;

    public static User produceUser(final String guid, final String name, final String role) {
        UUID newGuid = UUID.fromString(guid);
        List<String> roles = new ArrayList<>();
        roles.add(role);
        User user = new User();
        user.setId(newGuid);
        user.setName(name);
        user.setRoles(roles);
        return user;
    }

    @Test
    void shouldReturnAllUsers() throws Exception {
        User user = produceUser("55c1dc52-fe0d-4447-a53a-5566f60c5113", "alice", "user");
        List<User> usersList = new ArrayList<>();
        usersList.add(user);
        Mockito.when(userRepository.findAll()).thenReturn(usersList);

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(user.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(user.getName()));
    }

    @Test
    void shouldCreateUser() throws Exception {
        // define inquirer
        User inquirer = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "admin");

        // define roles of user to be created

        User user = new User();
        user.setName("alice");
        user.setRoles(new ArrayList<>(List.of("user")));

        User createdUser = new User();
        createdUser.setName("alice");
        createdUser.setRoles(new ArrayList<>(List.of("user")));
        createdUser.setId(UUID.randomUUID());

        Role userRole = new Role();
        userRole.setUserGuid(createdUser.getId());
        userRole.setRole("user");
        List userRoles = new ArrayList<>(List.of(userRole));

        Mockito.when(userRepository.existsById(inquirer.getId())).thenReturn(Boolean.TRUE);
        //define admin roles
        List<Role> adminRoles = new ArrayList<>();
        Role adminRole = new Role();
        adminRole.setUserGuid(inquirer.getId());
        adminRole.setRole("admin");
        adminRoles.add(adminRole);
        Mockito.when(roleRepository.findByUserGuid(inquirer.getId())).thenReturn(adminRoles);

        Mockito.when(userRepository.save(any())).thenReturn(createdUser);

        Mockito.when(roleRepository.save(userRole)).thenReturn(userRole);

        mockMvc.perform(post("/api/users")
                        .param("inquirerid", inquirer.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").exists())
                .andExpect(jsonPath(".name").value(user.getName()));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        // define admin
        User admin = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "admin");
        User user = produceUser("990b0301-9262-4048-a4d6-4f7ad19eeb46", "alice", "user");
        Role adminRole = new Role();
        adminRole.setRole("admin");
        adminRole.setUserGuid(admin.getId());
        Mockito.when(userRepository.existsById(admin.getId())).thenReturn(Boolean.TRUE);
        Mockito.when(roleRepository.findByUserGuid(admin.getId())).thenReturn(List.of(adminRole));

        Account account =  AccountControllerTest.produceAccount("c46b72b7-0382-4a73-b92e-be7580029e56", user.getId().toString(), 0);
        List<Account> accountList = new ArrayList<>();
        accountList.add(account);
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(accountRepository.findByOwnerGuid(user.getId())).thenReturn(accountList);

        mockMvc.perform(delete("/api/users/{id}", "990b0301-9262-4048-a4d6-4f7ad19eeb46")
                        .param("inquirerid", "c0a6d648-d769-4ebb-a0f0-7d44a0b0462e")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value("990b0301-9262-4048-a4d6-4f7ad19eeb46"))
                .andExpect(jsonPath(".name").value("alice"));
    }

    @Test
    void shouldGetUser() throws Exception {
        User user = produceUser("990b0301-9262-4048-a4d6-4f7ad19eeb46", "alice", "user");

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/{id}", user.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value(user.getId().toString()))
                .andExpect(jsonPath(".name").value(user.getName()));
    }

    @Test
    void shouldReturn404WhenUserNotFoundExceptionIsThrown() throws Exception {
        User admin = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "admin");
        User user = produceUser("990b0301-9262-4048-a4d6-4f7ad19eeb46", "alice", "user");
        List<User> userMap = new ArrayList<>();
        userMap.add(admin);
        Mockito.when(userRepository.findAll()).thenReturn(userMap);

        mockMvc.perform(get("/api/users/{id}", user.getId().toString())
                .param("inquirerid", admin.getId().toString()))
                .andExpect(status().isNotFound()); // Verifies the HTTP status is 404
    }

    @Test
    void shouldReturn404WhenNotFoundExceptionIsThrownForDeletion() throws Exception {
        User notAdmin = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "admin");
        User user = produceUser("990b0301-9262-4048-a4d6-4f7ad19eeb46", "alice", "user");
        List<User> userMap = new ArrayList<>();
        userMap.add(notAdmin);
        Mockito.when(userRepository.findAll()).thenReturn(userMap);

        mockMvc.perform(delete("/api/users/{id}", user.getId().toString())
                .param("inquirerid", notAdmin.getId().toString()))
                .andExpect(status().isNotFound()); // Verifies the HTTP status is 404
    }

    @Test
    void shouldReturn403WhenForbiddenExceptionIsThrownForAccountNotEmpty() throws Exception {
        // define admin
        User admin = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "admin");
        User user = produceUser("990b0301-9262-4048-a4d6-4f7ad19eeb46", "alice", "user");
        Role adminRole = new Role();
        adminRole.setRole("admin");
        adminRole.setUserGuid(admin.getId());
        Mockito.when(userRepository.existsById(admin.getId())).thenReturn(Boolean.TRUE);
        Mockito.when(roleRepository.findByUserGuid(admin.getId())).thenReturn(List.of(adminRole));

        Account account =  AccountControllerTest.produceAccount("c46b72b7-0382-4a73-b92e-be7580029e56", user.getId().toString(), 15);
        List<Account> accountList = new ArrayList<>();
        accountList.add(account);
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(accountRepository.findByOwnerGuid(user.getId())).thenReturn(accountList);

        mockMvc.perform(delete("/api/users/{id}", "990b0301-9262-4048-a4d6-4f7ad19eeb46")
                        .param("inquirerid", "c0a6d648-d769-4ebb-a0f0-7d44a0b0462e")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Verifies the HTTP status is 403
    }

    @Test
    void shouldReturn403WhenForbiddenExceptionIsThrownForInquirerNotAuthorized() throws Exception {
        // define admin
        User admin = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "admin");
        User user = produceUser("990b0301-9262-4048-a4d6-4f7ad19eeb46", "alice", "user");
        Role adminRole = new Role();
        adminRole.setRole("user");
        adminRole.setUserGuid(admin.getId());
        Mockito.when(userRepository.existsById(admin.getId())).thenReturn(Boolean.TRUE);
        Mockito.when(roleRepository.findByUserGuid(admin.getId())).thenReturn(List.of(adminRole));

        Account account =  AccountControllerTest.produceAccount("c46b72b7-0382-4a73-b92e-be7580029e56", user.getId().toString(), 15);
        List<Account> accountList = new ArrayList<>();
        accountList.add(account);
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(accountRepository.findByOwnerGuid(user.getId())).thenReturn(accountList);

        mockMvc.perform(delete("/api/users/{id}", "990b0301-9262-4048-a4d6-4f7ad19eeb46")
                        .param("inquirerid", "c0a6d648-d769-4ebb-a0f0-7d44a0b0462e")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Verifies the HTTP status is 403
    }

    @Test
    void shouldReturn404WhenExceptionIsThrownForCreatorNotExistent() throws Exception {
        // define inquirer
        User inquirer = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "admin");

        // define roles of user to be created

        User user = new User();
        user.setName("alice");
        user.setRoles(new ArrayList<>(List.of("user")));

        User createdUser = new User();
        createdUser.setName("alice");
        createdUser.setRoles(new ArrayList<>(List.of("user")));
        createdUser.setId(UUID.randomUUID());

        Role userRole = new Role();
        userRole.setUserGuid(createdUser.getId());
        userRole.setRole("user");
        List userRoles = new ArrayList<>(List.of(userRole));

        Mockito.when(userRepository.existsById(inquirer.getId())).thenReturn(Boolean.FALSE);
        mockMvc.perform(post("/api/users")
                        .param("inquirerid", inquirer.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound()); // Verifies the HTTP status is 403
    }

    @Test
    void shouldReturn403WhenForbiddenExceptionIsThrownForCreatorNotAuthorized() throws Exception {
        // define inquirer
        User inquirer = produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "admin");

        // define roles of user to be created

        User user = new User();
        user.setName("alice");
        user.setRoles(new ArrayList<>(List.of("user")));

        User createdUser = new User();
        createdUser.setName("alice");
        createdUser.setRoles(new ArrayList<>(List.of("user")));
        createdUser.setId(UUID.randomUUID());

        Role inquirerRole = new Role();
        inquirerRole.setUserGuid(inquirer.getId());
        inquirerRole.setRole("user");
        List<Role> inquirerRoles = new ArrayList<>();
        inquirerRoles.add(inquirerRole);

        Mockito.when(userRepository.existsById(inquirer.getId())).thenReturn(Boolean.TRUE);
        Mockito.when(roleRepository.findByUserGuid(inquirer.getId())).thenReturn(inquirerRoles);
        mockMvc.perform(post("/api/users")
                        .param("inquirerid", inquirer.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isForbidden()); // Verifies the HTTP status is 403
    }
}
