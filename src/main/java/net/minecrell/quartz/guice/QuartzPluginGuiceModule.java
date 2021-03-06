/*
 * Quartz
 * Copyright (c) 2015, Minecrell <https://github.com/Minecrell>
 *
 * Based on Sponge and SpongeAPI, licensed under the MIT License (MIT).
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
 * Copyright (c) contributors
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
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import net.minecrell.quartz.plugin.QuartzPluginContainer;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.config.ConfigDir;
import org.spongepowered.api.service.config.DefaultConfig;

import java.io.File;
import java.nio.file.Path;

import javax.inject.Inject;

public class QuartzPluginGuiceModule extends AbstractModule {

    private static final ConfigDir privateConfigDir = new ConfigDirAnnotation(false);
    private static final DefaultConfig sharedConfigFile = new ConfigFileAnnotation(true);
    private static final DefaultConfig privateConfigFile = new ConfigFileAnnotation(false);

    private final QuartzPluginContainer container;

    public QuartzPluginGuiceModule(QuartzPluginContainer container) {
        this.container = container;
    }

    @Override
    protected void configure() {
        bind(PluginContainer.class).toInstance(container);
        bind(Logger.class).toInstance(container.getLogger());

        bind(Path.class).annotatedWith(privateConfigDir).toProvider(PrivateConfigDirProvider.class);
        bind(File.class).annotatedWith(privateConfigDir).toProvider(FilePrivateConfigDirProvider.class);
        bind(File.class).annotatedWith(sharedConfigFile).toProvider(SharedConfigFileProvider.class);
        bind(File.class).annotatedWith(privateConfigFile).toProvider(PrivateConfigFileProvider.class);
        bind(new TypeLiteral<ConfigurationLoader<CommentedConfigurationNode>>() {
        }).annotatedWith(sharedConfigFile)
                .toProvider(SharedHoconConfigProvider.class);
        bind(new TypeLiteral<ConfigurationLoader<CommentedConfigurationNode>>() {
        }).annotatedWith(privateConfigFile)
                .toProvider(PrivateHoconConfigProvider.class);
    }

    private static class PrivateConfigDirProvider implements Provider<Path> {

        private final PluginContainer container;
        private final Path configDir;

        @Inject
        private PrivateConfigDirProvider(PluginContainer container, @ConfigDir(sharedRoot = true) Path configDir) {
            this.container = container;
            this.configDir = configDir;
        }

        @Override
        public Path get() {
            return configDir.resolve(container.getId());
        }
    }

    private static class FilePrivateConfigDirProvider implements Provider<File> {

        private final Path configDir;

        @Inject
        private FilePrivateConfigDirProvider(PluginContainer container, @ConfigDir(sharedRoot = false) Path configDir) {
            this.configDir = configDir;
        }

        @Override
        public File get() {
            return configDir.toFile();
        }

    }

    private static class PrivateConfigFileProvider implements Provider<File> {

        private final PluginContainer container;
        private final Path configDir;

        @Inject
        private PrivateConfigFileProvider(PluginContainer container, @ConfigDir(sharedRoot = false) Path configDir) {
            this.container = container;
            this.configDir = configDir;
        }

        @Override
        public File get() {
            return configDir.resolve(container.getId() + ".conf").toFile();
        }

    }

    private static class SharedConfigFileProvider implements Provider<File> {

        private final PluginContainer container;
        private final Path configDir;

        @Inject
        private SharedConfigFileProvider(PluginContainer container, @ConfigDir(sharedRoot = true) Path configDir) {
            this.container = container;
            this.configDir = configDir;
        }

        @Override
        public File get() {
            return configDir.resolve(container.getId() + ".conf").toFile();
        }

    }

    private static class SharedHoconConfigProvider implements Provider<ConfigurationLoader<CommentedConfigurationNode>> {

        private final File configFile;

        @Inject
        private SharedHoconConfigProvider(@DefaultConfig(sharedRoot = true) File configFile) {
            this.configFile = configFile;
        }

        @Override
        public ConfigurationLoader<CommentedConfigurationNode> get() {
            return HoconConfigurationLoader.builder().setFile(this.configFile).build();
        }

    }

    private static class PrivateHoconConfigProvider implements Provider<ConfigurationLoader<CommentedConfigurationNode>> {

        private final File configFile;

        @Inject
        private PrivateHoconConfigProvider(@DefaultConfig(sharedRoot = false) File configFile) {
            this.configFile = configFile;
        }

        @Override
        public ConfigurationLoader<CommentedConfigurationNode> get() {
            return HoconConfigurationLoader.builder().setFile(this.configFile).build();
        }

    }

}
