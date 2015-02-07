package net.minecrell.quartz;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.File;
import java.util.List;

public final class QuartzTweaker implements ITweaker {

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {

    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader loader) {
        loader.addClassLoaderExclusion("org.apache.");
        loader.addClassLoaderExclusion("com.google.common.");
        loader.addClassLoaderExclusion("com.mojang.util.QueueLogAppender");

        MixinBootstrap.init();
        MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
        env.addConfiguration("mixins.quartz.json");
        env.setSide(MixinEnvironment.Side.SERVER);
        loader.registerTransformer(MixinBootstrap.TRANSFORMER_CLASS);
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
