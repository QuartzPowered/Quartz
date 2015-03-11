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

import net.minecraft.server.network.NetworkManager;
import net.minecrell.quartz.QuartzMinecraftVersion;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager {

    @Shadow
    public abstract SocketAddress getRemoteAddress();

    private InetSocketAddress virtualHost;
    private MinecraftVersion version;

    public InetSocketAddress getAddress() {
        return (InetSocketAddress) getRemoteAddress();
    }

    public InetSocketAddress getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String host, int port) {
        this.virtualHost = InetSocketAddress.createUnresolved(host, port);
    }

    public MinecraftVersion getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = new QuartzMinecraftVersion(String.valueOf(version), version);
    }

}
