/*
 * MIT License
 *
 * Copyright (c) 2025 Yvan Mazy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.transferproxy.api.util;

import org.jetbrains.annotations.NotNull;

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
     * @param path the path of image (like png, jpg, ...)
     *
     * @return the built image in Base64
     *
     * @throws IOException I/O exception
     */
    public static String createImage(final @NotNull Path path) throws IOException {
        return createImage(ImageIO.read(Files.newInputStream(path)));
    }

    /**
     * Build an image (Base64 format) from {@link BufferedImage}
     *
     * @param image the {@link BufferedImage} base
     *
     * @return the built image in Base64
     *
     * @throws IOException I/O exception
     */
    public static String createImage(final @NotNull BufferedImage image) throws IOException {
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