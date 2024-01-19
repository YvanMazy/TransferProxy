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