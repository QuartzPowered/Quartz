/*
 * Ice
 * Copyright (c) 2015, Minecrell <https://github.com/Minecrell>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.minecrell.ice;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.minecrell.ice.guice.IceGuiceModule;
import net.minecrell.ice.plugin.IcePluginManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Ice {

    private static final Logger logger = LogManager.getLogger();

    public static Logger getLogger() {
        return logger;
    }

    private static final Injector injector = Guice.createInjector(new IceGuiceModule());

    public static Injector getInjector() {
        return injector;
    }

    public static Ice getInstance() {
        return injector.getInstance(Ice.class);
    }

    private Game game;
    private Path gameDir;
    private Path pluginsDir;

    public void initialize(Path gameDir, List<String> args) {
        this.gameDir = requireNonNull(gameDir, "gameDir");
        this.pluginsDir = gameDir.resolve("plugins");
    }

    public Game getGame() {
        return game;
    }

    public Path getGameDirectory() {
        return gameDir;
    }

    public Path getPluginsDirectory() {
        return pluginsDir;
    }

    public void launch() throws Exception {
        getLogger().info("Launching Ice...");

        this.game = injector.getInstance(Game.class);

        if (Files.notExists(gameDir) || Files.notExists(pluginsDir)) {
            Files.createDirectories(pluginsDir);
        }

        ((IcePluginManager) game.getPluginManager()).loadPlugins();

        // TODO
        getLogger().info("Done! Starting Minecraft server...");
    }

}
