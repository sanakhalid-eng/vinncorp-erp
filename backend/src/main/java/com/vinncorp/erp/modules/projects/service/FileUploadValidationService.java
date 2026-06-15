package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.shared.exception.FileUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Service
@Slf4j
public class FileUploadValidationService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );

    private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "text/csv"
    );

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final long MAX_DOCUMENT_SIZE = 10 * 1024 * 1024;

    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] GIF_MAGIC = {0x47, 0x49, 0x46, 0x38};
    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46};

    public void validateImage(MultipartFile file) {
        validateFileType(file, ALLOWED_IMAGE_TYPES, "image");
        validateFileSize(file, MAX_IMAGE_SIZE, "Image");
        validateMagicBytes(file, "image");
    }

    public void validateDocument(MultipartFile file) {
        validateFileType(file, ALLOWED_DOCUMENT_TYPES, "document");
        validateFileSize(file, MAX_DOCUMENT_SIZE, "Document");
        validateMagicBytes(file, "document");
    }

    public void validateFile(MultipartFile file, Set<String> allowedTypes, long maxSize, String category) {
        validateFileType(file, allowedTypes, category);
        validateFileSize(file, maxSize, category);
    }

    private void validateFileType(MultipartFile file, Set<String> allowedTypes, String category) {
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new FileUploadException(
                    "Invalid " + category + " file type: " + contentType + ". Allowed: " + allowedTypes,
                    com.vinncorp.erp.shared.exception.ErrorCode.INVALID_FILE_TYPE
            );
        }
    }

    private void validateFileSize(MultipartFile file, long maxSize, String category) {
        if (file.getSize() > maxSize) {
            throw new FileUploadException(
                    category + " file size exceeds maximum allowed size of " + (maxSize / 1024 / 1024) + "MB",
                    com.vinncorp.erp.shared.exception.ErrorCode.FILE_TOO_LARGE
            );
        }
    }

    private void validateMagicBytes(MultipartFile file, String category) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int bytesRead = is.read(header);

            if (bytesRead < 2) {
                throw new FileUploadException("File is empty or corrupted", com.vinncorp.erp.shared.exception.ErrorCode.FILE_EMPTY);
            }

            if (category.equals("image")) {
                if (matchesMagicBytes(header, bytesRead, JPEG_MAGIC) &&
                        matchesMagicBytes(header, bytesRead, PNG_MAGIC) &&
                        matchesMagicBytes(header, bytesRead, GIF_MAGIC)) {
                    throw new FileUploadException("File content does not match image type", com.vinncorp.erp.shared.exception.ErrorCode.INVALID_FILE_TYPE);
                }
            } else if (category.equals("document")) {
                if (file.getContentType() != null && file.getContentType().contains("pdf")) {
                    if (matchesMagicBytes(header, bytesRead, PDF_MAGIC)) {
                        throw new FileUploadException("File content does not match PDF type", com.vinncorp.erp.shared.exception.ErrorCode.INVALID_FILE_TYPE);
                    }
                }
            }
        } catch (IOException e) {
            throw new FileUploadException("Failed to validate file content", com.vinncorp.erp.shared.exception.ErrorCode.FILE_UPLOAD_FAILED, e);
        }
    }

    private boolean matchesMagicBytes(byte[] header, int bytesRead, byte[] magic) {
        if (bytesRead < magic.length) return true;
        for (int i = 0; i < magic.length; i++) {
            if (header[i] != magic[i]) return true;
        }
        return false;
    }
}



