package com.example.clichepiggybankdb.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class SanctionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldGetAndSetFieldsCorrectly() {
        Sanction sanction = new Sanction();
        List<String> roles = new ArrayList<>();
        roles.add("user");
        User newReporter = new User();
        newReporter.setName("alice");
        newReporter.setRoles(roles);
        User newReceiver = new User();
        newReporter.setName("bob");
        newReporter.setRoles(roles);
        Date datetime = new Date();
        sanction.setReporterGuid(newReporter.getId());
        sanction.setReceiverGuid(newReceiver.getId());
        sanction.setAmount(SanctionAmount.HIGH);
        sanction.setReason("some reason");
        sanction.setDatetime(datetime);
        sanction.setLikesNumber(0);

        assertThat(sanction.getReporterGuid()).isEqualTo(newReporter.getId());
        assertThat(sanction.getReceiverGuid()).isEqualTo(newReceiver.getId());
        assertThat(sanction.getAmount()).isEqualTo(SanctionAmount.HIGH);
        assertThat(sanction.getLikesNumber()).isEqualTo(0);
    }

    @Test
    void shouldSerializeToJsonSuccessfully() throws Exception {
        List<String> roles = new ArrayList<>();
        roles.add("user");
        User newReporter = new User();
        newReporter.setName("alice");
        newReporter.setRoles(roles);
        User newReceiver = new User();
        newReporter.setName("bob");
        newReporter.setRoles(roles);
        Date datetime = new Date();
        Sanction sanction = new Sanction();
        sanction.setReporterGuid(newReporter.getId());
        sanction.setReceiverGuid(newReceiver.getId());
        sanction.setAmount(SanctionAmount.HIGH);
        sanction.setReason("new reason");
        sanction.setDatetime(datetime);
        sanction.setLikesNumber(0);

        String jsonResult = objectMapper.writeValueAsString(sanction);

        assertThat(jsonResult)
                .contains("\"reason\":\"new reason\"");
    }

    @Test
    void shouldDeserializeFromJsonSuccessfully() throws Exception {
        // Arrange - A JSON string exactly like what Postman sends
        String inputJson = "{\n" +
                "    \"id\": \"7b485c91-95af-4153-b51a-6b4310a368e0\",\n" +
                "    \"reporterGuid\": \"27ee6c9a-ce75-4515-a4a8-27e755efe0e2\",\n" +
                "    \"receiverGuid\": \"fba592ba-36c3-4479-ba38-bcaeceff9ff0\",\n" +
                "    \"amount\": \"STANDARD\",\n" +
                "    \"reason\": \"abce\",\n" +
                "    \"datetime\": \"2026-10-01T08:22:15.032Z\",\n" +
                "    \"likedBy\": [],\n" +
                "    \"likes\": 0\n" +
                "  }";

        Sanction sanctionResult = objectMapper.readValue(inputJson, Sanction.class);

        assertThat(sanctionResult).isNotNull();
        assertThat(sanctionResult.getId().toString()).isEqualTo("7b485c91-95af-4153-b51a-6b4310a368e0");
        assertThat(sanctionResult.getReporterGuid().toString()).isEqualTo("27ee6c9a-ce75-4515-a4a8-27e755efe0e2");
        assertThat(sanctionResult.getReceiverGuid().toString()).isEqualTo("fba592ba-36c3-4479-ba38-bcaeceff9ff0");
        assertThat(sanctionResult.getAmount()).isEqualTo(SanctionAmount.STANDARD);
        assertThat(sanctionResult.getReason()).isEqualTo("abce");
    }
}