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

package net.minecrell.ice.launch;

import com.google.common.base.Throwables;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecrell.ice.Ice;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public final class IceTweaker implements ITweaker {

    private final Ice ice = Ice.getInjector().getInstance(Ice.class);

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        ice.initialize(gameDir != null ? gameDir.toPath() : Paths.get(""), args != null ? args : Collections.emptyList());
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader loader) {
        Ice.getLogger().info("Initializing Ice...");

        loader.addClassLoaderExclusion("org.apache.");
        loader.addClassLoaderExclusion("io.netty.");
        loader.addClassLoaderExclusion("gnu.");
        loader.addClassLoaderExclusion("com.google.");
        loader.addClassLoaderExclusion("joptsimple.");
        loader.addClassLoaderExclusion("com.mojang.util.QueueLogAppender");
        loader.addClassLoaderExclusion("org.spongepowered.tools.");

        // Check if we're running in deobfuscated environment already
        if (isObfuscated()) {
            Launch.blackboard.put("ice.deobf-srg", Paths.get("bin", "deobf.srg.gz"));
            loader.registerTransformer("net.minecrell.ice.launch.transformers.DeobfuscationTransformer");
        }

        Launch.blackboard.put("ice.at", "ice_at.cfg");
        loader.registerTransformer("net.minecrell.ice.launch.transformers.AccessTransformer");

        MixinBootstrap.init();
        MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
        env.addConfiguration("mixins.ice.json");
        env.setSide(MixinEnvironment.Side.SERVER);
        loader.registerTransformer(MixinBootstrap.TRANSFORMER_CLASS);

        try {
            ice.launch();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static boolean isObfuscated() {
        try {
            byte[] bytes = Launch.classLoader.getClassBytes("net.minecraft.world.World");
            if (bytes != null) {
                Ice.getLogger().log(Level.INFO, "We're in a deobfuscated environment!");
                return false;
            }
        } catch (IOException ignored) {
        }

        return true;
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
