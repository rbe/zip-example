package eu.artofcoding.example.zip.truezip;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.sl.FsDriverLocator;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ZipFacadeTest {

    @Test
    public void testArchive() throws IOException {
        Path pomXml = Paths.get("pom.xml");
        // Create archive
        Path archive = Paths.get("pom.zip");
        ZipFacade.archive(pomXml, archive);
        // Unarchive
        Path fileInArchive = Paths.get("pom.xml");
        Path destination = Paths.get("pom-from-zip.xml");
        ZipFacade.unarchive(archive, fileInArchive, destination);
        // Cleanup
        TVFS.umount();
        // Check
        long test1FileSize = Files.size(pomXml);
        assertThat(Files.size(destination), is(test1FileSize));
        // Cleanup
        Files.delete(archive);
        Files.delete(destination);
    }

    @Test
    public void testArchiveRAES() throws IOException {
        Path pomXml = Paths.get("pom.xml");
        String RAES_ZIP_SUFFIX = "zip.raes";
        // Setup encryption
        TConfig config = TConfig.get();
        TArchiveDetector raesDetector = RaesDetector.createDetector(FsDriverLocator.SINGLETON, RAES_ZIP_SUFFIX, "password".toCharArray());
        config.setArchiveDetector(raesDetector);
        // Create archive
        Path archive = Paths.get("pom." + RAES_ZIP_SUFFIX);
        ZipFacade.archive(pomXml, archive);
        // Unarchive
        Path fileInArchive = Paths.get("pom.xml");
        Path destination = Paths.get("pom-from-raes-zip.xml");
        ZipFacade.unarchive(archive, fileInArchive, destination);
        // Cleanup
        TVFS.umount();
        // Check
        long test2FileSize = Files.size(pomXml);
        assertThat(Files.size(destination), is(test2FileSize));
        // Cleanup
        Files.delete(archive);
        Files.delete(destination);
    }

}
