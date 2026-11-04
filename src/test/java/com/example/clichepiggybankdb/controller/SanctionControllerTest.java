package com.example.clichepiggybankdb.controller;

import com.example.clichepiggybankdb.model.*;
import com.example.clichepiggybankdb.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private LikeRepository likeRepository;

    public Page<Sanction> createPageFromList(List<Sanction> list, Pageable pageable) {
        // 1. Calculate the start index based on the page offset
        int start = (int) pageable.getOffset();

        // 2. Prevent index out of bounds if the requested page starts past the list size
        int end = Math.min((start + pageable.getPageSize()), list.size());

        // 3. Extract the subset list for the current page
        List<Sanction> pageContent = list.subList(Math.min(start, list.size()), end);

        // 4. Return the Page implementation with content, request parameters, and total list size
        return new PageImpl<>(pageContent, pageable, list.size());
    }

    @Test
    void wontCreateSanctionBecauseNonExistingReceiver() throws Exception {
        UUID inquirerId = UUID.fromString("990b0301-9262-4048-a4d6-4f7ad19eeb46");
        UUID receiverId = UUID.fromString("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e");
        Sanction newSanction = new Sanction();
        newSanction.setReceiverGuid(receiverId);
        newSanction.setReason("some Reason");
        newSanction.setAmount(SanctionAmount.HIGH);

        Mockito.when(userRepository.existsById(receiverId)).thenReturn(Boolean.FALSE);

        mockMvc.perform(post("/api/sanctions")
                        .param("inquirerid", inquirerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSanction)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id '" + receiverId.toString() + "' was not found."));
    }

    @Test
    void wontCreateSanctionBecauseNonExistingInquirer() throws Exception {
        UUID inquirerId = UUID.fromString("990b0301-9262-4048-a4d6-4f7ad19eeb46");
        UUID receiverId = UUID.fromString("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e");
        Sanction newSanction = new Sanction();
        newSanction.setReceiverGuid(receiverId);
        newSanction.setReason("some Reason");
        newSanction.setAmount(SanctionAmount.HIGH);

        Mockito.when(userRepository.existsById(receiverId)).thenReturn(Boolean.TRUE);
        Mockito.when(userRepository.existsById(inquirerId)).thenReturn(Boolean.FALSE);

        mockMvc.perform(post("/api/sanctions")
                        .param("inquirerid", inquirerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSanction)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Inquirer with id '" + inquirerId.toString() + "' was not found."));
    }

    @Test
    void wontCreateSanctionBecauseNoInquirerRole() throws Exception {
        UUID inquirerId = UUID.fromString("990b0301-9262-4048-a4d6-4f7ad19eeb46");
        UUID receiverId = UUID.fromString("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e");
        Sanction newSanction = new Sanction();
        newSanction.setReceiverGuid(receiverId);
        newSanction.setReason("some Reason");
        newSanction.setAmount(SanctionAmount.HIGH);

        Mockito.when(userRepository.existsById(receiverId)).thenReturn(Boolean.TRUE);
        Mockito.when(userRepository.existsById(inquirerId)).thenReturn(Boolean.TRUE);
        Mockito.when(roleRepository.findByUserGuid(inquirerId)).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/api/sanctions")
                        .param("inquirerid", inquirerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSanction)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Inquirer with id '" + inquirerId.toString() + "' did not have enough rights to perform the request."));
    }

    @Test
    void createsSanctionForAccountThatAlreadyExists() throws Exception {
        UUID inquirerId = UUID.fromString("990b0301-9262-4048-a4d6-4f7ad19eeb46");
        UUID receiverId = UUID.fromString("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e");
        Sanction newSanction = new Sanction();
        Sanction completedSanction = new Sanction();
        newSanction.setReceiverGuid(receiverId);
        newSanction.setReason("some Reason");
        newSanction.setAmount(SanctionAmount.HIGH);
        completedSanction.setReceiverGuid(receiverId);
        completedSanction.setReason("some Reason");
        completedSanction.setAmount(SanctionAmount.HIGH);
        completedSanction.setReporterGuid(inquirerId);
        completedSanction.setId(UUID.randomUUID());
        completedSanction.setDatetime(new Date());

        Mockito.when(userRepository.existsById(receiverId)).thenReturn(Boolean.TRUE);
        Mockito.when(userRepository.existsById(inquirerId)).thenReturn(Boolean.TRUE);

        List<Role> roles = new ArrayList<>();
        Role userRole = new Role();
        userRole.setUserGuid(inquirerId);
        userRole.setRole("admin");
        roles.add(userRole);
        Mockito.when(roleRepository.findByUserGuid(inquirerId)).thenReturn(roles);

        Account existingAccount = new Account();
        existingAccount.setOwnerGuid(receiverId);
        existingAccount.setBalance(0);
        existingAccount.setId(UUID.randomUUID());

        List<Account> accountList = new ArrayList<>();
        accountList.add(existingAccount);

        Mockito.when(accountRepository.findByOwnerGuid(receiverId)).thenReturn(accountList);
        Mockito.when(accountRepository.save(Mockito.any())).thenReturn(existingAccount);
        Mockito.when(sanctionRepository.save(Mockito.any())).thenReturn(completedSanction);

        mockMvc.perform(post("/api/sanctions")
                        .param("inquirerid", inquirerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSanction)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason", is("some Reason")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.datetime", notNullValue()));
    }

    @Test
    void shouldCreateSanctionAndCreateAndChargeAccount() throws Exception {
        UUID inquirerId = UUID.fromString("990b0301-9262-4048-a4d6-4f7ad19eeb46");
        UUID receiverId = UUID.fromString("c0a6d648-d769-4ebb-a0f0-7d44a0b0462e");
        Sanction newSanction = new Sanction();
        Sanction completedSanction = new Sanction();
        newSanction.setReceiverGuid(receiverId);
        newSanction.setReason("some Reason");
        newSanction.setAmount(SanctionAmount.HIGH);
        completedSanction.setReceiverGuid(receiverId);
        completedSanction.setReason("some Reason");
        completedSanction.setAmount(SanctionAmount.HIGH);
        completedSanction.setReporterGuid(inquirerId);
        completedSanction.setId(UUID.randomUUID());
        completedSanction.setDatetime(new Date());

        Mockito.when(userRepository.existsById(receiverId)).thenReturn(Boolean.TRUE);
        Mockito.when(userRepository.existsById(inquirerId)).thenReturn(Boolean.TRUE);

        List<Role> roles = new ArrayList<>();
        Role userRole = new Role();
        userRole.setUserGuid(inquirerId);
        userRole.setRole("admin");
        roles.add(userRole);
        Mockito.when(roleRepository.findByUserGuid(inquirerId)).thenReturn(roles);

        List<Account> accountEmptyList = new ArrayList<>();

        Mockito.when(accountRepository.findByOwnerGuid(receiverId)).thenReturn(accountEmptyList);
        List<Account> accountToBeFilledList = new ArrayList<>();
        Account newAccount = new Account();
        newAccount.setOwnerGuid(receiverId);
        newAccount.setBalance(0);
        newAccount.setId(UUID.randomUUID());
        accountToBeFilledList.add(newAccount);
        Mockito.when(accountRepository.findByOwnerGuid(receiverId)).thenReturn(accountToBeFilledList);

        Mockito.when(accountRepository.save(Mockito.any())).thenReturn(newAccount);
        Mockito.when(sanctionRepository.save(Mockito.any())).thenReturn(completedSanction);

        mockMvc.perform(post("/api/sanctions")
                        .param("inquirerid", inquirerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSanction)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason", is("some Reason")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.datetime", notNullValue()));
    }

    @Test
    void shouldReturnNotFoundWhenInquirerDoesNotExist() throws Exception {
        UUID missingInquirerId = UUID.randomUUID();
        Mockito.when(userRepository.existsById(missingInquirerId)).thenReturn(Boolean.FALSE);

        mockMvc.perform(get("/api/sanctions").param("inquirerid", missingInquirerId.toString()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Inquirer with id '" + missingInquirerId + "' was not found."));
    }

    @Test
    void shouldReturnForbiddenWhenInquirerNotUser() throws Exception {
        User inquirer = configureInquirer();
        List<Role> roles = new ArrayList<>();
        Role userRole = new Role();
        userRole.setUserGuid(inquirer.getId());
        userRole.setRole("admin");
        roles.add(userRole);
        Mockito.when(userRepository.existsById(inquirer.getId())).thenReturn(Boolean.TRUE);
        Mockito.when(roleRepository.findByUserGuid(inquirer.getId())).thenReturn(roles);

        mockMvc.perform(get("/api/sanctions")
                        .param("inquirerid", inquirer.getId().toString()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Inquirer with id '" + inquirer.getId() + "' did not have enough rights to perform the request."));
    }

    @Test
    void shouldPaginateSanctionsInRequestedSortOrder() throws Exception {
        User inquirer = configureInquirer();

        Sanction oldest = produceSanction(UUID.randomUUID(), 1L, 30);
        Sanction secondOldest = produceSanction(UUID.randomUUID(), 2L, 10);
        Sanction secondNewest = produceSanction(UUID.randomUUID(), 3L, 40);
        Sanction newest = produceSanction(UUID.randomUUID(), 4L, 20);

        List<Sanction> sanctions = new ArrayList<>(List.of(oldest, secondOldest, secondNewest, newest));

        List<Role> roles = new ArrayList<>();
        Role userRole = new Role();
        userRole.setUserGuid(inquirer.getId());
        userRole.setRole("user");
        roles.add(userRole);

        String sort = "datetime";
        String order = "asc";
        String size = "2";
        String page = "0";
        Sort sanitizedSortOrder = Sort.by(sort).ascending();
        PageRequest pageRequest = PageRequest.of(
                Integer.valueOf(page),
                Integer.valueOf(size),
                sanitizedSortOrder
        );

        Mockito.when(userRepository.existsById(inquirer.getId())).thenReturn(Boolean.TRUE);
        Mockito.when(roleRepository.findByUserGuid(inquirer.getId())).thenReturn(roles);
        Page<Sanction> pageFromList = createPageFromList(sanctions, pageRequest);

        Mockito.when(sanctionRepository.findAll(pageRequest)).thenReturn(pageFromList);

        mockMvc.perform(get("/api/sanctions")
                        .param("inquirerid", inquirer.getId().toString())
                        .param("sort", sort)
                        .param("order", order)
                        .param("size", size)
                        .param("page", page))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(4)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.pageable.pageSize", is(2)))
                .andExpect(jsonPath("$.content[0].id", is(oldest.getId().toString())))
                .andExpect(jsonPath("$.content[0].datetime", is("1970-01-01T00:00:00.001Z")))
                .andExpect(jsonPath("$.content[1].likesNumber", is(10)));
    }

    @Test
    void shouldUseDefaultPaginationAndSortByNewestFirst() throws Exception {
        User inquirer = configureInquirer();

        Sanction oldest = produceSanction(UUID.randomUUID(), 1L, 30);
        Sanction secondOldest = produceSanction(UUID.randomUUID(), 2L, 10);
        Sanction secondNewest = produceSanction(UUID.randomUUID(), 3L, 40);
        Sanction newest = produceSanction(UUID.randomUUID(), 4L, 20);

        List<Sanction> sanctions = new ArrayList<>(List.of(oldest, oldest, secondOldest, secondOldest, secondOldest, secondNewest, secondNewest, secondNewest, newest, newest));

        List<Role> roles = new ArrayList<>();
        Role userRole = new Role();
        userRole.setUserGuid(inquirer.getId());
        userRole.setRole("user");
        roles.add(userRole);

        String sort = "datetime";
        PageRequest pageRequest = PageRequest.of(
                Integer.valueOf("0"),
                Integer.valueOf("10"),
                Sort.by(sort).descending()
        );

        Mockito.when(userRepository.existsById(inquirer.getId())).thenReturn(Boolean.TRUE);
        Mockito.when(roleRepository.findByUserGuid(inquirer.getId())).thenReturn(roles);
        Page<Sanction> pageFromList = createPageFromList(sanctions, pageRequest);

        Mockito.when(sanctionRepository.findAll(pageRequest)).thenReturn(pageFromList);

        mockMvc.perform(get("/api/sanctions")
                        .param("inquirerid", inquirer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageable.pageSize", is(10)));
    }

    @Test
    void shouldReturnNotFoundWhenSanctionDoesNotExist() throws Exception {
        User inquirer = configureInquirer();

        List<Role> roles = new ArrayList<>();
        Role userRole = new Role();
        userRole.setUserGuid(inquirer.getId());
        userRole.setRole("user");
        roles.add(userRole);

        Mockito.when(userRepository.existsById(inquirer.getId())).thenReturn(Boolean.TRUE);
        Mockito.when(roleRepository.findByUserGuid(inquirer.getId())).thenReturn(roles);

        UUID missingId = UUID.randomUUID();
        Mockito.when(sanctionRepository.findById(missingId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/sanctions/{id}", missingId)
                        .param("inquirerid", inquirer.getId().toString()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sanction with id '" + missingId + "' was not found."));
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
        sanction.setReason("So to say");
        sanction.setDatetime(new Date());
        sanction.setLikesNumber(0);

        List<Role> roles = new ArrayList<>();
        Role userRole = new Role();
        userRole.setUserGuid(reporter.getId());
        userRole.setRole("user");
        roles.add(userRole);
        Mockito.when(roleRepository.findByUserGuid(reporter.getId())).thenReturn(roles);
        Mockito.when(userRepository.existsById(reporter.getId())).thenReturn(Boolean.TRUE);

        Like like = new Like();
        like.setLikerGuid(reporter.getId());
        like.setLikedGuid(sanctionId);
        Mockito.when(sanctionRepository.findById(sanctionId)).thenReturn(Optional.of(sanction));
        Mockito.when(likeRepository.findByLikedGuidAndLikerGuid(sanctionId, reporter.getId())).thenReturn(Optional.of(like));

        mockMvc.perform(get("/api/sanctions/{id}", sanctionId)
                        .param("inquirerid", reporter.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sanctionId.toString()))
                .andExpect(jsonPath("$.receiverGuid").value(receiver.getId().toString()));
    }


//    @Test
//    void shouldGetAllSanctions() throws Exception {
//        User inquirer = configureInquirer();
//        Sanction firstSanction = produceSanction(UUID.randomUUID(), 1L, 2);
//        Sanction secondSanction = produceSanction(UUID.randomUUID(), 2L, 5);
//        List<Sanction> sanctionsList = new ArrayList<>();
//        sanctionsList.add(firstSanction);
//        sanctionsList.add(secondSanction);
//        List<Role> roles = new ArrayList<>();
//        Role userRole = new Role();
//        userRole.setUserGuid(inquirer.getId());
//        userRole.setRole("user");
//        roles.add(userRole);
//        Mockito.when(roleRepository.findByUserGuid(inquirer.getId())).thenReturn(roles);
//        Mockito.when(userRepository.existsById(inquirer.getId())).thenReturn(Boolean.TRUE);
//        Mockito.when(sanctionRepository.findAll()).thenReturn(sanctionsList);
//
//        mockMvc.perform(get("/api/sanctions/all").param("inquirerid", inquirer.getId().toString()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[*].id", containsInAnyOrder(
//                        firstSanction.getId().toString(),
//                        secondSanction.getId().toString()
//                )));
//    }




//    @Test
//    void shouldGetLastTenSanctionsInDescendingDateOrder() throws Exception {
//        User inquirer = configureInquirer();
//        List<Sanction> sanctionList = new ArrayList<>();
//        List<String> expectedIds = new ArrayList<>();
//        for (int index = 0; index < 12; index++) {
//            Sanction sanction = produceSanction(UUID.randomUUID(), index, index);
//            sanctionList.addFirst(sanction);
//            if (index > 1) {
//                expectedIds.addFirst(sanction.getId().toString());
//            }
//        }
//        Mockito.when(userRepository.existsById(inquirer.getId())).thenReturn(Boolean.TRUE);
//        List<Role> roles = new ArrayList<>();
//        Role userRole = new Role();
//        userRole.setUserGuid(inquirer.getId());
//        userRole.setRole("user");
//        roles.add(userRole);
//        Mockito.when(roleRepository.findByUserGuid(inquirer.getId())).thenReturn(roles);
//        Mockito.when(sanctionRepository.createQuery(SanctionController.LAST_SANCTIONS_QUERY, Sanction.class)).thenReturn(sanctionList);
//
//        mockMvc.perform(get("/api/sanctions/latest").param("inquirerid", inquirer.getId().toString()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(10)))
//                .andExpect(jsonPath("$[*].id", contains(expectedIds.toArray())));
//    }


//    @Test
//    void shouldGetBestSanctionsInDescendingLikesOrder() throws Exception {
//        User inquirer = configureInquirer();
//        Sanction leastLiked = produceSanction(UUID.randomUUID(), 1L, 1);
//        Sanction mostLiked = produceSanction(UUID.randomUUID(), 2L, 9);
//        Sanction middleLiked = produceSanction(UUID.randomUUID(), 3L, 4);
//        List<Sanction> sanctionsList = new ArrayList<>();
//        sanctionsList.add(leastLiked);
//        sanctionsList.add(mostLiked);
//        sanctionsList.add(middleLiked);
//        Mockito.when(sanctionRepository.findAll()).thenReturn(sanctionsList);
//
//        mockMvc.perform(get("/api/sanctions/best").param("inquirerid", inquirer.getId().toString()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[*].id", contains(
//                        mostLiked.getId().toString(),
//                        middleLiked.getId().toString(),
//                        leastLiked.getId().toString()
//                )));
//    }

    @Test
    void shouldLikeSanctionAndSaveUpdatedLikes() throws Exception {
        //define inquirer
        User inquirer = configureInquirer();
        //define existing sanction
        UUID otherUserId = UUID.randomUUID();
        Sanction sanction = produceSanction(UUID.randomUUID(), 1L, 1);
        List<Sanction> sanctions = new ArrayList<>();
        sanctions.add(sanction);

        //mock search of sanction
        Mockito.when(sanctionRepository.findById(sanction.getId())).thenReturn(Optional.of(sanction));

        //mock search of inquirer
        Mockito.when(userRepository.existsById(inquirer.getId())).thenReturn(Boolean.TRUE);

        //sets roles of inquirer
        List<Role> roles = new ArrayList<>();
        Role userRole = new Role();
        userRole.setUserGuid(inquirer.getId());
        userRole.setRole("user");
        roles.add(userRole);

        //mocks search of inquirer roles
        Mockito.when(roleRepository.findByUserGuid(inquirer.getId())).thenReturn(roles);

        //define like
        Like like = new Like();
        like.setId(UUID.randomUUID());
        like.setLikedGuid(sanction.getId());
        like.setLikerGuid(inquirer.getId());
        List<Like> emptyLikes = new ArrayList<>();
        //mock like search
        Mockito.when(likeRepository.findByLikedGuidAndLikerGuid(sanction.getId(), inquirer.getId())).thenReturn(Optional.empty());
        Mockito.when(sanctionRepository.save(sanction)).thenReturn(sanction);
        Mockito.when(likeRepository.save(like)).thenReturn(like);

        mockMvc.perform(post("/api/sanctions/{id}/like", sanction.getId().toString())
                        .param("inquirerid", inquirer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likesNumber").value(2));

        Mockito.verify(sanctionRepository).save(sanction);
    }

    @Test
    void shouldReturnNotFoundWhenLikingSanctionDoesNotExist() throws Exception {
        User inquirer = configureInquirer();
        UUID missingId = UUID.randomUUID();
        Mockito.when(sanctionRepository.existsById(missingId)).thenReturn(Boolean.FALSE);

        mockMvc.perform(post("/api/sanctions/{id}/like", missingId)
                        .param("inquirerid", inquirer.getId().toString()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sanction with id '" + missingId + "' was not found."));
    }

    @Test
    void shouldUnlikeSanctionAndSaveUpdatedLikes() throws Exception {
        //define inquirer
        User inquirer = configureInquirer();
        //define existing sanction
        UUID otherUserId = UUID.randomUUID();
        Sanction sanction = produceSanction(UUID.randomUUID(), 1L, 1);
        List<Sanction> sanctions = new ArrayList<>();
        sanctions.add(sanction);

        //mock search of sanction
        Mockito.when(sanctionRepository.findById(sanction.getId())).thenReturn(Optional.of(sanction));

        //mock search of inquirer
        Mockito.when(userRepository.existsById(inquirer.getId())).thenReturn(Boolean.TRUE);

        //sets roles of inquirer
        List<Role> roles = new ArrayList<>();
        Role userRole = new Role();
        userRole.setUserGuid(inquirer.getId());
        userRole.setRole("user");
        roles.add(userRole);

        //mocks search of inquirer roles
        Mockito.when(roleRepository.findByUserGuid(inquirer.getId())).thenReturn(roles);

        //define like
        Like like = new Like();
        like.setId(UUID.randomUUID());
        like.setLikedGuid(sanction.getId());
        like.setLikerGuid(inquirer.getId());
        List<Like> emptyLikes = new ArrayList<>();
        //mock like search
        Mockito.when(likeRepository.findByLikedGuidAndLikerGuid(sanction.getId(), inquirer.getId())).thenReturn(Optional.of(like));
        Mockito.when(sanctionRepository.save(sanction)).thenReturn(sanction);
        Mockito.doNothing().when(likeRepository).delete(like);
        mockMvc.perform(delete("/api/sanctions/{id}/like", sanction.getId())
                        .param("inquirerid", inquirer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likesNumber").value(0));

        Mockito.verify(sanctionRepository).save(sanction);
    }

    @Test
    void shouldReturnNotFoundWhenUnlikingSanctionDoesNotExist() throws Exception {
        User inquirer = configureInquirer();
        UUID missingId = UUID.randomUUID();
        Mockito.when(sanctionRepository.findAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(delete("/api/sanctions/{id}/like", missingId)
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
