package com.yespackage.dfs.repository;

import com.yespackage.dfs.entity.FileDownload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileDownloadRepository extends JpaRepository<FileDownload, Long> {
    List<FileDownload> findByUserId(Long userId);
}