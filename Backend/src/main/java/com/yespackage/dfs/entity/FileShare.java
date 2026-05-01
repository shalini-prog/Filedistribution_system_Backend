package com.yespackage.dfs.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_shares")
public class FileShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fileId; // Reference to FileEntity

    private Long sharedBy; // User ID of sender

    private Long sharedWith; // User ID of receiver

    private LocalDateTime sharedAt;

    public FileShare() {}

    public FileShare(Long fileId, Long sharedBy, Long sharedWith) {
        this.fileId = fileId;
        this.sharedBy = sharedBy;
        this.sharedWith = sharedWith;
        this.sharedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public Long getSharedBy() { return sharedBy; }
    public void setSharedBy(Long sharedBy) { this.sharedBy = sharedBy; }

    public Long getSharedWith() { return sharedWith; }
    public void setSharedWith(Long sharedWith) { this.sharedWith = sharedWith; }

    public LocalDateTime getSharedAt() { return sharedAt; }
    public void setSharedAt(LocalDateTime sharedAt) { this.sharedAt = sharedAt; }
}