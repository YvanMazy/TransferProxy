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

import be.yvanmazy.remotedminecraft.MinecraftHolder;
import be.yvanmazy.remotedminecraft.RemotedMinecraft;
import be.yvanmazy.remotedminecraft.auth.Auth;
import be.yvanmazy.remotedminecraft.config.ProcessConfiguration;
import be.yvanmazy.remotedminecraft.controller.MinecraftController;
import be.yvanmazy.remotedminecraft.controller.agent.AgentFileBuilder;
import be.yvanmazy.remotedminecraft.controller.exception.AgentConnectException;
import be.yvanmazy.remotedminecraft.controller.exception.AgentLoadingException;
import net.kyori.adventure.key.Key;
import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.configuration.ProxyConfiguration;
import net.transferproxy.api.event.EventType;
import net.transferproxy.api.event.listener.ReadyListener;
import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.api.util.PropertyHelper;
import net.transferproxy.test.agent.AgentMain;
import net.transferproxy.test.agent.TestAgent;
import net.transferproxy.test.agent.TestAgentImpl;
import net.transferproxy.test.util.ByteUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.abort;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JoinIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JoinIntegrationTest.class);

    private static final int AGENT_PORT = 1099;
    private static final String PLAYER_NAME = "TestPlayer";
    private static final int TEST_LOG_AMOUNT = 3;

    private static final Phaser PHASER = new Phaser(2);
    private static final AtomicBoolean SUCCESS = new AtomicBoolean();

    private static final Key AMOUNT_KEY = Key.key("transferproxy", "log_amount");

    private static Path cacheDirectory;
    private static String host;
    private static int hostPort;

    @TempDir
    private static Path tempDir;

    @BeforeAll
    static void beforeAll() {
        final String cacheDir = System.getProperty("library.cache.dir");
        if (cacheDir == null) {
            LOGGER.error("Property 'library.cache.dir' is not set!");
            abort("Property 'library.cache.dir' is not set!");
            return;
        }
        cacheDirectory = Path.of(cacheDir);
        if (!Files.isDirectory(cacheDirectory)) {
            try {
                Files.createDirectories(cacheDirectory);
            } catch (final IOException e) {
                LOGGER.error("Failed to create cache directory", e);
                abort("Failed to create cache directory");
                return;
            }
        }

        System.setProperty(PropertyHelper.BASE_DIRECTORY_KEY, tempDir.toString());

        net.transferproxy.main.Main.main(new String[0]);

        final TransferProxy proxy = TransferProxy.getInstance();

        final ProxyConfiguration.Network network = proxy.getConfiguration().getNetwork();
        host = network.getBindAddress();
        hostPort = network.getBindPort();

        proxy.getModuleManager().getEventManager().<ReadyListener>addListener(EventType.READY, JoinIntegrationTest::onReady);
    }

    @Test
    void testMultipleReconnectWithCookieCounterAndTransfer() {
        // Prepare the agent file
        final Path agentPath = new AgentFileBuilder(AgentMain.class).addClasses(TestAgent.class, TestAgentImpl.class)
                .addRemotedAgentClasses()
                .buildUnchecked(tempDir.resolve("agent.jar"));

        // Run a Minecraft client
        LOGGER.info("Starting Minecraft client...");
        final MinecraftHolder holder = RemotedMinecraft.run(ProcessConfiguration.newBuilder()
                .version("1.21.3")
                .authentication(Auth.byUsername(PLAYER_NAME))
                .processDirectory(cacheDirectory)
                .jvmAgentArg(agentPath.toString(), AGENT_PORT)
                .inheritIO(false)
                .build()).getReadyFuture().join();
        assumeTrue(holder.isStarted());

        // Connect agent
        LOGGER.info("Connecting agent...");
        final MinecraftController<TestAgent> controller = holder.newController();
        try {
            assumeTrue(controller.connect(TestAgent.ID, AGENT_PORT, 10L, TimeUnit.SECONDS));
        } catch (final AgentConnectException e) {
            LOGGER.error("Failed to connect to agent", e);
            abort("Failed to connect to agent");
            return;
        }

        // Connect the client to proxy
        LOGGER.info("Waiting for game to be ready...");
        final TestAgent agent;
        try {
            agent = controller.awaitReady();
        } catch (final AgentLoadingException | InterruptedException e) {
            LOGGER.error("Game failed to start", e);
            abort("Game failed to start");
            return;
        }

        // Connect to proxy
        LOGGER.info("Connecting client to proxy...");
        try {
            agent.connect(host, hostPort);
        } catch (final RemoteException e) {
            fail(e);
        }

        // Await proxy receive connection
        LOGGER.info("Waiting for proxy to receive connection...");
        for (int i = 0; i < TEST_LOG_AMOUNT; i++) {
            PHASER.arriveAndAwaitAdvance();
        }
        PHASER.arriveAndAwaitAdvance();
        assertTrue(SUCCESS.get());

        controller.process().destroy();

        if (controller.process().onExit().orTimeout(5, TimeUnit.SECONDS).thenApply(p -> false).exceptionally(t -> true).join()) {
            LOGGER.error("Failed to shutdown process");
        }
    }

    @AfterAll
    static void afterAll() {
        final TransferProxy instance = TransferProxy.getInstance();
        if (instance != null) {
            instance.stop();
        }
    }

    private static void onReady(final PlayerConnection connection) {
        connection.fetchCookie(AMOUNT_KEY)
                .thenApply(data -> data != null ? ByteUtil.toInt(data) : null)
                .orTimeout(3L, TimeUnit.SECONDS)
                .thenAccept(amount -> {
                    if (amount != null && amount == TEST_LOG_AMOUNT) {
                        SUCCESS.set(true);
                        PHASER.arriveAndAwaitAdvance();
                    } else {
                        PHASER.arriveAndAwaitAdvance();
                        connection.storeCookie(AMOUNT_KEY, ByteUtil.toByteArray(amount != null ? amount + 1 : 1));
                        connection.transfer(host, hostPort);
                    }
                });
    }

}