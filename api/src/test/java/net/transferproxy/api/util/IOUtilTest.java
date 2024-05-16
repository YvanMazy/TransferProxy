/*
 * MIT License
 *
 * Copyright (c) 2024 Yvan Mazy
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.stubbing.OngoingStubbing;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IOUtilTest {

    @Test
    void testCreateImageWithCorrectSize() {
        mockWrite(ignored -> {
            final String result = assertDoesNotThrow(() -> IOUtil.createImage(mockImage(64, 64)));
            assertTrue(result.startsWith("data:image/png;base64,"));
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {63, 65})
    void testCreateImageWithIncorrectSize(final int size) {
        mockWrite(ignored -> {
            assertThrows(IOException.class, () -> IOUtil.createImage(mockImage(64, size)));
            assertThrows(IOException.class, () -> IOUtil.createImage(mockImage(size, 64)));
        });
    }

    @Test
    void testCreateImageOutputLength() throws IOException {
        final BufferedImage realImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);

        final String result = IOUtil.createImage(realImage);
        assertTrue(result.length() <= Short.MAX_VALUE);
    }

    @Test
    void testIOExceptionDuringImageProcessing() {
        mockWrite(mockedStatic -> assertThrows(IOException.class, () -> IOUtil.createImage(mockImage(64, 64))),
                () -> whenWrite().thenThrow(IOException.class));
    }

    private static BufferedImage mockImage(final int width, final int height) {
        final BufferedImage image = mock(BufferedImage.class);
        when(image.getWidth()).thenReturn(width);
        when(image.getHeight()).thenReturn(height);
        return image;
    }

    private static void mockWrite(final Consumer<MockedStatic<ImageIO>> consumer) {
        mockWrite(consumer, () -> whenWrite().thenReturn(true));
    }

    private static void mockWrite(final Consumer<MockedStatic<ImageIO>> consumer, final Executable executable) {
        try (final MockedStatic<ImageIO> mockedStatic = mockStatic(ImageIO.class)) {
            executable.execute();
            consumer.accept(mockedStatic);
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static OngoingStubbing<Boolean> whenWrite() throws IOException {
        return when(ImageIO.write(any(BufferedImage.class), anyString(), any(ByteArrayOutputStream.class)));
    }

}