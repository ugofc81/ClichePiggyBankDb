package com.example.clichepiggybankdb.service;

import com.example.clichepiggybankdb.model.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountStorageServiceTest {
    private AccountStorageService storageService;

    private final String TEST_FILE = "accounts-test.json";
    private final String TEST_TEMP_FILE = "accounts-test.json.tmp";

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        storageService = new AccountStorageService(objectMapper);

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
        HashMap<UUID, Account> accounts = storageService.loadAccounts();

        assertThat(accounts).isNotNull().isEmpty();
    }

    private void cleanUpTestFiles() {
        new File(TEST_FILE).delete();
        new File(TEST_TEMP_FILE).delete();
    }

    @Test
    void shouldSaveAndLoadAccountsSuccessfully() {
        HashMap<UUID, Account> testAccounts = new HashMap<>();
        UUID newAccountGuid = UUID.fromString("55c1dc52-fe0d-4447-a53a-5566f60c5113");
        UUID newUserGuid = UUID.fromString("be4523cf-6cba-4747-8989-ce8e21470e34");

        Account newAccount = new Account();
        newAccount.setId(newAccountGuid);
        newAccount.setOwnerGuid(newUserGuid);
        newAccount.setBalance(0);
        testAccounts.put(newAccountGuid, newAccount);

        storageService.saveAccounts(testAccounts);
        HashMap<UUID, Account> loadedAccounts = storageService.loadAccounts();

        assertThat(loadedAccounts).hasSize(1);
        assertThat(loadedAccounts.get(newAccountGuid).getId()).isEqualTo(newAccountGuid);
        assertThat(loadedAccounts.get(newAccountGuid).getOwnerGuid()).isEqualTo(newUserGuid);
        assertThat(loadedAccounts.get(newAccountGuid).getBalance()).isEqualTo(0);
    }

    @Test
    void shouldAtomicSwapDeleteOldFileLayout() {
        HashMap<UUID, Account> testAccounts = new HashMap<>();
        UUID newAccountGuid = UUID.fromString("55c1dc52-fe0d-4447-a53a-5566f60c5113");
        UUID newUserGuid = UUID.fromString("be4523cf-6cba-4747-8989-ce8e21470e34");
        Account newAccount = new Account();
        newAccount.setId(newAccountGuid);
        newAccount.setOwnerGuid(newUserGuid);
        newAccount.setBalance(0);
        testAccounts.put(newAccount);

        storageService.saveAccounts(testAccounts);

        File mainFile = new File(TEST_FILE);
        File tempFile = new File(TEST_TEMP_FILE);

        assertThat(mainFile).exists(); // The final file must exist
        assertThat(tempFile).doesNotExist(); // The temporary file must have been swapped and cleaned up
    }
}
