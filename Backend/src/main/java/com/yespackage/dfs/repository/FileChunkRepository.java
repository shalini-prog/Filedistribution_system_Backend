package com.yespackage.dfs.repository;

import com.yespackage.dfs.entity.FileChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileChunkRepository extends JpaRepository<FileChunk, Long> {

    List<FileChunk> findByFileIdOrderByChunkIndex(Long fileId);

    List<FileChunk> findByFileIdAndChunkIndex(Long fileId, int chunkIndex);
}