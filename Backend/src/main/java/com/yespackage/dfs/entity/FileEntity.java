package com.yespackage.dfs.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    private String checksum;

    private Long uploadedBy; // user ID

    private LocalDateTime uploadDate;

    // Constructors
    public FileEntity() {}

    public FileEntity(String filename, String checksum, Long uploadedBy) {
        this.filename = filename;
        this.checksum = checksum;
        this.uploadedBy = uploadedBy;
        this.uploadDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public Long getUploadedBy() { return uploadedBy; } // ✅ this replaces getUserId()
    public void setUploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
}