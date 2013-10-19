package eu.artofcoding.example.zip.truezip;

import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.nio.file.TPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ZipFacade {

    public static void archive(Path file, Path archive) throws IOException {
        // Setup the file operands.
        TPath src = new TPath(file);
        TPath dst = new TPath(archive);

        // TFile  doesn't do path name completion, so we do it manually in
        // order to emulate the behavior of many copy command line utilities.
        if (TConfig.get().isLenient() && dst.isArchive() || Files.isDirectory(dst)) {
            dst = dst.resolve(src.getFileName());
        }

        // If TConfig.get().setLenient(false) is never called in your
        // application, then you might as well shorten this to...
        /*if (dst.isArchive() || Files.isDirectory(dst))
            dst = dst.resolve(src.getFileName());*/

        // If you don't like path name completion for non-existent files which
        // just look like archive files according to their path name,
        // then you might even shorten this to...
        /*if (Files.isDirectory(dst))
            dst = dst.resolve(src.getFileName());*/

        // Perform a non-recursive archive copy.
        Files.copy(src, dst, COPY_ATTRIBUTES, REPLACE_EXISTING);

        // Okay, if this example should demonstrate a recursive copy, I'ld back
        // out to the TrueZIP File* API as follows because a recursive copy
        // with the NIO.2 API is way too complex for this most prominent use
        // case.
        //TFile.cp_rp(src.toFile(), dst.toFile(), TArchiveDetector.NULL);
    }

    public static void unarchive(Path archive, Path fileInArchive, Path destination) throws IOException {
        TPath src;
        // Downcast or wrap
        if (archive instanceof TPath) {
            src = (TPath) archive;
        } else {
            src = new TPath(archive);
        }
        // Resolve file in archive
        TPath resolve = src.resolve(fileInArchive);
        // Copy replace existing destination file
        Files.copy(resolve, destination, COPY_ATTRIBUTES, REPLACE_EXISTING);
    }

}
