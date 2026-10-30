package com.example.clichepiggybank.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "sanctions") // Maps this class to a "users" table in MySQL
public class Sanction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Automatically generates clean UUIDs!
    private UUID id;

    @Column(nullable = false, unique = false, name = "reporter_guid")
    private UUID reporterGuid;

    @Column(nullable = false, unique = false, name = "receiver_guid")
    private UUID receiverGuid;

    @Column(nullable = false, unique = false, name = "amount")
    private SanctionAmount amount;

    @Column(nullable = false, unique = false, name = "reason")
    private String reason;

    @Column(nullable = false, unique = false, name = "datetime")
    private Date datetime;

    @Column(nullable = false, unique = false, name = "likes_number")
    private Integer likesNumber;

    public Sanction() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getReporterGuid() {
        return reporterGuid;
    }

    public void setReporterGuid(UUID reporterGuid) {
        this.reporterGuid = reporterGuid;
    }

    public UUID getReceiverGuid() {
        return receiverGuid;
    }

    public void setReceiverGuid(UUID receiverGuid) {
        this.receiverGuid = receiverGuid;
    }

    public SanctionAmount getAmount() {
        return amount;
    }

    public void setAmount(SanctionAmount amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public Integer getLikesNumber() {
        return likesNumber;
    }

    public void setLikesNumber(Integer likesNumber) {
        this.likesNumber = likesNumber;
    }
}
