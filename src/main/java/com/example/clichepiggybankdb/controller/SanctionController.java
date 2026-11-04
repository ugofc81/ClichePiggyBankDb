package com.example.clichepiggybankdb.controller;

import com.example.clichepiggybankdb.controller.exceptions.*;
import com.example.clichepiggybankdb.model.Account;
import com.example.clichepiggybankdb.model.Like;
import com.example.clichepiggybankdb.model.Role;
import com.example.clichepiggybankdb.model.Sanction;
import com.example.clichepiggybankdb.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sanctions")
@CrossOrigin(origins = "http://localhost:5173")
public class SanctionController {
    private final SanctionRepository sanctionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final LikeRepository likeRepository;
    private final static int AMOUNT_OF_LAST_SANCTIONS = 10;
    public final static String LAST_SANCTIONS_QUERY = "SELECT s FROM sanctions s ORDER BY s.datetime DESC";

    public SanctionController(
            SanctionRepository sanctionRepository,
            UserRepository userRepository,
            AccountRepository accountRepository,
            RoleRepository roleRepository,
            LikeRepository likeRepository
    ) {
        this.sanctionRepository = sanctionRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.likeRepository = likeRepository;
    }

//    @GetMapping("/all")
//    public ResponseEntity<List<Sanction>> getAllSanctions(@RequestParam("inquirerid") UUID inquirerId) {
//        if(!userRepository.existsById(inquirerId)) {
//            throw new InquirerNotFoundException(inquirerId);
//        }
//        if (!UserController.isUser(roleRepository.findByUserGuid(inquirerId))) {
//            throw new ForbiddenException(inquirerId);
//        }
//        return ResponseEntity.ok(new ArrayList<>(sanctionRepository.findAll()));
//    }

    @PostMapping
    public ResponseEntity<Sanction> createSanction(@RequestBody Sanction newSanction, @RequestParam("inquirerid") UUID inquirerId) {
        System.out.println(inquirerId.toString());

        if(!userRepository.existsById(newSanction.getReceiverGuid())) {
            throw new UserNotFoundException(newSanction.getReceiverGuid());
        }
        if(!userRepository.existsById(inquirerId)) {
            throw new InquirerNotFoundException(inquirerId);
        }

        List<Role> inquirerRoles = roleRepository.findByUserGuid(inquirerId);

        if (!UserController.isAdmin(inquirerRoles) && !UserController.isUser(inquirerRoles)) {
            throw new ForbiddenException(inquirerId);
        }

        newSanction.setReporterGuid(inquirerId);
        newSanction.setLikesNumber(0);

        if (accountRepository.findByOwnerGuid(newSanction.getReceiverGuid()).isEmpty()) {
            Account toBeSaved = new Account();
            toBeSaved.setOwnerGuid(newSanction.getReceiverGuid());
            toBeSaved.setBalance(0);
            accountRepository.save(toBeSaved);
        }
        Account toBeCharged = accountRepository.findByOwnerGuid(newSanction.getReceiverGuid()).getFirst();
        toBeCharged.setBalance(toBeCharged.getBalance() + newSanction.getAmount().amount());
        accountRepository.save(toBeCharged);
        return ResponseEntity.ok(sanctionRepository.save(newSanction));
    }

    @GetMapping
    public ResponseEntity<Page<Sanction>> paginateSanctions(
            @RequestParam(name = "sort", defaultValue = "datetime") String sort,
            @RequestParam(name = "order", defaultValue = "desc") String order,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam("inquirerid") UUID inquirerId) {
        if(!userRepository.existsById(inquirerId)) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!UserController.isUser(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        String sanitizedSort;
        switch(sort) {
            case "likes":
                sanitizedSort = "likesNumber";
                break;
            default:
                sanitizedSort = "datetime";
        }
        Sort sanitizedSortOrder;
        switch(order.toUpperCase(Locale.ROOT)) {
            case "ASC":
                sanitizedSortOrder = Sort.by(sanitizedSort).ascending();
                break;
            default:
                sanitizedSortOrder = Sort.by(sanitizedSort).descending();
        }

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                sanitizedSortOrder
        );
        Page<Sanction> sanctionsPage = sanctionRepository.findAll(pageRequest);
        sanctionsPage.getContent().forEach(s -> {
            if (likeRepository.findByLikedGuidAndLikerGuid(s.getId(), inquirerId).isEmpty()) {
                s.setLiked(Boolean.FALSE);
            } else {
                s.setLiked(Boolean.TRUE);
            }
        });

        return ResponseEntity.ok(sanctionsPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sanction> getSanction(
            @PathVariable UUID id,  @RequestParam("inquirerid") UUID inquirerId) {
        if(!userRepository.existsById(inquirerId)) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!UserController.isUser(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        Sanction sanction = sanctionRepository.findById(id)
                .orElseThrow(() -> new SanctionNotFoundException(id));
        if (likeRepository.findByLikedGuidAndLikerGuid(sanction.getId(), inquirerId).isEmpty()) {
            sanction.setLiked(Boolean.FALSE);
        } else {
            sanction.setLiked(Boolean.TRUE);
        }
        return ResponseEntity.ok(sanction);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Sanction> likeSanction(@PathVariable UUID id, @RequestParam("inquirerid") UUID inquirerId) {
        Sanction updatableSanction = sanctionRepository.findById(id)
                .orElseThrow(() -> new SanctionNotFoundException(id));
        if(!userRepository.existsById(inquirerId)) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!UserController.isUser(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        if(likeRepository.findByLikedGuidAndLikerGuid(id, inquirerId).isPresent()) {
            throw new AlreadyLikedException(id, inquirerId);
        }
        Like newLike = new Like();
        newLike.setLikedGuid(id);
        newLike.setLikerGuid(inquirerId);
        updatableSanction.setLikesNumber(updatableSanction.getLikesNumber() + 1);
        sanctionRepository.save(updatableSanction);
        likeRepository.save(newLike);
        updatableSanction.setLiked(Boolean.TRUE);
        return ResponseEntity.ok(updatableSanction);
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Sanction> unlikeSanction(@PathVariable UUID id, @RequestParam("inquirerid") UUID inquirerId) {
        Sanction updatableSanction = sanctionRepository.findById(id)
                .orElseThrow(() -> new SanctionNotFoundException(id));
        if(!userRepository.existsById(inquirerId)) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!UserController.isUser(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        if(likeRepository.findByLikedGuidAndLikerGuid(id, inquirerId).isEmpty()) {
            throw new LikeNotFoundException(id, inquirerId);
        }
        Like deletableLike = likeRepository.findByLikedGuidAndLikerGuid(id, inquirerId).get();
        updatableSanction.setLikesNumber(updatableSanction.getLikesNumber() - 1);
        sanctionRepository.save(updatableSanction);
        likeRepository.delete(deletableLike);
        updatableSanction.setLiked(Boolean.FALSE);
        return ResponseEntity.ok(updatableSanction);
    }
}
