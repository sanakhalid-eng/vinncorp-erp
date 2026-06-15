package com.vinncorp.erp.shared.security;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface StorageProvider {

    String store(MultipartFile file, String subdirectory) throws IOException;

    Resource loadAsResource(String filename, String subdirectory);

    Path load(String filename, String subdirectory);

    void delete(String filename, String subdirectory);

    String getPublicUrl(String filename, String subdirectory);

    boolean exists(String filename, String subdirectory);
}

