package com.odysseusinc.athena.service.writer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FileHelperTest {

    @TempDir
    Path store;

    @Test
    void publishesOnlyCompletedPartialArchive() throws Exception {

        FileHelper helper = new FileHelper();
        ReflectionTestUtils.setField(helper, "fileStorePath", store.toString());
        byte[] archive = {1, 2, 3};
        String partialZipPath = helper.getPartialZipPath("bundle", "attempt-1");
        Files.write(Path.of(partialZipPath), archive);

        helper.publishZip("bundle", partialZipPath);

        assertArrayEquals(archive, Files.readAllBytes(Path.of(helper.getZipPath("bundle"))));
        assertFalse(Files.exists(Path.of(partialZipPath)));
    }
}
