package com.example.clichepiggybankdb.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "likes") // Maps this class to a "users" table in MySQL
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Automatically generates clean UUIDs!
    private UUID id;

    @Column(nullable = false, unique = false, name = "liked_guid")
    private UUID likedGuid;

    @Column(nullable = false, unique = false, name = "liker_guid")
    private UUID likerGuid;

    public Like() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getLikedGuid() {
        return likedGuid;
    }

    public void setLikedGuid(UUID likedGuid) {
        this.likedGuid = likedGuid;
    }

    public UUID getLikerGuid() {
        return likerGuid;
    }

    public void setLikerGuid(UUID likerGuid) {
        this.likerGuid = likerGuid;
    }
}
