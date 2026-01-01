/*
 * MIT License
 *
 * Copyright (c) 2026 Yvan Mazy
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
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class ResourceUtil {

    private static final ObjectMapper DEFAULT_MAPPER =
            YAMLMapper.builder().propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE).build();

    private ResourceUtil() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    /**
     * Copies a resource if the file does not exist
     *
     * @param path The file path
     *
     * @return true if the resource has been copied and written
     *
     * @throws IOException          if an I/O error occurs while copying the resource
     * @throws NullPointerException if any parameter is null
     * @apiNote This method uses {@link #copyResource(ClassLoader, Path, String)} using the file name as the resource name
     */
    public static boolean copyResource(final @NotNull ClassLoader classLoader, final @NotNull Path path) throws IOException {
        return copyResource(classLoader, path, path.getFileName().toString());
    }

    /**
     * Copies a resource if the file does not exist.
     *
     * @param path the file path
     *
     * @return true if the resource has been copied and written.
     *
     * @throws IOException          if an I/O error occurs while copying the resource
     * @throws NullPointerException if any parameter is null
     */
    public static boolean copyResource(final @NotNull ClassLoader classLoader,
                                       final @NotNull Path path,
                                       final @NotNull String resourceName) throws IOException {
        if (Files.notExists(Objects.requireNonNull(path, "path must not be null"))) {
            Objects.requireNonNull(classLoader, "classLoader must not be null");
            Objects.requireNonNull(resourceName, "resourceName must not be null");
            final Path parent = path.getParent();
            if (Files.notExists(parent)) {
                Files.createDirectories(parent);
            }
            final InputStream stream = classLoader.getResourceAsStream(resourceName);
            if (stream != null) {
                try (stream) {
                    Files.copy(stream, path);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Combines {@link #copyResource(ClassLoader, Path)} and {@link #readYaml(Path, Class)} to copy and read a YAML file
     *
     * @param path      the YAML file path
     * @param typeClass the class of the type to deserialize into
     *
     * @return deserialized instance of {@link T}
     *
     * @throws IOException          if an I/O error occurs while reading the file
     * @throws NullPointerException if any parameter is null
     */
    public static <T> T copyAndReadYaml(final @NotNull Path path, final @NotNull Class<T> typeClass) throws IOException {
        copyResource(typeClass.getClassLoader(), path);
        return readYaml(path, typeClass);
    }

    /**
     * Reads a YAML file and deserializes its content into an object of the specified type
     *
     * @param path      the YAML file path
     * @param typeClass the class of the type to deserialize into
     *
     * @return deserialized instance of {@link T}
     *
     * @throws IOException          if an I/O error occurs while reading the file
     * @throws NullPointerException if any parameter is null
     */
    public static <T> T readYaml(final @NotNull Path path, final @NotNull Class<T> typeClass) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(typeClass, "typeClass must not be null");
        try (final BufferedReader reader = Files.newBufferedReader(path)) {
            return DEFAULT_MAPPER.readValue(reader, typeClass);
        }
    }

    /**
     * Returns the default YAML {@link ObjectMapper} configured with Kebab Case property naming strategy
     * and ignoring unknown properties during deserialization.
     *
     * @return the default YAML {@link ObjectMapper}
     */
    public static @NotNull ObjectMapper getDefaultYamlMapper() {
        return DEFAULT_MAPPER;
    }

}