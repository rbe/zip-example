package eu.artofcoding.example.zip.truezip;

import de.schlichtherle.truezip.crypto.raes.RaesKeyException;
import de.schlichtherle.truezip.crypto.raes.RaesParameters;
import de.schlichtherle.truezip.crypto.raes.Type0RaesParameters;
import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.fs.FsController;
import de.schlichtherle.truezip.fs.FsDriverProvider;
import de.schlichtherle.truezip.fs.FsModel;
import de.schlichtherle.truezip.fs.archive.zip.raes.SafeZipRaesDriver;
import de.schlichtherle.truezip.key.sl.KeyManagerLocator;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;

public class RaesDetector {

    /**
     * Returns a new archive detector which uses the given password for all
     * RAES encrypted ZIP files with the given list of suffixes.
     * <p/>
     * When used for encryption, the AES key strength will be set to 256 bits.
     * <p/>
     * A protective copy of the given password char array is made.
     * It's recommended to overwrite the parameter array with any non-password
     * data after calling this method.
     * @param delegate the file system driver provider to decorate.
     * @param suffixes A list of file name suffixes which shall identify
     *                 prospective archive files.
     *                 This must not be {@code null} and must not be empty.
     * @param password the password char array to be copied for internal use.
     * @return A new archive detector which uses the given password for all
     *         RAES encrypted ZIP files with the given list of suffixes.
     */
    public static TArchiveDetector createDetector(FsDriverProvider delegate, String suffixes, char[] password) {
        return new TArchiveDetector(delegate, suffixes, new CustomZipRaesDriver(password));
    }

    private static final class CustomZipRaesDriver extends SafeZipRaesDriver {

        final RaesParameters param;

        private CustomZipRaesDriver(char[] password) {
            super(IOPoolLocator.SINGLETON, KeyManagerLocator.SINGLETON);
            param = new CustomRaesParameters(password);
        }

        @Override
        protected RaesParameters raesParameters(FsModel model) {
            // If you need the URI of the particular archive file, then call
            // model.getMountPoint().toUri().
            // If you need a more user friendly form of this URI, then call
            // model.getMountPoint().toHierarchicalUri().
            // Let's not use the key manager but instead our custom parameters.
            return param;
        }

        @Override
        public <M extends FsModel> FsController<M> decorate(FsController<M> controller) {
            // This is a minor improvement: The default implementation decorates
            // the default file system controller chain with a package private
            // file system controller which uses the key manager to keep track
            // of the encryption parameters.
            // Because we are not using the key manager, we don't need this
            // special purpose file system controller and can simply return the
            // given file system controller chain instead.
            return controller;
        }
    }

    private static final class CustomRaesParameters implements Type0RaesParameters {

        final char[] password;

        private CustomRaesParameters(final char[] password) {
            this.password = password.clone();
        }

        @Override
        public char[] getWritePassword() throws RaesKeyException {
            return password.clone();
        }

        @Override
        public char[] getReadPassword(boolean invalid) throws RaesKeyException {
            if (invalid) {
                throw new RaesKeyException("Invalid password!");
            }
            return password.clone();
        }

        @Override
        public KeyStrength getKeyStrength() throws RaesKeyException {
            return KeyStrength.BITS_256;
        }

        @Override
        public void setKeyStrength(KeyStrength keyStrength) throws RaesKeyException {
            // We have been using only 128 bits to create archive entries.
            if (KeyStrength.BITS_256 != keyStrength) {
                throw new IllegalStateException("Key strength != 256");
            }
        }

    }

}
