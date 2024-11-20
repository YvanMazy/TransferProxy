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

package net.transferproxy.main;

import net.transferproxy.TransferProxyImpl;
import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.configuration.ProxyConfiguration;
import net.transferproxy.api.configuration.yaml.YamlProxyConfiguration;
import net.transferproxy.api.util.PropertyHelper;
import net.transferproxy.api.util.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final Path DEFAULT_CONFIG_PATH = Path.of("./config.yml");

    public static void main(final String[] args) {
        final ProxyConfiguration configuration;
        try {
            final Path configPath = PropertyHelper.resolve("transferproxy.config.path", DEFAULT_CONFIG_PATH);
            configuration = ResourceUtil.copyAndReadYaml(configPath, YamlProxyConfiguration.class);
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

}