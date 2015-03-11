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
package net.minecrell.quartz.mixin.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetworkManager;
import net.minecraft.server.network.NetworkStatusHandler;
import net.minecraft.server.network.handler.StatusHandler;
import net.minecraft.server.network.packet.status.PacketC00StatusRequest;
import net.minecraft.server.network.packet.status.PacketS00StatusResponse;
import net.minecraft.server.status.ServerStatusResponse;
import net.minecrell.quartz.status.QuartzStatusClient;
import net.minecrell.quartz.status.QuartzStatusResponse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NetworkStatusHandler.class)
public abstract class MixinNetworkStatusHandler implements StatusHandler {

    @Shadow
    private MinecraftServer server;

    @Shadow
    private NetworkManager manager;

    @Override @Overwrite
    public void handleStatusRequest(PacketC00StatusRequest packet) {
        // Clone the response
        ServerStatusResponse response = QuartzStatusResponse.post(server, new QuartzStatusClient(manager));
        if (response != null) {
            this.manager.sendPacket(new PacketS00StatusResponse(response));
        } else {
            this.manager.disconnect(null);
        }
    }

}
