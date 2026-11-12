package com.example.clichepiggybankdb.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"liked_guid", "liker_guid"})
)
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Automatically generates clean UUIDs!
    @Column(name = "id", length = 16)
    @JdbcTypeCode(SqlTypes.BINARY) // Forces Hibernate to use MySQL's BINARY(16)
    private UUID id;

    @Column(nullable = false, name = "liked_guid", length = 16)
    @JdbcTypeCode(SqlTypes.BINARY) // Forces Hibernate to use MySQL's BINARY(16)
    private UUID likedGuid;

    @Column(nullable = false, name = "liker_guid", length = 16)
    @JdbcTypeCode(SqlTypes.BINARY) // Forces Hibernate to use MySQL's BINARY(16)
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
