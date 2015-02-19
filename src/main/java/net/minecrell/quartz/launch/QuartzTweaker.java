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

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public final class QuartzTweaker implements ITweaker {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        QuartzLaunch.initialize(gameDir != null ? gameDir.toPath() : Paths.get(""));
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader loader) {
        logger.info("Initializing Quartz...");

        loader.addClassLoaderExclusion("org.apache.");
        loader.addClassLoaderExclusion("io.netty.");
        loader.addClassLoaderExclusion("gnu.");
        loader.addClassLoaderExclusion("joptsimple.");
        loader.addClassLoaderExclusion("com.mojang.util.QueueLogAppender");
        loader.addClassLoaderExclusion("org.spongepowered.tools.");
        loader.addClassLoaderExclusion("net.minecrell.quartz.mixin.");

        // Check if we're running in deobfuscated environment already
        logger.info("Enabling runtime deobfuscation...");
        if (isObfuscated()) {
            Launch.blackboard.put("quartz.deobf-srg", Paths.get("bin", "deobf.srg.gz"));
            loader.registerTransformer("net.minecrell.quartz.launch.transformers.DeobfuscationTransformer");
            logger.info("Runtime deobfuscation is enabled.");
        } else {
            logger.info("Runtime deobfuscation is disabled - Quartz was loaded in a deobfuscated environment.");
        }

        logger.info("Enabling access transformer...");
        Launch.blackboard.put("quartz.at", "quartz_at.cfg");
        loader.registerTransformer("net.minecrell.quartz.launch.transformers.AccessTransformer");

        logger.info("Initializing Mixin environment...");
        MixinBootstrap.init();
        MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
        env.addConfiguration("mixins.quartz.json");
        env.setSide(MixinEnvironment.Side.SERVER);
        loader.registerTransformer(MixinBootstrap.TRANSFORMER_CLASS);

        logger.info("Done! Starting Minecraft server...");
    }

    private static boolean isObfuscated() {
        try {
            return Launch.classLoader.getClassBytes("net.minecraft.world.World") == null;
        } catch (IOException ignored) {
            return true;
        }
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.server.MinecraftServer";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[]{"nogui"};
    }
}
