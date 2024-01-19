/*
 * MIT License
 *
 * Copyright (c) 2024 Darkkraft
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

package be.darkkraft.transferproxy.main;

import be.darkkraft.transferproxy.TransferProxyImpl;
import be.darkkraft.transferproxy.api.TransferProxy;
import be.darkkraft.transferproxy.api.configuration.ProxyConfiguration;
import be.darkkraft.transferproxy.api.configuration.yaml.YamlProxyConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final Path CONFIG_PATH = Path.of("config.yml");

    public static void main(final String[] args) {
        final ProxyConfiguration configuration;
        try {
            configuration = loadConfiguration();
        } catch (final IOException e) {
            LOGGER.error("Failed to load configuration", e);
            return;
        }
        if (configuration == null) {
            LOGGER.error("Failed to load configuration");
            return;
        }

        final TransferProxy proxy = new TransferProxyImpl(configuration);

        proxy.start();
    }

    private static @Nullable ProxyConfiguration loadConfiguration() throws IOException {
        if (Files.notExists(CONFIG_PATH)) {
            final InputStream stream = ProxyConfiguration.class.getClassLoader().getResourceAsStream(CONFIG_PATH.getFileName().toString());
            if (stream != null) {
                try (stream) {
                    Files.copy(stream, CONFIG_PATH);
                }
            } else {
                return null;
            }
        }

        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()).setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

        try (final BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            return mapper.readValue(reader, YamlProxyConfiguration.class);
        }
    }

}