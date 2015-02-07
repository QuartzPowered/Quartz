package net.minecrell.quartz.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.net.Proxy;

@Mixin(DedicatedServer.class)
public abstract class MixinDedicatedServer extends MinecraftServer {

    public MixinDedicatedServer(Proxy p_i46053_1_, File p_i46053_2_) {
        super(p_i46053_1_, p_i46053_2_);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onStartServer(File workDir, CallbackInfo ci) {
        LogManager.getLogger().info("YOO QUARTZ");
    }
}
