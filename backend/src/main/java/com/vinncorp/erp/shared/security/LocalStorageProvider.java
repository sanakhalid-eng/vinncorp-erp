package com.vinncorp.erp.shared.security;

import com.vinncorp.erp.shared.exception.FileUploadException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class LocalStorageProvider implements StorageProvider {

    private final Path uploadDir;

    public LocalStorageProvider(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new FileUploadException("Could not create upload directory: " + uploadDir, e);
        }
    }

    @Override
    public String store(MultipartFile file, String subdirectory) throws IOException {
        Path targetDir = uploadDir.resolve(subdirectory);
        Files.createDirectories(targetDir);

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID().toString() + extension;

        Path targetPath = targetDir.resolve(storedFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return storedFilename;
    }

    @Override
    public Resource loadAsResource(String filename, String subdirectory) {
        try {
            Path filePath = load(filename, subdirectory);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new FileUploadException("File not found or not readable: " + filename);
        } catch (MalformedURLException e) {
            throw new FileUploadException("File not found: " + filename, e);
        }
    }

    @Override
    public Path load(String filename, String subdirectory) {
        return uploadDir.resolve(subdirectory).resolve(filename).normalize();
    }

    @Override
    public void delete(String filename, String subdirectory) {
        try {
            Path filePath = load(filename, subdirectory);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new FileUploadException("Could not delete file: " + filename, e);
        }
    }

    @Override
    public String getPublicUrl(String filename, String subdirectory) {
        return "/api/files/" + subdirectory + "/" + filename;
    }

    @Override
    public boolean exists(String filename, String subdirectory) {
        return Files.exists(load(filename, subdirectory));
    }
}

