package com.tutorassist.material.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageServiceTest {

    @TempDir
    Path storageRoot;

    private LocalFileStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new LocalFileStorageService(storageRoot.toString());
        storageService.initialize();
    }

    @Test
    void uploadsReadsUpdatesAndDeletesFile() throws Exception {
        MockMultipartFile upload = new MockMultipartFile(
                "file", "lesson.pdf", "application/pdf", "first".getBytes());

        String objectKey = storageService.uploadFile(upload, "materials");
        assertThat(objectKey).startsWith("materials/").endsWith(".pdf");
        assertThat(storageService.getFile(objectKey).readAllBytes()).isEqualTo("first".getBytes());

        storageService.saveFile(objectKey, "updated".getBytes());
        assertThat(storageService.getFile(objectKey).readAllBytes()).isEqualTo("updated".getBytes());

        storageService.deleteFile(objectKey);
        assertThat(Files.exists(storageRoot.resolve(objectKey))).isFalse();
    }

    @Test
    void rejectsPathTraversal() {
        assertThatThrownBy(() -> storageService.getFile("../../outside.txt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("非法文件路径");
    }
}
