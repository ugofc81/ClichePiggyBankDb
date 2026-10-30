package com.example.clichepiggybank.service;

import com.example.clichepiggybank.model.Sanction;
import com.example.clichepiggybank.model.SanctionAmount;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class SanctionStorageServiceTest {
    private SanctionStorageService storageService;

    private final String TEST_FILE = "users-test.json";
    private final String TEST_TEMP_FILE = "users-test.json.tmp";

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        storageService = new SanctionStorageService(objectMapper);

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
        HashMap<UUID, Sanction> users = storageService.loadSanctions();

        assertThat(users).isNotNull().isEmpty();
    }

    private void cleanUpTestFiles() {
        new File(TEST_FILE).delete();
        new File(TEST_TEMP_FILE).delete();
    }

    @Test
    void shouldSaveAndLoadSanctionsSuccessfully() {
        HashMap<UUID, Sanction> testSanctions = new HashMap<>();
        UUID newSanctionGuid = UUID.fromString("55c1dc52-fe0d-4447-a53a-5566f60c5113");
        UUID newReporterGuid = UUID.fromString("be4523cf-6cba-4747-8989-ce8e21470e34");
        UUID newReceiverGuid = UUID.fromString("f6774142-8b20-45fd-bd62-c1dab2a678f6");
        Date now = new Date();
        String reason = "Sample reason";
        Sanction newSanction = new Sanction();
        newSanction.setId(newSanctionGuid);
        newSanction.setReporterGuid(newReporterGuid);
        newSanction.setReceiverGuid(newReceiverGuid);
        newSanction.setAmount(SanctionAmount.HIGH);
        newSanction.setReason(reason);
        newSanction.setDatetime(now);
        newSanction.setLikesNumber(0);
        testSanctions.put(newSanctionGuid, newSanction);

        storageService.saveSanctions(testSanctions);
        HashMap<UUID, Sanction> loadedSanctions = storageService.loadSanctions();

        assertThat(loadedSanctions).hasSize(1);
        assertThat(loadedSanctions.get(newSanctionGuid).getId()).isEqualTo(newSanctionGuid);
        assertThat(loadedSanctions.get(newSanctionGuid).getReporterGuid()).isEqualTo(newReporterGuid);
        assertThat(loadedSanctions.get(newSanctionGuid).getReceiverGuid()).isEqualTo(newReceiverGuid);
        assertThat(loadedSanctions.get(newSanctionGuid).getDatetime()).isEqualTo(now);
        assertThat(loadedSanctions.get(newSanctionGuid).getReason()).isEqualTo(reason);
    }

    @Test
    void shouldAtomicSwapDeleteOldFileLayout() {
        HashMap<UUID, Sanction> testSanctions = new HashMap<>();
        UUID newSanctionGuid = UUID.fromString("55c1dc52-fe0d-4447-a53a-5566f60c5113");
        UUID newReporterGuid = UUID.fromString("be4523cf-6cba-4747-8989-ce8e21470e34");
        UUID newReceiverGuid = UUID.fromString("f6774142-8b20-45fd-bd62-c1dab2a678f6");
        Date now = new Date();
        String reason = "Sample reason";
        Sanction newSanction = new Sanction();
        newSanction.setId(newSanctionGuid);
        newSanction.setReporterGuid(newReporterGuid);
        newSanction.setReceiverGuid(newReceiverGuid);
        newSanction.setAmount(SanctionAmount.HIGH);
        newSanction.setReason(reason);
        newSanction.setDatetime(now);
        newSanction.setLikesNumber(0);
        testSanctions.put(newSanctionGuid, newSanction);

        storageService.saveSanctions(testSanctions);

        File mainFile = new File(TEST_FILE);
        File tempFile = new File(TEST_TEMP_FILE);

        assertThat(mainFile).exists(); // The final file must exist
        assertThat(tempFile).doesNotExist(); // The temporary file must have been swapped and cleaned up
    }
}
