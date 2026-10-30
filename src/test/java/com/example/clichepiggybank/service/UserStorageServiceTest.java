package com.example.clichepiggybank.service;

import com.example.clichepiggybank.model.User;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class UserStorageServiceTest {
    private UserStorageService storageService;

    private final String TEST_FILE = "users-test.json";
    private final String TEST_TEMP_FILE = "users-test.json.tmp";

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        storageService = new UserStorageService(objectMapper);

        ReflectionTestUtils.setField(storageService, "filePath", TEST_FILE);
        ReflectionTestUtils.setField(storageService, "tempFilePath", TEST_TEMP_FILE);

        cleanUpTestFiles();
    }

    @AfterEach
    void tearDown() {
        cleanUpTestFiles();
    }

    @Test
    void shouldReturnEmptyListWhenFileDoesNotExist() {
        HashMap<UUID, User> users = storageService.loadUsers();

        assertThat(users).isNotNull().isEmpty();
    }

    private void cleanUpTestFiles() {
        new File(TEST_FILE).delete();
        new File(TEST_TEMP_FILE).delete();
    }

    @Test
    void shouldSaveAndLoadUsersSuccessfully() {
        HashMap<UUID, User> testUsers = new HashMap<>();
        UUID newGuid = UUID.fromString("55c1dc52-fe0d-4447-a53a-5566f60c5113");
        String[] roles = {"roles"};
        User newUser = new User(newGuid, "test_user", roles);
        testUsers.put(newGuid, newUser);

        storageService.saveUsers(testUsers);
        HashMap<UUID, User> loadedUsers = storageService.loadUsers();

        assertThat(loadedUsers).hasSize(1);
        assertThat(loadedUsers.get(newGuid).getName()).isEqualTo("test_user");
        assertThat(loadedUsers.get(newGuid).getId()).isEqualTo(newGuid);
    }

    @Test
    void shouldAtomicSwapDeleteOldFileLayout() {
        HashMap<UUID, User> testUsers = new HashMap<>();
        UUID newGuid = UUID.fromString("55c1dc52-fe0d-4447-a53a-5566f60c5113");
        String[] roles = {"user"};
        testUsers.put(newGuid, new User(newGuid, "delete_check", roles));

        storageService.saveUsers(testUsers);

        File mainFile = new File(TEST_FILE);
        File tempFile = new File(TEST_TEMP_FILE);

        assertThat(mainFile).exists(); // The final file must exist
        assertThat(tempFile).doesNotExist(); // The temporary file must have been swapped and cleaned up
    }
}
