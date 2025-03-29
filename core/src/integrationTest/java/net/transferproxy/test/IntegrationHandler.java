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

package net.transferproxy.test;

import be.yvanmazy.remotedminecraft.auth.Auth;
import be.yvanmazy.remotedminecraft.testing.MinecraftTestStarter;
import be.yvanmazy.remotedminecraft.testing.StartedMinecraft;
import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.module.ModuleManager;
import net.transferproxy.api.util.PropertyHelper;
import net.transferproxy.module.ModuleManagerImpl;
import net.transferproxy.test.agent.AgentMain;
import net.transferproxy.test.agent.TestAgent;
import net.transferproxy.test.agent.TestAgentImpl;
import net.transferproxy.test.agent.callback.StatusResponseCallback;
import net.transferproxy.test.common.SimpleStatusResponse;
import net.transferproxy.test.util.DeleteAllFileVisitor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assumptions.abort;

public class IntegrationHandler implements TestExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationHandler.class);

    private static IntegrationHandler instance;

    public static final String PLAYER_NAME = "TestPlayer";

    private String minecraftVersion;
    private Path workDirectory;
    private Path cacheDirectory;

    private StartedMinecraft<TestAgent> minecraft;

    public IntegrationHandler() {
        if (instance != null) {
            throw new IllegalStateException("Only one instance of IntegrationHandler can be created");
        }
        instance = this;
    }

    @Override
    public void testPlanExecutionStarted(final TestPlan testPlan) {
        final String version = System.getProperty("minecraft.version");
        if (version == null) {
            LOGGER.error("Property 'minecraft.version' is not set!");
            abort("Property 'minecraft.version' is not set!");
        }
        this.minecraftVersion = version;
        LOGGER.info("Using Minecraft version: {}", this.minecraftVersion);

        final String cacheDir = System.getProperty("library.cache.dir");
        if (cacheDir == null) {
            LOGGER.error("Property 'library.cache.dir' is not set!");
            abort("Property 'library.cache.dir' is not set!");
        }

        this.cacheDirectory = Path.of(cacheDir);
        if (!Files.isDirectory(this.cacheDirectory)) {
            try {
                Files.createDirectories(this.cacheDirectory);
            } catch (final IOException e) {
                LOGGER.error("Failed to create cache directory", e);
                abort("Failed to create cache directory");
            }
        }

        try {
            this.workDirectory = Files.createTempDirectory("junit-tp-");
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        System.setProperty(PropertyHelper.BASE_DIRECTORY_KEY, this.workDirectory.toString());

        this.minecraft = this.launchMinecraft();

        this.launchTransferProxyServer();
    }

    @Override
    public void executionFinished(final TestIdentifier testIdentifier, final TestExecutionResult testExecutionResult) {
        if (!testIdentifier.isTest()) {
            return;
        }
        LOGGER.info("Cleaning up session after test {}...", testIdentifier.getDisplayName());
        // Disconnect Minecraft client
        if (this.minecraft != null) {
            final TestAgent agent = this.minecraft.agent();
            try {
                if (agent.isReady()) {
                    agent.disconnectServer();
                    agent.updateClientOptions(false);
                }
            } catch (final RemoteException e) {
                LOGGER.error("Failed to disconnect Minecraft client", e);
            }
        }

        // Reinitialize default modules
        final ModuleManager moduleManager = TransferProxy.getInstance().getModuleManager();
        if (moduleManager instanceof final ModuleManagerImpl impl) {
            impl.initializeDefaults(true);
        }
        LOGGER.info("Cleaning up session after test {} finished", testIdentifier.getDisplayName());
    }

    @Override
    public void testPlanExecutionFinished(final TestPlan testPlan) {
        if (this.minecraft != null) {
            this.minecraft.holder().destroy();
        }
        final TransferProxy instance = TransferProxy.getInstance();
        if (instance != null) {
            instance.stop();
        }
        if (this.workDirectory != null) {
            try {
                Files.walkFileTree(this.workDirectory, new DeleteAllFileVisitor());
            } catch (final IOException e) {
                LOGGER.error("Failed to delete work directory", e);
            }
        }
    }

    private void launchTransferProxyServer() {
        net.transferproxy.main.Main.main(new String[0]);
    }

    private StartedMinecraft<TestAgent> launchMinecraft() {
        return MinecraftTestStarter.<TestAgent>newStarterLocal()
                .agentMainClass(AgentMain.class)
                .applyAgentBuilder(builder -> builder.addClasses(TestAgent.class,
                        TestAgentImpl.class,
                        StatusResponseCallback.class,
                        SimpleStatusResponse.class,
                        SimpleStatusResponse.Players.class,
                        SimpleStatusResponse.Version.class,
                        SimpleStatusResponse.Players.SampleEntry.class))
                .agentPath(this.workDirectory.resolve("agent.jar"))
                .agentId(TestAgent.ID)
                .config(config -> config.version(this.minecraftVersion)
                        .authentication(Auth.byUsername(PLAYER_NAME))
                        .processDirectory(this.cacheDirectory)
                        .inheritIO(false))
                .start();
    }

    public static StartedMinecraft<TestAgent> getMinecraft() {
        return instance.minecraft;
    }

}