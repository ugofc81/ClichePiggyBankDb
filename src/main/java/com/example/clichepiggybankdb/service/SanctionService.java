package com.example.clichepiggybankdb.service;

import com.example.clichepiggybankdb.controller.exceptions.*;
import com.example.clichepiggybankdb.model.Account;
import com.example.clichepiggybankdb.model.Like;
import com.example.clichepiggybankdb.model.Role;
import com.example.clichepiggybankdb.model.Sanction;
import com.example.clichepiggybankdb.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class SanctionService {
    private final SanctionRepository sanctionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final LikeRepository likeRepository;

    public SanctionService(
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

    @Transactional
    public Sanction create(Sanction newSanction, UUID inquirerId) throws UserNotFoundException, InquirerNotFoundException, ForbiddenException {
        if(!userRepository.existsById(newSanction.getReceiverGuid())) {
            throw new UserNotFoundException(newSanction.getReceiverGuid());
        }
        if(!userRepository.existsById(inquirerId)) {
            throw new InquirerNotFoundException(inquirerId);
        }

        List<Role> inquirerRoles = roleRepository.findByUserGuid(inquirerId);

        if (!UserService.isAdmin(inquirerRoles) && !UserService.isUser(inquirerRoles)) {
            throw new ForbiddenException(inquirerId);
        }

        newSanction.setReporterGuid(inquirerId);
        newSanction.setLikesNumber(0);
        newSanction.setLiked(Boolean.FALSE);
        newSanction.setDatetime(new Date());

        if (accountRepository.findByOwnerGuid(newSanction.getReceiverGuid()).isEmpty()) {
            Account toBeSaved = new Account();
            toBeSaved.setOwnerGuid(newSanction.getReceiverGuid());
            toBeSaved.setBalance(0);
            accountRepository.save(toBeSaved);
        }
        Account toBeCharged = accountRepository.findByOwnerGuid(newSanction.getReceiverGuid()).getFirst();
        toBeCharged.setBalance(toBeCharged.getBalance() + newSanction.getAmount().amount());
        accountRepository.save(toBeCharged);
        return sanctionRepository.save(newSanction);
    }

    @Transactional
    public Page<Sanction> getPage( String sort,
                             String order,
                             Integer size,
                             Integer page,
                             UUID inquirerId) throws InquirerNotFoundException, ForbiddenException {
        if(!userRepository.existsById(inquirerId)) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!UserService.isUser(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        String sanitizedSort;
        if (sort.equals("likes")) {
            sanitizedSort = "likesNumber";
        } else {
            sanitizedSort = "datetime";
        }
        Sort sanitizedSortOrder;
        if (order.toUpperCase(Locale.ROOT).equals("ASC")) {
            sanitizedSortOrder = Sort.by(sanitizedSort).ascending();
        } else {
            sanitizedSortOrder = Sort.by(sanitizedSort).descending();
        }

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                sanitizedSortOrder
        );
        Page<Sanction> sanctionsPage = sanctionRepository.findAll(pageRequest);
        sanctionsPage.getContent().forEach(s -> {
            if (likeRepository.existsByLikedGuidAndLikerGuid(s.getId(), inquirerId)) {
                s.setLiked(Boolean.TRUE);
            } else {
                s.setLiked(Boolean.FALSE);
            }
        });
        return sanctionsPage;
    }

    @Transactional
    public Sanction get(UUID sanctionId, UUID inquirerId) throws SanctionNotFoundException, InquirerNotFoundException, ForbiddenException {
        if(!userRepository.existsById(inquirerId)) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!UserService.isUser(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        Sanction sanction = sanctionRepository.findById(sanctionId)
                .orElseThrow(() -> new SanctionNotFoundException(sanctionId));
        if (likeRepository.existsByLikedGuidAndLikerGuid(sanction.getId(), inquirerId)) {
            sanction.setLiked(Boolean.TRUE);
        } else {
            sanction.setLiked(Boolean.FALSE);
        }
        return sanction;
    }

    @Transactional
    public Sanction like(UUID sanctionId, UUID inquirerId) throws SanctionNotFoundException, InquirerNotFoundException, ForbiddenException, AlreadyLikedException {
        Sanction updatableSanction = sanctionRepository.findById(sanctionId)
                .orElseThrow(() -> new SanctionNotFoundException(sanctionId));
        if(!userRepository.existsById(inquirerId)) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!UserService.isUser(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        if(likeRepository.existsByLikedGuidAndLikerGuid(sanctionId, inquirerId)) {
            throw new AlreadyLikedException(sanctionId, inquirerId);
        }
        Like newLike = new Like();
        newLike.setLikedGuid(sanctionId);
        newLike.setLikerGuid(inquirerId);
        updatableSanction.setLikesNumber(updatableSanction.getLikesNumber() + 1);
        sanctionRepository.save(updatableSanction);
        likeRepository.save(newLike);
        updatableSanction.setLiked(Boolean.TRUE);
        return updatableSanction;
    }

    @Transactional
    public Sanction unlike(UUID sanctionId, UUID inquirerId) throws SanctionNotFoundException, InquirerNotFoundException, ForbiddenException {
        Sanction updatableSanction = sanctionRepository.findById(sanctionId)
                .orElseThrow(() -> new SanctionNotFoundException(sanctionId));
        if(!userRepository.existsById(inquirerId)) {
            throw new InquirerNotFoundException(inquirerId);
        }
        if (!UserService.isUser(roleRepository.findByUserGuid(inquirerId))) {
            throw new ForbiddenException(inquirerId);
        }
        if(!likeRepository.existsByLikedGuidAndLikerGuid(sanctionId, inquirerId)) {
            throw new LikeNotFoundException(sanctionId, inquirerId);
        }
        Like deletableLike = likeRepository.findByLikedGuidAndLikerGuid(sanctionId, inquirerId).get();
        updatableSanction.setLikesNumber(updatableSanction.getLikesNumber() - 1);
        updatableSanction.setLiked(Boolean.FALSE);
        likeRepository.delete(deletableLike);
        return sanctionRepository.save(updatableSanction);
    }
}
