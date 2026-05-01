package com.yespackage.dfs.service;

import com.yespackage.dfs.entity.FileEntity;
import com.yespackage.dfs.repository.FileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;

@Service
public class FileService {

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    // Save a new file
    public FileEntity saveFile(MultipartFile file, Long userId) throws Exception {
        String checksum = calculateChecksum(file.getInputStream());
        FileEntity fileEntity = new FileEntity(file.getOriginalFilename(), checksum, userId);
        return fileRepository.save(fileEntity);
    }

    // Get file by ID
    public FileEntity getFileById(Long id) {
        return fileRepository.findById(id).orElse(null);
    }
    
    public void delete(FileEntity fileEntity) {
        fileRepository.delete(fileEntity);
    }

    // SHA-256 checksum calculation
    private String calculateChecksum(InputStream inputStream) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytesBuffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
            digest.update(bytesBuffer, 0, bytesRead);
        }
        byte[] hashedBytes = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}