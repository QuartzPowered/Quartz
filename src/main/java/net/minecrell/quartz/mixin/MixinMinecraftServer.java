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

import static net.minecraft.server.MinecraftServer.MINECRAFT_SERVER;
import static net.minecraft.server.MinecraftServer.exit;
import static net.minecraft.server.MinecraftServer.loadFavicon;
import static net.minecraft.server.MinecraftServer.stop;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.chat.TextComponent;
import net.minecraft.server.command.CommandSender;
import net.minecrell.quartz.Quartz;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.state.ServerStartedEvent;
import org.spongepowered.api.event.state.ServerStoppedEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements Server, CommandSource, CommandSender {

    @Inject(method = "run", at = @At(value = "INVOKE", target = MINECRAFT_SERVER + loadFavicon, shift = At.Shift.AFTER))
    public void onServerStarted(CallbackInfo ci) {
        Quartz.instance.postState(ServerStartedEvent.class);
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = MINECRAFT_SERVER + stop, ordinal = 0, shift = At.Shift.BY, by = -8))
    public void onServerStopping(CallbackInfo ci) {
        Quartz.instance.postState(ServerStoppingEvent.class);
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = MINECRAFT_SERVER + exit))
    public void onServerStopped(CallbackInfo ci) {
        Quartz.instance.postState(ServerStoppedEvent.class);
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            sendMessage(TextComponent.create(message));
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

}
