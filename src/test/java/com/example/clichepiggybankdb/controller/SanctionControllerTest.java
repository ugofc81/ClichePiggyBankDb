package com.example.clichepiggybankdb.controller;

import com.example.clichepiggybankdb.model.Sanction;
import com.example.clichepiggybankdb.model.SanctionAmount;
import com.example.clichepiggybankdb.model.User;
import com.example.clichepiggybankdb.service.AccountRepository;
import com.example.clichepiggybankdb.service.SanctionRepository;
import com.example.clichepiggybankdb.service.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SanctionController.class)
class SanctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SanctionRepository sanctionRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AccountRepository accountRepository;

    @Test
    void shouldCreateSanctionAndIndirectlyCreateAndChargeAccount() throws Exception {
        String inquirerId = "990b0301-9262-4048-a4d6-4f7ad19eeb46";
        String receiverId = "c0a6d648-d769-4ebb-a0f0-7d44a0b0462e";
        UUID sanctionId = UUID.randomUUID();

        User reporter = UserControllerTest.produceUser(inquirerId, "alice", "user");
        User receiver = UserControllerTest.produceUser(receiverId, "bob", "user");

        List<User> mockUserList = new ArrayList<>();
        mockUserList.add(reporter);
        mockUserList.add(receiver);

        Mockito.when(userRepository.findAll()).thenReturn(mockUserList);
        Mockito.when(accountRepository.findAll()).thenReturn(new ArrayList<>());
        Mockito.when(sanctionRepository.findAll()).thenReturn(new ArrayList<>());

        Sanction incomingSanction = new Sanction();
        incomingSanction.setId(sanctionId);
        incomingSanction.setReporterGuid(reporter.getId());
        incomingSanction.setReceiverGuid(receiver.getId());
        incomingSanction.setAmount(com.example.clichepiggybankdb.model.SanctionAmount.HIGH);
        incomingSanction.setReason("run you fools");
        incomingSanction.setDatetime(new Date());
        incomingSanction.setLikesNumber(0);

        mockMvc.perform(post("/api/sanctions")
                        .param("inquirerid", inquirerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incomingSanction)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.reporter.name").value("alice"))
                .andExpect(jsonPath("$.receiver.name").value("bob"));
    }

    @Test
    void shouldGetSanctionById() throws Exception {
        UUID sanctionId = UUID.randomUUID();
        User reporter = UserControllerTest.produceUser("990b0301-9262-4048-a4d6-4f7ad19eeb46", "alice", "user");
        User receiver = UserControllerTest.produceUser("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e", "bob", "user");
        Sanction sanction = new Sanction();
        sanction.setId(sanctionId);
        sanction.setReporterGuid(reporter.getId());
        sanction.setReceiverGuid(receiver.getId());
        sanction.setAmount(SanctionAmount.HIGH);
        sanction.setReason("late payment");
        sanction.setDatetime(new Date());
        sanction.setLikesNumber(0);

        List<Sanction> sanctions = new ArrayList<>();
        sanctions.add(sanction);
        Mockito.when(sanctionRepository.findAll()).thenReturn(sanctions);

        mockMvc.perform(get("/api/sanctions/{id}", sanctionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sanctionId.toString()))
                .andExpect(jsonPath("$.receiver.name").value("bob"));
    }

    @Test
    void shouldReturnNotFoundWhenSanctionDoesNotExist() throws Exception {
        UUID missingId = UUID.randomUUID();
        Mockito.when(sanctionRepository.findById(missingId)).thenReturn(null);

        mockMvc.perform(get("/api/sanctions/{id}", missingId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sanction with id '" + missingId + "' was not found."));
    }

    @Test
    void shouldGetAllSanctions() throws Exception {
        User inquirer = configureInquirer();
        Sanction firstSanction = produceSanction(UUID.randomUUID(), 1L, 2);
        Sanction secondSanction = produceSanction(UUID.randomUUID(), 2L, 5);
        List<Sanction> sanctionsList = new ArrayList<>();
        sanctionsList.add(firstSanction);
        sanctionsList.add(secondSanction);
        Mockito.when(sanctionRepository.findAll()).thenReturn(sanctionsList);

        mockMvc.perform(get("/api/sanctions").param("inquirerid", inquirer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(
                        firstSanction.getId().toString(),
                        secondSanction.getId().toString()
                )));
    }

    @Test
    void shouldReturnNotFoundWhenInquirerDoesNotExist() throws Exception {
        UUID missingInquirerId = UUID.randomUUID();
        Mockito.when(userRepository.findById(missingInquirerId)).thenReturn(null);

        mockMvc.perform(get("/api/sanctions").param("inquirerid", missingInquirerId.toString()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Inquirer with id '" + missingInquirerId + "' was not found."));
    }

    @Test
    void shouldGetLastTenSanctionsInDescendingDateOrder() throws Exception {
        User inquirer = configureInquirer();
        List<Sanction> sanctionList = new ArrayList<>();
        List<String> expectedIds = new ArrayList<>();
        for (int index = 0; index < 12; index++) {
            Sanction sanction = produceSanction(UUID.randomUUID(), index, index);
            sanctionList.add(sanction);
            if (index >= 2) {
                expectedIds.addFirst(sanction.getId().toString());
            }
        }
        Mockito.when(sanctionRepository.findAll()).thenReturn(sanctionList);

        mockMvc.perform(get("/api/sanctions/latest").param("inquirerid", inquirer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[*].id", contains(expectedIds.toArray())));
    }

    @Test
    void shouldPaginateSanctionsInRequestedSortOrder() throws Exception {
        User inquirer = configureInquirer();
        Sanction oldest = produceSanction(UUID.randomUUID(), 1L, 30);
        Sanction secondOldest = produceSanction(UUID.randomUUID(), 2L, 10);
        Sanction secondNewest = produceSanction(UUID.randomUUID(), 3L, 40);
        Sanction newest = produceSanction(UUID.randomUUID(), 4L, 20);
        List<Sanction> sanctions = new ArrayList<>(List.of(oldest, secondOldest, secondNewest, newest));
        Mockito.when(sanctionRepository.findAll()).thenReturn(sanctions);

        mockMvc.perform(get("/api/sanctions/paginated")
                        .param("inquirerid", inquirer.getId().toString())
                        .param("sort", "datetime")
                        .param("order", "asc")
                        .param("size", "2")
                        .param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", contains(
                        secondNewest.getId().toString(),
                        newest.getId().toString()
                )));

        mockMvc.perform(get("/api/sanctions/paginated")
                        .param("inquirerid", inquirer.getId().toString())
                        .param("sort", "likes")
                        .param("order", "asc")
                        .param("size", "4")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", contains(
                        secondOldest.getId().toString(),
                        newest.getId().toString(),
                        oldest.getId().toString(),
                        secondNewest.getId().toString()
                )));
    }

    @Test
    void shouldUseDefaultPaginationAndSortByNewestFirst() throws Exception {
        User inquirer = configureInquirer();
        List<Sanction> sanctionsList = new ArrayList<>();
        List<String> expectedIds = new ArrayList<>();
        for (int index = 0; index < 12; index++) {
            Sanction sanction = produceSanction(UUID.randomUUID(), index, index);
            sanctionsList.add(sanction);
            if (index >= 2) {
                expectedIds.addFirst(sanction.getId().toString());
            }
        }
        Mockito.when(sanctionRepository.findAll()).thenReturn(sanctionsList);

        mockMvc.perform(get("/api/sanctions/paginated")
                        .param("inquirerid", inquirer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[*].id", contains(expectedIds.toArray())));
    }

    @Test
    void shouldGetBestSanctionsInDescendingLikesOrder() throws Exception {
        User inquirer = configureInquirer();
        Sanction leastLiked = produceSanction(UUID.randomUUID(), 1L, 1);
        Sanction mostLiked = produceSanction(UUID.randomUUID(), 2L, 9);
        Sanction middleLiked = produceSanction(UUID.randomUUID(), 3L, 4);
        List<Sanction> sanctionsList = new ArrayList<>();
        sanctionsList.add(leastLiked);
        sanctionsList.add(mostLiked);
        sanctionsList.add(middleLiked);
        Mockito.when(sanctionRepository.findAll()).thenReturn(sanctionsList);

        mockMvc.perform(get("/api/sanctions/best").param("inquirerid", inquirer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", contains(
                        mostLiked.getId().toString(),
                        middleLiked.getId().toString(),
                        leastLiked.getId().toString()
                )));
    }

    @Test
    void shouldLikeSanctionAndSaveUpdatedLikes() throws Exception {
        User inquirer = configureInquirer();
        UUID otherUserId = UUID.randomUUID();
        Sanction sanction = produceSanction(UUID.randomUUID(), 1L, 1);
        List<Sanction> sanctions = new ArrayList<>();
        sanctions.add(sanction);
        Mockito.when(sanctionRepository.findAll()).thenReturn(sanctions);

        mockMvc.perform(put("/api/sanctions/{id}/like", sanction.getId())
                        .param("inquirerid", inquirer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes").value(2))
                .andExpect(jsonPath("$.likedBy", containsInAnyOrder(
                        inquirer.getId().toString(),
                        otherUserId.toString()
                )));

        Mockito.verify(sanctionRepository).save(sanction);
    }

    @Test
    void shouldReturnNotFoundWhenLikingSanctionDoesNotExist() throws Exception {
        User inquirer = configureInquirer();
        UUID missingId = UUID.randomUUID();
        Mockito.when(sanctionRepository.findAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(put("/api/sanctions/{id}/like", missingId)
                        .param("inquirerid", inquirer.getId().toString()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sanction with id '" + missingId + "' was not found."));
    }

    @Test
    void shouldUnlikeSanctionAndSaveUpdatedLikes() throws Exception {
        User inquirer = configureInquirer();
        UUID otherUserId = UUID.randomUUID();
        Sanction sanction = produceSanction(UUID.randomUUID(), 1L, 2);
        List<Sanction> sanctions = new ArrayList<>();
        sanctions.add(sanction);
        Mockito.when(sanctionRepository.findAll()).thenReturn(sanctions);

        mockMvc.perform(put("/api/sanctions/{id}/unlike", sanction.getId())
                        .param("inquirerid", inquirer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes").value(1))
                .andExpect(jsonPath("$.likedBy", contains(otherUserId.toString())));

        Mockito.verify(sanctionRepository).save(sanction);
    }

    @Test
    void shouldReturnNotFoundWhenUnlikingSanctionDoesNotExist() throws Exception {
        User inquirer = configureInquirer();
        UUID missingId = UUID.randomUUID();
        Mockito.when(sanctionRepository.findAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(put("/api/sanctions/{id}/unlike", missingId)
                        .param("inquirerid", inquirer.getId().toString()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sanction with id '" + missingId + "' was not found."));
    }

    private User configureInquirer() {
        User inquirer = UserControllerTest.produceUser(
                "990b0301-9262-4048-a4d6-4f7ad19eeb46",
                "alice",
                "user"
        );
        List<User> users = new ArrayList<>();
        users.add(inquirer);
        Mockito.when(userRepository.findAll()).thenReturn(users);
        return inquirer;
    }

    private Sanction produceSanction(UUID id, long datetime, int likes) {
        User reporter = UserControllerTest.produceUser(
                "990b0301-9262-4048-a4d6-4f7ad19eeb46",
                "alice",
                "user"
        );
        User receiver = UserControllerTest.produceUser(
                "c0a6d648-d769-4ebb-a0f0-7d44a0b0462e",
                "bob",
                "user"
        );

        Sanction sanction = new Sanction();
        sanction.setId(id);
        sanction.setReporterGuid(reporter.getId());
        sanction.setReceiverGuid(receiver.getId());
        sanction.setAmount(SanctionAmount.STANDARD);
        sanction.setReason("late payment");
        sanction.setDatetime(new Date(datetime));
        sanction.setLikesNumber(likes);

        return sanction;
    }
}
