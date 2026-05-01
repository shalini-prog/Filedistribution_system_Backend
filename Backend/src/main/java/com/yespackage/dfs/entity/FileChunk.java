package com.yespackage.dfs.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "file_chunks")
public class FileChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fileId;
    private int chunkIndex;

    // Which node this chunk copy belongs to
    private String nodeName;

    private String chunkPath;
    private long chunkSize;

    public FileChunk() {}

    public FileChunk(Long fileId, int chunkIndex, String nodeName,
                     String chunkPath, long chunkSize) {
        this.fileId = fileId;
        this.chunkIndex = chunkIndex;
        this.nodeName = nodeName;
        this.chunkPath = chunkPath;
        this.chunkSize = chunkSize;
    }

    public Long getId() { return id; }

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }

    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }

    public String getChunkPath() { return chunkPath; }
    public void setChunkPath(String chunkPath) { this.chunkPath = chunkPath; }

    public long getChunkSize() { return chunkSize; }
    public void setChunkSize(long chunkSize) { this.chunkSize = chunkSize; }
}