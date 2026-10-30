package com.example.clichepiggybank.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldGetAndSetFieldsCorrectly() {
        UUID newAccountGuid = UUID.fromString("55c1dc52-fe0d-4447-a53a-5566f60c5113");
        UUID newUserGuid = UUID.fromString("be4523cf-6cba-4747-8989-ce8e21470e34");
        Account account = new Account();
        account.setId(newAccountGuid);
        account.setOwnerGuid(newUserGuid);
        account.setBalance(15);

        assertThat(account.getId()).isEqualTo(newAccountGuid);
        assertThat(account.getOwnerGuid()).isEqualTo(newUserGuid);
        assertThat(account.getBalance()).isEqualTo(15);
    }

    @Test
    void shouldSerializeToJsonSuccessfully() throws Exception {
        UUID newAccountGuid = UUID.fromString("55c1dc52-fe0d-4447-a53a-5566f60c5113");
        UUID newUserGuid = UUID.fromString("be4523cf-6cba-4747-8989-ce8e21470e34");
        Account account = new Account();

        String jsonResult = objectMapper.writeValueAsString(account);

        assertThat(jsonResult)
                .contains("\"id\":\"55c1dc52-fe0d-4447-a53a-5566f60c5113\"")
                .contains("\"ownerId\":\"be4523cf-6cba-4747-8989-ce8e21470e34\"")
                .contains("\"balance\":0");
    }

    @Test
    void shouldDeserializeFromJsonSuccessfully() throws Exception {
        // Arrange - A JSON string exactly like what Postman sends
        String inputJson = "{\"id\":\"55c1dc52-fe0d-4447-a53a-5566f60c5113\",\"ownerId\":\"be4523cf-6cba-4747-8989-ce8e21470e34\",\"balance\":5}";

        // Act - Turn the JSON string back into a Java Account object
        Account accountResult = objectMapper.readValue(inputJson, Account.class);

        // Assert
        assertThat(accountResult).isNotNull();
        assertThat(accountResult.getId().toString()).isEqualTo("55c1dc52-fe0d-4447-a53a-5566f60c5113");
        assertThat(accountResult.getOwnerGuid().toString()).isEqualTo("be4523cf-6cba-4747-8989-ce8e21470e34");
        assertThat(accountResult.getBalance()).isEqualTo(5);
    }
}