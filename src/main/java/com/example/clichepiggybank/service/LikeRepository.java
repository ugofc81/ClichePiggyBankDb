package com.example.clichepiggybank.service;

import com.example.clichepiggybank.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {
    // Inherits: save(), findAll(), findById(), deleteById() automatically!
    List<Like> findByLikedGuid(UUID likedGuid);
    java.util.Optional<Like> findByLikedGuidAndLikerGuid(UUID likedGuid, UUID likerGuid);

}