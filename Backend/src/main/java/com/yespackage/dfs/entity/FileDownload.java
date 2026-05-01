package com.yespackage.dfs.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "file_downloads")
public class FileDownload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fileId;
    private Long userId; // Who downloaded
    private Long timestamp; // store epoch millis

    public FileDownload() {}

    public FileDownload(Long fileId, Long userId) {
        this.fileId = fileId;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}