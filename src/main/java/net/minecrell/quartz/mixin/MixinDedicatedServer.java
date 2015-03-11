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

package net.minecrell.quartz.mixin;

import static net.minecraft.server.DedicatedServer.DEDICATED_SERVER;

import jline.console.completer.Completer;
import net.minecraft.server.DedicatedServer;
import net.minecraft.server.MinecraftServer;
import net.minecrell.quartz.Quartz;
import org.spongepowered.api.event.state.ServerAboutToStartEvent;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public abstract class MixinDedicatedServer extends MinecraftServer implements Completer {

    @Inject(method = "startServer", at = @At(value = "INVOKE_STRING", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V",
            args = {"ldc=Loading properties"}, shift = At.Shift.BY, by = -2, remap = false))
    public void onServerLoad(CallbackInfoReturnable<Boolean> ci) {
        Quartz.instance.load();
    }

    @Inject(method = "startServer", at = @At(value = "INVOKE", target = DEDICATED_SERVER + setManager, shift = At.Shift.BY, by = -7))
    public void onServerInitialize(CallbackInfoReturnable<Boolean> ci) {
        Quartz.instance.initialize();
    }

    @Inject(method = "startServer", at = @At(value = "INVOKE", target = DEDICATED_SERVER + loadWorlds, shift = At.Shift.BY, by = -24))
    public void onServerAboutToStart(CallbackInfoReturnable<Boolean> ci) {
        Quartz.instance.postState(ServerAboutToStartEvent.class);
    }

    @Inject(method = "startServer", at = @At(value = "INVOKE", target = DEDICATED_SERVER + loadWorlds, shift = At.Shift.AFTER))
    public void onServerStarting(CallbackInfoReturnable<Boolean> ci) {
        Quartz.instance.postState(ServerStartingEvent.class);
    }

}
