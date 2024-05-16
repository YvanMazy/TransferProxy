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

package net.transferproxy.plugin;

import net.transferproxy.api.plugin.Plugin;
import net.transferproxy.api.plugin.PluginManager;
import net.transferproxy.api.plugin.classloader.PluginClassloader;
import net.transferproxy.api.plugin.info.PluginInfo;
import net.transferproxy.api.util.ResourceUtil;
import net.transferproxy.plugin.classloader.PluginClassloaderImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class PluginManagerImpl implements PluginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginManagerImpl.class);
    private static final Path ROOT_PATH = Path.of("plugins");

    private final List<Plugin> plugins = new CopyOnWriteArrayList<>();

    @Override
    public void start() {
        if (Files.notExists(ROOT_PATH)) {
            try {
                Files.createDirectories(ROOT_PATH);
            } catch (final IOException e) {
                LOGGER.error("Plugins directory cannot be created", e);
                return;
            }
        }
        try (final Stream<Path> stream = Files.list(ROOT_PATH)
                .filter(Files::isRegularFile)
                .filter(f -> f.getFileName().toString().endsWith(".jar"))) {
            for (final Path path : stream.toList()) {
                this.loadPlugin(path);
            }
        } catch (final IOException e) {
            LOGGER.error("Plugins cannot be loaded", e);
        }
        if (this.plugins.isEmpty()) {
            LOGGER.warn("No plugins were found, so there are no redirection rules defined.");
        }
    }

    @Override
    public void stop() {
        for (final Plugin plugin : this.plugins) {
            plugin.onDisable();
            final PluginInfo info = plugin.getInfo();
            if (info == null) {
                continue;
            }
            LOGGER.info("Plugin {} {} from {} are now disabled", info.getName(), info.getVersion(), info.getAuthor());
        }
    }

    @SuppressWarnings("resource")
    private void loadPlugin(final Path path) {
        try {
            final URL jarUrl = path.toUri().toURL();

            final PluginInfo pluginInfo = this.loadPluginInfo(path);
            if (pluginInfo == null) {
                LOGGER.warn("Invalid plugin.yml in plugin '{}'", path.getFileName());
                return;
            }

            final PluginClassloader classloader = new PluginClassloaderImpl(jarUrl, this.getClass().getClassLoader(), pluginInfo);
            final Plugin plugin = classloader.getPlugin();

            this.plugins.add(plugin);

            plugin.onEnable();
            LOGGER.info("Plugin {} {} from {} are now enabled", pluginInfo.getName(), pluginInfo.getVersion(), pluginInfo.getAuthor());
        } catch (final Exception e) {
            LOGGER.error("Plugin '{}' cannot be loaded", path.getFileName(), e);
        }
    }

    private PluginInfo loadPluginInfo(final Path path) throws IOException {
        try (final JarFile jarFile = new JarFile(path.toFile())) {
            final JarEntry entry = jarFile.getJarEntry("plugin.yml");
            if (entry != null) {
                try (final InputStream input = jarFile.getInputStream(entry)) {
                    return ResourceUtil.getDefaultYamlMapper().readValue(input, PluginInfo.class);
                }
            }
        }
        throw new IllegalArgumentException("Main class not found in plugin.yml");
    }

    @Override
    public @NotNull Collection<Plugin> getPlugins() {
        return List.copyOf(this.plugins);
    }

}