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

package net.transferproxy.plugin.classloader;

import net.transferproxy.api.plugin.Plugin;
import net.transferproxy.api.plugin.classloader.PluginClassloader;
import net.transferproxy.api.plugin.exception.PluginInitializationException;
import net.transferproxy.api.plugin.info.PluginInfo;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class PluginClassloaderImpl extends URLClassLoader implements PluginClassloader {

    private final PluginInfo info;
    private final Plugin plugin;
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

    public PluginClassloaderImpl(final @NotNull URL url, final @NotNull ClassLoader parent, final @NotNull PluginInfo info) {
        super(new URL[] {url}, parent);
        this.info = info;
        final String mainClass = info.getMain();
        final Class<?> main;
        try {
            main = Class.forName(Objects.requireNonNull(mainClass, "Main class must not be null"), true, this);
        } catch (final ClassNotFoundException e) {
            throw new PluginInitializationException("Failed to find main class '" + mainClass + '\'', e);
        }
        final Class<? extends Plugin> pluginClass;
        try {
            pluginClass = main.asSubclass(Plugin.class);
        } catch (final ClassCastException e) {
            throw new PluginInitializationException('\'' + mainClass + "' does not implements Plugin", e);
        }

        try {
            this.plugin = pluginClass.getDeclaredConstructor().newInstance();
        } catch (final NoSuchMethodException e) {
            throw new PluginInitializationException("Failed to find a public constructor for '" + mainClass + '\'', e);
        } catch (final InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new PluginInitializationException("Invalid plugin main class '" + mainClass + '\'', e);
        }
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        Class<?> clazz = this.classes.get(name);
        if (clazz == null) {
            clazz = super.findClass(name);
            this.classes.put(name, clazz);
        }
        return clazz;
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
        final URL url = this.findResource(name);
        if (url != null) {
            try {
                return url.openStream();
            } catch (final IOException e) {
                return null;
            }
        }
        return super.getResourceAsStream(name);
    }

    @Override
    public PluginInfo getInfo() {
        return this.info;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

}