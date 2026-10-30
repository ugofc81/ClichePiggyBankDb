package com.example.clichepiggybank.service;

import com.example.clichepiggybank.model.Sanction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.UUID;

@Service
public class SanctionStorageService {
    @Value("${storage.sanction-file}")
    private String filePath;

    @Value("${storage.sanction-temp-file}")
    private String tempFilePath;

    private final ObjectMapper objectMapper;

    public SanctionStorageService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public synchronized HashMap<UUID, Sanction> loadSanctions() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(file, new TypeReference<HashMap<UUID, Sanction>>() {});
        } catch (JacksonException e) {
            System.err.println("error reading sanction data file" + e.getMessage());
            return new HashMap<>();
        }
    }

    public synchronized void saveSanctions(HashMap<UUID, Sanction> sanctionsMap) {
        File tempFile = new File(tempFilePath);
        File finalFile = new File(filePath);

        try {
            objectMapper.writeValue(tempFile, sanctionsMap);
            Files.move(
                    tempFile.toPath(),
                    finalFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (IOException e) {
            System.err.println("error saving sanction data file" + e.getMessage());
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
