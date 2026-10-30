package com.example.clichepiggybankdb.controller;

import com.example.clichepiggybankdb.controller.exceptions.*;
import com.example.clichepiggybankdb.model.Account;
import com.example.clichepiggybankdb.model.Like;
import com.example.clichepiggybankdb.model.Sanction;
import com.example.clichepiggybankdb.service.*;
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

    @GetMapping
    public ResponseEntity<List<Sanction>> getAllSanctions(@RequestParam("inquirerid") UUID inquirerId) {
        if(userRepository.findById(inquirerId).isEmpty()) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!UserController.isUser(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        return ResponseEntity.ok(new ArrayList<>(sanctionRepository.findAll()));
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Sanction>> getLastSanctions(@RequestParam("inquirerid") UUID inquirerId) {
        if(userRepository.findById(inquirerId).isEmpty()) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!UserController.isUser(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }

        List<Sanction> sanctionList = getAllSanctions(inquirerId).getBody();
        Collections.sort(sanctionList, Comparator.comparing(Sanction::getDatetime).reversed());
        int limit = Math.min(sanctionList.size(), AMOUNT_OF_LAST_SANCTIONS);
        return ResponseEntity.ok(sanctionList.subList(0, limit));
    }

    @GetMapping("/best")
    public ResponseEntity<List<Sanction>> getBestSanctions(@RequestParam("inquirerid") UUID inquirerId) {
        if(userRepository.findById(inquirerId).isEmpty()) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!UserController.isUser(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        List<Sanction> sanctionList = getAllSanctions(inquirerId).getBody();
        Collections.sort(sanctionList, Comparator.comparing(Sanction::getLikesNumber).reversed());
        return ResponseEntity.ok(sanctionList);
    }

    @GetMapping("/paginated")
    public ResponseEntity<List<Sanction>> paginateSanctions(
            @RequestParam(name = "sort", defaultValue = "datetime") String sort,
            @RequestParam(name = "order", defaultValue = "desc") String order,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam("inquirerid") UUID inquirerId) {
        if(userRepository.findById(inquirerId).isEmpty()) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!UserController.isUser(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        List<Sanction> sanctionList = getAllSanctions(inquirerId).getBody();
        Comparator c;
        switch(sort) {
            case "likes":
                c = Comparator.comparing(Sanction::getLikesNumber);
                break;
            default:
                c = Comparator.comparing(Sanction::getDatetime);
        }
        switch(order) {
            case "desc":
                c = c.reversed();
                break;
        }
        Collections.sort(sanctionList, c);
        Integer offset = Math.min(Math.max((page-1) * size, 0), sanctionList.size());
        return ResponseEntity.ok(sanctionList.subList(offset, offset+Math.min(sanctionList.size()-offset, size)));
    }

    @PostMapping
    public ResponseEntity<Sanction> createSanction(@RequestBody Sanction newSanction, @RequestParam("inquirerid") UUID inquirerId) {
        newSanction.setDatetime(new Date());

        if(userRepository.findById(newSanction.getReceiverGuid()).isEmpty()) {
            throw new UserNotFoundException(newSanction.getReceiverGuid());
        }
        if(userRepository.findById(inquirerId).isEmpty()) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!UserController.isAdmin(roleRepository.findByUserGuid(inquirerId)) && !UserController.isUser(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }

        newSanction.setReporterGuid(inquirerId);
        newSanction.setLikesNumber(0);

        if (accountRepository.findByOwnerId(newSanction.getReceiverGuid()).isEmpty()) {
            Account toBeSaved = new Account();
            toBeSaved.setOwnerGuid(newSanction.getReceiverGuid());
            toBeSaved.setBalance(0);
            accountRepository.save(toBeSaved);
        }
        Account toBeCharged = accountRepository.findByOwnerId(newSanction.getReceiverGuid()).getFirst();
        toBeCharged.setBalance(toBeCharged.getBalance() + newSanction.getAmount().amount());
        accountRepository.save(toBeCharged);
        return ResponseEntity.ok(sanctionRepository.save(newSanction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sanction> getSanction(@PathVariable UUID id) {
        if(!sanctionRepository.existsById(id)) {
            throw new SanctionNotFoundException(id);
        }
        return ResponseEntity.ok(sanctionRepository.findById(id).get());
    }

    @GetMapping("/{id}/isLiked")
    public ResponseEntity<Boolean> isLiked(@PathVariable UUID id, @RequestParam("inquirerid") UUID inquirerId) {
        if(sanctionRepository.findById(id).isEmpty()) {
            throw new SanctionNotFoundException(id);
        }
        if(userRepository.findById(inquirerId).isEmpty()) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if(likeRepository.findByLikedGuidAndLikerGuid(id, inquirerId).isEmpty()) {
            return ResponseEntity.ok(Boolean.FALSE);
        }
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<Sanction> likeSanction(@PathVariable UUID id, @RequestParam("inquirerid") UUID inquirerId) {
        if(sanctionRepository.findById(id).isEmpty()) {
            throw new SanctionNotFoundException(id);
        }
        if(userRepository.findById(inquirerId).isEmpty()) {
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
        Sanction updatableSanction = sanctionRepository.findById(id).get();
        updatableSanction.setLikesNumber(updatableSanction.getLikesNumber() + 1);
        sanctionRepository.save(updatableSanction);
        likeRepository.save(newLike);
        return ResponseEntity.ok(updatableSanction);
    }

    @PutMapping("/{id}/unlike")
    public ResponseEntity<Sanction> unlikeSanction(@PathVariable UUID id, @RequestParam("inquirerid") UUID inquirerId) {
        if(sanctionRepository.findById(id).isEmpty()) {
            throw new SanctionNotFoundException(id);
        }
        if(userRepository.findById(inquirerId).isEmpty()) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!UserController.isUser(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        if(likeRepository.findByLikedGuidAndLikerGuid(id, inquirerId).isEmpty()) {
            throw new LikeNotFoundException(id, inquirerId);
        }
        Like deletableLike = likeRepository.findByLikedGuidAndLikerGuid(id, inquirerId).get();
        Sanction updatableSanction = sanctionRepository.findById(id).get();
        updatableSanction.setLikesNumber(updatableSanction.getLikesNumber() - 1);
        sanctionRepository.save(updatableSanction);
        likeRepository.delete(deletableLike);
        return ResponseEntity.ok(updatableSanction);
    }
}
