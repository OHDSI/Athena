package com.odysseusinc.athena.glue;

import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.v5history.VocabularyReleaseVersionRepository;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.service.writer.FileHelper;
import com.odysseusinc.athena.util.CDMVersion;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.function.Function;

@Slf4j
public class AthenaSteps {

    private static final List<Integer> TEST_VOCABULARIES = Arrays.asList(18, 125, 21, 78, 71, 72, 66, 2, 127, 1, 0, 90, 70, 34);

    @Autowired
    private World world;

    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private VocabularyReleaseVersionRepository vocabularyReleaseVersionRepository;

    @Autowired
    private FileHelper fileHelper;

    @Autowired
    @Qualifier("dataSourceAthenaV5History")
    protected DataSource v5HistoryDataSource;


    @Then("user import vocabulary from the {string} schema")
    public void userImportVocabulary(String schema) {
        importNewVersion("public", schema);
    }
//
//    @When("user generate bundle")
//    public void userGenerateBundle() throws IOException {
//        List<Integer> list = Arrays.asList(18, 125, 21, 78, 71, 72, 66, 2, 127, 1, 0, 90, 70, 34);
//        new ArrayList<>();
//        AthenaUser currentUser = new AthenaUser(1L);
//        DownloadBundle downloadBundle = vocabularyService.saveBundle("test", list, currentUser, CDMVersion.V5, 20200523, true, 20200521);
//        vocabularyService.saveContent(downloadBundle, currentUser);
//        String zipPath = fileHelper.getZipPath(downloadBundle.getUuid());
//        System.out.println(zipPath);
//
//        Path tempDir = Files.createTempDirectory(UUID.randomUUID().toString());
//        List<String> unzip = unzip(zipPath, tempDir.toString());
//        Assertions.assertEquals(10, unzip.size());
//
//    }

    @When("user generates a {int} version bundle")
    public void userGeneratesAVersionBundleIncludeFilesWithRecordCounts(Integer version) throws IOException {

        AthenaUser user = new AthenaUser(1L); //TODO DEV this is not correct, the authorization should be done
        String bundleName = String.format("%s-%s-%s", "Bundle", version, UUID.randomUUID());
        DownloadBundle bundle = vocabularyService.saveBundle(
                bundleName, TEST_VOCABULARIES,
                user, CDMVersion.V5, version, false, null);
        vocabularyService.saveContent(bundle, user);
        String zipPath = fileHelper.getZipPath(bundle.getUuid());

        Path tempDir = Files.createTempDirectory(UUID.randomUUID().toString());
        List<FileInfo> unzip = unzip(zipPath, tempDir.toString(), FileInfo::fromPath);
        world.setCursor(() -> unzip);
    }

    @When("user inspects list of vocabulary release version")
    public void userInspectsListOfVocabularyReleaseVersion() {
        world.setCursor(() ->
                new ArrayList<>(vocabularyReleaseVersionRepository.findAll())
        );
    }

    public <T> List<T> unzip(String zipFilePath, String destDir, Function<String, T> fun) throws IOException {
        List<T> result = new ArrayList<>();
        try (ZipArchiveInputStream zipIn = new ZipArchiveInputStream(Files.newInputStream(Paths.get(zipFilePath)))) {
            ArchiveEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDir + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    extractFile(zipIn, filePath);
                    Optional.ofNullable(fun.apply(filePath)).ifPresent(result::add);
                }
                entry = zipIn.getNextEntry();
            }
        }
        return result;
    }

    public static void extractFile(InputStream input, String outputPath) throws IOException {
        try (OutputStream output = Files.newOutputStream(Paths.get(outputPath))) {
            IOUtils.copy(input, output);
        }
    }

    /**
     * This import_new_version is not directly used in Java code; instead, it is part of the update vocabulary release process.
     */
    public void importNewVersion(String target, String source) {

        try (
                Connection conn = v5HistoryDataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT import_new_version(?,?)")
        ) {
            stmt.setString(1, target);
            stmt.setString(2, source);
            stmt.execute();

        } catch (Exception e) {
            log.error("Error importing version: from {} to {}", source, target, e);
        }
    }

    @AllArgsConstructor
    @Getter
    public static class FileInfo {
        private final String name;
        private final String ext;
        private final Path path;
        private final long size;
        private final long rows;

        public static FileInfo fromPath(String pathString) {
            return fromPath(Paths.get(pathString));
        }

        public static FileInfo fromPath(Path path) {
            try {
                String fileName = path.getFileName().toString();
                String name = fileName.substring(0, fileName.lastIndexOf('.'));
                String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
                long size = Files.size(path);
                long lineCount = extension.equals("csv") ? Files.lines(path).count() : -1;
                return new FileInfo(name, extension, path, size, lineCount - 1);
            } catch (IOException e) {
                return null;
            }
        }
    }
}

