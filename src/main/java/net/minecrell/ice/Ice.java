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

import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import net.minecrell.ice.event.IceEventFactory;
import net.minecrell.ice.guice.IceGuiceModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.state.ConstructionEvent;
import org.spongepowered.api.event.state.InitializationEvent;
import org.spongepowered.api.event.state.LoadCompleteEvent;
import org.spongepowered.api.event.state.PostInitializationEvent;
import org.spongepowered.api.event.state.PreInitializationEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Ice {

    private static final Ice instance = new Ice();

    public static Ice getInstance() {
        return instance;
    }

    private static final Injector injector = Guice.createInjector(new IceGuiceModule(instance));

    public static Injector getInjector() {
        return injector;
    }

    private final Logger logger = LogManager.getLogger();

    private final Path gameDir;
    private final Path pluginsDir;

    private IceGame game;

    private Ice() {
        this.gameDir = IceLaunch.getGameDirectory();
        this.pluginsDir = gameDir.resolve("plugins");
    }

    public Game getGame() {
        return game;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getGameDirectory() {
        return gameDir;
    }

    public Path getPluginsDirectory() {
        return pluginsDir;
    }

    public void load() {
        try {
            logger.info("Loading Ice...");

            this.game = injector.getInstance(IceGame.class);

            if (Files.notExists(gameDir) || Files.notExists(pluginsDir)) {
                Files.createDirectories(pluginsDir);
            }

            getLogger().info("Loading plugins...");
            game.getPluginManager().loadPlugins();
            game.getEventManager().post(IceEventFactory.createStateEvent(ConstructionEvent.class, game));
            getLogger().info("Done! Initializing plugins...");
            game.getEventManager().post(IceEventFactory.createStateEvent(PreInitializationEvent.class, game));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public void initialize() {
        game.getEventManager().post(IceEventFactory.createStateEvent(InitializationEvent.class, game));
        game.getEventManager().post(IceEventFactory.createStateEvent(PostInitializationEvent.class, game));
        getLogger().info("Successfully loaded and initialized plugins.");

        game.getEventManager().post(IceEventFactory.createStateEvent(LoadCompleteEvent.class, game));
    }

}
