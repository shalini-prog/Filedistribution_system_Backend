package com.yespackage.dfs.repository;

import com.yespackage.dfs.entity.FileShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileShareRepository extends JpaRepository<FileShare, Long> {

    List<FileShare> findByFileId(Long fileId);
    List<FileShare> findBySharedWith(Long userId);
    List<FileShare> findBySharedBy(Long userId);
}