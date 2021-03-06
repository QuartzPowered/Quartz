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

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.LegacyPingHandler;
import net.minecraft.server.network.NetworkSystem;
import net.minecraft.server.status.ServerStatusResponse;
import net.minecrell.quartz.status.QuartzLegacyMinecraftVersion;
import net.minecrell.quartz.status.QuartzStatusResponse;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.net.InetSocketAddress;

import javax.annotation.Nullable;

@Mixin(LegacyPingHandler.class)
public abstract class MixinLegacyPingHandler extends ChannelInboundHandlerAdapter {

    @Shadow
    private static Logger logger;

    @Shadow
    private NetworkSystem system;

    @Nullable private ByteBuf buf;

    @Shadow
    abstract void writeAndFlush(ChannelHandlerContext ctx, ByteBuf data);

    @Shadow
    abstract ByteBuf writeString(String string);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.buf = ctx.alloc().buffer();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if (this.buf != null) {
            this.buf.release();
            this.buf = null;
        }
    }

    @Override
    @Overwrite
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf m = (ByteBuf) msg;
        this.buf.writeBytes(m);
        m.release();

        this.buf.markReaderIndex();
        boolean result = false;

        try {
            result = readLegacy(ctx, this.buf);
        } finally {
            this.buf.resetReaderIndex();
            if (!result) {
                ByteBuf buf = this.buf;
                this.buf = null;

                ctx.pipeline().remove("legacy_query");
                ctx.fireChannelRead(buf);
            }
        }
    }

    private boolean readLegacy(ChannelHandlerContext ctx, ByteBuf buf) {
        if (buf.readUnsignedByte() != 0xFE) {
            return false;
        }

        MinecraftServer server = this.system.getServer();
        InetSocketAddress client = (InetSocketAddress) ctx.channel().remoteAddress();
        ServerStatusResponse response;

        int i = buf.readableBytes();
        switch (i) {
            case 0:
                logger.debug("Ping: (<=1.3) from {}:{}", client.getAddress(), client.getPort());

                response = QuartzStatusResponse.postLegacy(server, client, QuartzLegacyMinecraftVersion.V1_3, null);
                if (response != null) {
                    this.writeResponse(ctx, String.format("%s§%d§%d",
                            QuartzStatusResponse.getUnformattedMotd(response),
                            response.getPlayers().getOnline(),
                            response.getPlayers().getMax()));
                } else {
                    ctx.close();
                }

                break;
            case 1:
                if (buf.readUnsignedByte() != 0x01) {
                    return false;
                }

                logger.debug("Ping: (1.4-1.5) from {}:{}", client.getAddress(), client.getPort());

                response = QuartzStatusResponse.postLegacy(server, client, QuartzLegacyMinecraftVersion.V1_5, null);
                if (response != null) {
                    this.writeResponse(ctx, String.format("§1\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d",
                            response.getVersion().getProtocol(),
                            response.getVersion().getName(),
                            QuartzStatusResponse.getMotd(response),
                            response.getPlayers().getOnline(),
                            response.getPlayers().getMax()));
                } else {
                    ctx.close();
                }

                break;
            default:
                if (buf.readUnsignedByte() != 0x01 || buf.readUnsignedByte() != 0xFA) {
                    return false;
                }
                if (!buf.isReadable(2)) {
                    break;
                }
                short length = buf.readShort();
                if (!buf.isReadable(length * 2)) {
                    break;
                }
                if (!buf.readBytes(length * 2).toString(Charsets.UTF_16BE).equals("MC|PingHost")) {
                    return false;
                }
                if (!buf.isReadable(2)) {
                    break;
                }
                length = buf.readShort();
                if (!buf.isReadable(length)) {
                    break;
                }

                int protocol = buf.readUnsignedByte();
                length = buf.readShort();
                String host = buf.readBytes(length * 2).toString(Charsets.UTF_16BE);
                int port = buf.readInt();

                logger.debug("Ping: (1.6) from {}:{}", client.getAddress(), client.getPort());

                response =
                        QuartzStatusResponse.postLegacy(server, client, new QuartzLegacyMinecraftVersion(QuartzLegacyMinecraftVersion.V1_6, protocol),
                                InetSocketAddress.createUnresolved(host, port));
                if (response != null) {
                    this.writeResponse(ctx, String.format("§1\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d",
                            response.getVersion().getProtocol(),
                            response.getVersion().getName(),
                            QuartzStatusResponse.getMotd(response),
                            response.getPlayers().getOnline(),
                            response.getPlayers().getMax()));
                } else {
                    ctx.close();
                }

                break;
        }

        return true;
    }

    private void writeResponse(ChannelHandlerContext ctx, String response) {
        writeAndFlush(ctx, writeString(response));
    }


}
