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

package be.darkkraft.transferproxy.api.plugin;

import be.darkkraft.transferproxy.api.TransferProxy;
import be.darkkraft.transferproxy.api.module.ModuleManager;
import be.darkkraft.transferproxy.api.plugin.classloader.PluginClassloader;
import be.darkkraft.transferproxy.api.plugin.info.PluginInfo;
import be.darkkraft.transferproxy.api.util.ResourceUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public interface Plugin {

    void onEnable();

    default void onDisable() {
    }

    default <T> T makeConfiguration(final @NotNull Class<T> confiurationClass) {
        return this.makeConfiguration("config.yml", confiurationClass);
    }

    default <T> T makeConfiguration(final @NotNull String fileName, final @NotNull Class<T> confiurationClass) {
        final Path path = Path.of("plugins").resolve(this.getInfo().getName()).resolve(fileName);
        try {
            return ResourceUtil.copyAndReadYaml(path, confiurationClass);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    default PluginInfo getInfo() {
        if (this.getClass().getClassLoader() instanceof final PluginClassloader classloader) {
            return classloader.getInfo();
        }
        throw new IllegalStateException("Invalid plugin classloader");
    }

    default ModuleManager getModuleManager() {
        return this.getProxy().getModuleManager();
    }

    default TransferProxy getProxy() {
        return TransferProxy.getInstance();
    }

}