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
package net.minecrell.quartz.launch;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Throwables;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecrell.quartz.launch.mappings.Mappings;
import net.minecrell.quartz.launch.mappings.MappingsLoader;
import net.minecrell.quartz.launch.mappings.MappingsParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public final class QuartzTweaker implements ITweaker {

    public static final String MAIN = "net.minecraft.server.MinecraftServer";
    public static final String MAIN_PATH = "net/minecraft/server/MinecraftServer";
    public static final String MAIN_CLASS = MAIN_PATH + ".class";

    private static final Logger logger = LogManager.getLogger();

    private static Path serverJar;

    public static Path getServerJar() {
        return serverJar;
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        Path gamePath = gameDir != null ? gameDir.toPath() : Paths.get("");
        QuartzLaunch.initialize(gamePath);
        serverJar = gamePath.resolve("bin").resolve(QuartzMain.MINECRAFT_SERVER_LOCAL);
        checkState(Files.exists(serverJar), "Failed to load server JAR");
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader loader) {
        try {
            logger.info("Initializing Quartz...");

            byte[] mainClass = loader.getClassBytes(MAIN);

            // Check if we're running in development environment
            if (mainClass != null && MappingsParser.isMappingsClass(mainClass)) {
                // We need to replace the main class because it clashes with our mapping
                logger.info("Enabling main class transformer...");
                loader.registerTransformer("net.minecrell.quartz.launch.transformers.MainClassTransformer");
            } else {
                loader.clearNegativeEntries(Collections.singleton(MAIN_CLASS));
            }

            // Load Minecraft Server
            loader.addURL(serverJar.toUri().toURL());

            // Would rather not load these through Launchwrapper as they use native dependencies
            loader.addClassLoaderExclusion("com.sun.");
            loader.addClassLoaderExclusion("oshi.");
            loader.addClassLoaderExclusion("io.netty.");

            // Some libraries shouldn't get transformed, don't even give the chance for that
            loader.addTransformerExclusion("joptsimple.");

            // Minecraft Server libraries
            loader.addTransformerExclusion("com.google.gson.");
            loader.addTransformerExclusion("org.apache.commons.codec.");
            loader.addTransformerExclusion("org.apache.commons.io.");
            loader.addTransformerExclusion("org.apache.commons.lang3.");

            // SpongeAPI
            loader.addTransformerExclusion("com.flowpowered.math.");
            loader.addTransformerExclusion("org.slf4j.");

            // Guice
            loader.addTransformerExclusion("com.google.inject.");
            loader.addTransformerExclusion("org.aopalliance.");

            // configurate
            loader.addTransformerExclusion("ninja.leaping.configurate.");
            loader.addTransformerExclusion("com.googlecode.concurrentlinkedhashmap.");
            loader.addTransformerExclusion("com.typesafe.config.");

            // Mixins
            loader.addClassLoaderExclusion("org.spongepowered.tools.");
            loader.addTransformerExclusion("net.minecrell.quartz.mixin.");

            // The server GUI won't work if we don't exclude this: log4j2 wants to have this in the same classloader
            loader.addClassLoaderExclusion("com.mojang.util.QueueLogAppender");

            logger.info("Initializing Mappings...");
            Mappings mappings = MappingsLoader.load();
            Launch.blackboard.put("quartz.mappings", mappings);
            loader.registerTransformer("net.minecrell.quartz.launch.transformers.DeobfuscationTransformer");

            if (mappings.hasAccessMappings()) {
                logger.info("Enabling access transformer...");
                loader.registerTransformer("net.minecrell.quartz.launch.transformers.AccessTransformer");
            }

            logger.info("Initializing Mixin environment...");
            MixinBootstrap.init();
            MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
            env.addConfiguration("mixins.quartz.json");
            env.setSide(MixinEnvironment.Side.SERVER);
            loader.registerTransformer(MixinBootstrap.TRANSFORMER_CLASS);

            logger.info("Done! Starting Minecraft server...");
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String getLaunchTarget() {
        return MAIN;
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[]{"nogui"};
    }
}
