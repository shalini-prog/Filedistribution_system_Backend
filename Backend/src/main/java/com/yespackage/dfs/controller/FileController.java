package com.yespackage.dfs.controller;

import com.yespackage.dfs.entity.*;
import com.yespackage.dfs.repository.*;
import com.yespackage.dfs.service.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.stream.Collectors;
import com.yespackage.dfs.entity.FileShare;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final FileChunkService fileChunkService;
    private final FileChunkRepository fileChunkRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FileShareRepository fileShareRepository;
    private final FileDownloadRepository fileDownloadRepository;

    public FileController(FileService fileService,
                          FileChunkService fileChunkService,
                          FileChunkRepository fileChunkRepository,
                          UserRepository userRepository,
                          FileRepository fileRepository,
                          FileShareRepository fileShareRepository,
                          FileDownloadRepository fileDownloadRepository) {
        this.fileService = fileService;
        this.fileChunkService = fileChunkService;
        this.fileChunkRepository = fileChunkRepository;
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.fileShareRepository = fileShareRepository;
        this.fileDownloadRepository = fileDownloadRepository;
    }

    // -------------------- Upload --------------------
    @PostMapping("/upload-chunked")
    public ResponseEntity<?> uploadFileChunked(
            @RequestParam("file") MultipartFile file) {

        try {
            User user = getAuthenticatedUser();
            FileEntity savedFile =
                    fileService.saveFile(file, user.getId());

            int chunkSizeKB = 1024;

            fileChunkService.splitFileIntoChunks(
                    savedFile,
                    file,
                    chunkSizeKB
            );

            return ResponseEntity.ok("File uploaded with replication.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Chunked upload failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/shared-by-me")
    public List<FileShare> getSharedByMe(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow();

        return fileShareRepository.findBySharedBy(user.getId());
    }

    // -------------------- Delete --------------------
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId) {
        try {
            User user = getAuthenticatedUser();
            FileEntity fileEntity = fileService.getFileById(fileId);

            if (!fileEntity.getUploadedBy().equals(user.getId())) {
                return ResponseEntity.status(403).body("You can only delete your own files.");
            }

            // Delete chunks
            List<FileChunk> chunks = fileChunkService.getChunksByFileId(fileId);
            for (FileChunk chunk : chunks) {
                File f = new File(chunk.getChunkPath());
                if (f.exists()) f.delete();
                fileChunkRepository.delete(chunk);
            }

            // Delete file entity
            fileRepository.delete(fileEntity);

            return ResponseEntity.ok("File deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to delete file: " + e.getMessage());
        }
    }

    // -------------------- Download --------------------
    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable Long fileId) {
        try {
            User user = getAuthenticatedUser();
            FileEntity fileEntity = fileService.getFileById(fileId);

            boolean isOwner = fileEntity.getUploadedBy().equals(user.getId());
            boolean isShared = fileShareRepository.findByFileId(fileId)
                    .stream()
                    .anyMatch(fs -> fs.getSharedWith().equals(user.getId()));

            if (!isOwner && !isShared) {
                return ResponseEntity.status(403).body("You are not authorized to download this file.");
            }

            // Reconstruct file
            List<FileChunk> chunks = fileChunkService.getChunksByFileId(fileId);
            File reconstructedFile =
                    fileChunkService.reconstructFile(fileId, fileEntity.getFilename());
            if (!reconstructedFile.exists()) return ResponseEntity.status(404).body("File not found");

            // Log download
            FileDownload downloadRecord = new FileDownload(fileId, user.getId());
            fileDownloadRepository.save(downloadRecord);

            InputStreamResource resource = new InputStreamResource(new FileInputStream(reconstructedFile));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getFilename() + "\"")
                    .contentLength(reconstructedFile.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("File download failed: " + e.getMessage());
        }
    }

    // -------------------- Share --------------------
    @PostMapping("/share/{fileId}")
    public ResponseEntity<?> shareFile(@PathVariable Long fileId, @RequestParam String recipientUsernameOrEmail) {
        try {
            User user = getAuthenticatedUser();
            FileEntity fileEntity = fileService.getFileById(fileId);

            if (!fileEntity.getUploadedBy().equals(user.getId())) {
                return ResponseEntity.status(403).body("Only the owner can share this file.");
            }

            User recipient = userRepository.findByUsername(recipientUsernameOrEmail)
                    .orElseGet(() -> userRepository.findByEmail(recipientUsernameOrEmail)
                            .orElseThrow(() -> new RuntimeException("Recipient not found")));

            FileShare fileShare = new FileShare(
                    fileId,
                    user.getId(),              // sharedBy (owner)
                    recipient.getId()          // sharedWith
            );
            fileShareRepository.save(fileShare);

            return ResponseEntity.ok("File shared with " + recipient.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to share file: " + e.getMessage());
        }
    }

    // -------------------- List --------------------
    @GetMapping("/list")
    public ResponseEntity<?> listFiles() {
        try {
            User user = getAuthenticatedUser();

            // Uploaded
            List<FileEntity> uploaded = fileRepository.findAll()
                    .stream()
                    .filter(f -> f.getUploadedBy().equals(user.getId()))
                    .collect(Collectors.toList());

            // Shared with me (safe)
            List<FileEntity> sharedWithMe = fileShareRepository.findAll()
                    .stream()
                    .filter(fs -> fs.getSharedWith().equals(user.getId()))
                    .map(fs -> fileService.getFileById(fs.getFileId()))
                    .filter(f -> f != null)   // 🔥 important
                    .collect(Collectors.toList());

            // Downloaded by me (safe)
            List<FileEntity> downloaded = fileDownloadRepository.findByUserId(user.getId())
                    .stream()
                    .map(fd -> fileService.getFileById(fd.getFileId()))
                    .filter(f -> f != null)   // 🔥 important
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new FileListResponse(uploaded, sharedWithMe, downloaded));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to list files: " + e.getMessage());
        }
    }

    // -------------------- Helper --------------------
    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public static class FileListResponse {
        public List<FileEntity> uploadedFiles;
        public List<FileEntity> sharedFiles;
        public List<FileEntity> downloadedFiles;

        public FileListResponse(List<FileEntity> uploadedFiles, List<FileEntity> sharedFiles, List<FileEntity> downloadedFiles) {
            this.uploadedFiles = uploadedFiles;
            this.sharedFiles = sharedFiles;
            this.downloadedFiles = downloadedFiles;
        }
    }
}