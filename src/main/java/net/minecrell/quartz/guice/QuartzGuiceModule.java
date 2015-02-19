/*
 * Quartz
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

package net.minecrell.quartz.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import net.minecrell.quartz.Quartz;
import net.minecrell.quartz.QuartzGame;
import net.minecrell.quartz.QuartzGameRegistry;
import net.minecrell.quartz.event.QuartzEventManager;
import net.minecrell.quartz.plugin.QuartzPluginManager;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.SimpleServiceManager;
import org.spongepowered.api.service.event.EventManager;

import java.io.File;
import java.nio.file.Path;

public class QuartzGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Quartz.class).toInstance(Quartz.getInstance());
        bind(Game.class).to(QuartzGame.class).in(Scopes.SINGLETON);
        bind(PluginManager.class).to(QuartzPluginManager.class).in(Scopes.SINGLETON);
        bind(EventManager.class).to(QuartzEventManager.class).in(Scopes.SINGLETON);
        bind(GameRegistry.class).to(QuartzGameRegistry.class).in(Scopes.SINGLETON);
        bind(ServiceManager.class).to(SimpleServiceManager.class).in(Scopes.SINGLETON);

        ConfigDirAnnotation sharedRoot = new ConfigDirAnnotation(true);
        Path configDir = Quartz.getInstance().getConfigDirectory();

        bind(Path.class).annotatedWith(sharedRoot).toInstance(configDir);
        bind(File.class).annotatedWith(sharedRoot).toInstance(configDir.toFile());
    }

}
