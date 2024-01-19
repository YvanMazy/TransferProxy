package be.darkkraft.transferproxy.api.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public final class IOUtil {

    private IOUtil() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    /**
     * Build an image (Base64 format) from {@link BufferedImage}
     *
     * @param path The path of image (like png, jpg, ...)
     *
     * @return The built image in Base64
     *
     * @throws IOException I/O exception
     */
    public static String createImage(final Path path) throws IOException {
        return createImage(ImageIO.read(Files.newInputStream(path)));
    }

    /**
     * Build an image (Base64 format) from {@link BufferedImage}
     *
     * @param image The {@link BufferedImage} base
     *
     * @return The built image in Base64
     *
     * @throws IOException I/O exception
     */
    public static String createImage(final BufferedImage image) throws IOException {
        if (image.getWidth() != 64 || image.getHeight() != 64) {
            throw new IOException("Server icon must be exactly 64x64 pixels");
        }

        final byte[] bytes;
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", out);
            bytes = out.toByteArray();
        }

        final String encoded = "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        if (encoded.length() > Short.MAX_VALUE) {
            throw new IOException("Favicon file too large for server to process");
        }

        return encoded;
    }

}