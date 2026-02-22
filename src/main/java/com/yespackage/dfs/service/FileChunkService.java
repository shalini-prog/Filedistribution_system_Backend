package com.yespackage.dfs.service;

import com.yespackage.dfs.entity.FileChunk;
import com.yespackage.dfs.entity.FileEntity;
import com.yespackage.dfs.repository.FileChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

@Service
public class FileChunkService {

    private final FileChunkRepository fileChunkRepository;

    // Simulated storage nodes
    private final List<String> storageNodes = Arrays.asList(
            "C:/dfs_nodes/node1",
            "C:/dfs_nodes/node2",
            "C:/dfs_nodes/node3"
    );

    private final int replicationFactor = 2; // each chunk stored in 2 nodes

    public FileChunkService(FileChunkRepository fileChunkRepository) {
        this.fileChunkRepository = fileChunkRepository;
    }

    public List<FileChunk> getChunksByFileId(Long fileId) {
        return fileChunkRepository.findByFileIdOrderByChunkIndex(fileId);
    }

    // -------------------- SPLIT WITH REPLICATION --------------------
    public void splitFileIntoChunks(FileEntity fileEntity,
                                    MultipartFile file,
                                    int chunkSizeKB) throws Exception {

        InputStream inputStream = file.getInputStream();
        byte[] buffer = new byte[chunkSizeKB * 1024];
        int bytesRead;
        int chunkIndex = 0;

        while ((bytesRead = inputStream.read(buffer)) != -1) {

            // Randomly pick nodes for replication
            Collections.shuffle(storageNodes);
            List<String> selectedNodes =
                    storageNodes.subList(0, replicationFactor);

            for (String nodePath : selectedNodes) {

                File nodeDir = new File(nodePath);
                if (!nodeDir.exists()) nodeDir.mkdirs();

                String chunkFilename =
                        fileEntity.getId() + "_chunk_" + chunkIndex;

                File chunkFile = new File(nodeDir, chunkFilename);

                try (FileOutputStream fos = new FileOutputStream(chunkFile)) {
                    fos.write(buffer, 0, bytesRead);
                }

                FileChunk chunk = new FileChunk(
                        fileEntity.getId(),
                        chunkIndex,
                        nodePath,
                        chunkFile.getAbsolutePath(),
                        bytesRead
                );

                fileChunkRepository.save(chunk);
            }

            chunkIndex++;
        }

        inputStream.close();
    }

    // -------------------- RECONSTRUCT USING FIRST AVAILABLE REPLICA --------------------
    public File reconstructFile(Long fileId, String originalFilename) throws Exception {

        File outputDir = new File("C:/dfs_reconstructed/");
        if (!outputDir.exists()) outputDir.mkdirs();

        File reconstructedFile =
                new File(outputDir, originalFilename);

        try (FileOutputStream fos = new FileOutputStream(reconstructedFile)) {

            List<FileChunk> allChunks =
                    fileChunkRepository.findByFileIdOrderByChunkIndex(fileId);

            Map<Integer, List<FileChunk>> grouped =
                    new HashMap<>();

            for (FileChunk chunk : allChunks) {
                grouped.computeIfAbsent(chunk.getChunkIndex(),
                        k -> new ArrayList<>()).add(chunk);
            }

            for (int i = 0; i < grouped.size(); i++) {

                List<FileChunk> replicas = grouped.get(i);
                boolean written = false;

                for (FileChunk replica : replicas) {
                    File chunkFile =
                            new File(replica.getChunkPath());

                    if (chunkFile.exists()) {
                        byte[] data =
                                java.nio.file.Files.readAllBytes(chunkFile.toPath());
                        fos.write(data);
                        written = true;
                        break;
                    }
                }

                if (!written) {
                    throw new RuntimeException("Chunk missing: " + i);
                }
            }
        }

        return reconstructedFile;
    }
}