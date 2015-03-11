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

package net.minecrell.quartz.status;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.status.ServerStatusResponse;
import net.minecrell.quartz.Quartz;
import org.spongepowered.api.Game;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.server.StatusPingEvent;
import org.spongepowered.api.status.StatusClient;
import org.spongepowered.api.status.StatusResponse;

import java.net.InetSocketAddress;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public final class QuartzStatusResponse {

    private QuartzStatusResponse() {
    }

    @Nullable
    public static ServerStatusResponse post(MinecraftServer server, StatusClient client) {
        return call(create(server), client);
    }

    @Nullable
    public static ServerStatusResponse postLegacy(MinecraftServer server, InetSocketAddress address, MinecraftVersion version,
            @Nullable InetSocketAddress virtualHost) {
        ServerStatusResponse response = create(server);
        response.setVersion(
                new ServerStatusResponse.Version(response.getVersion().getName(), Byte.MAX_VALUE));
        response = call(response, new QuartzLegacyStatusClient(address, version, virtualHost));
        if (response != null && response.getPlayers() == null) {
            response.setPlayers(new ServerStatusResponse.Players(-1, 0));
        }
        return response;
    }

    @Nullable
    private static ServerStatusResponse call(ServerStatusResponse response, StatusClient client) {
        Game game = Quartz.instance.getGame();
        if (!game.getEventManager().post(SpongeEventFactory.createStatusPing(game, client, (StatusPingEvent.Response) response))) {
            return response;
        } else {
            return null;
        }
    }

    public static ServerStatusResponse create(MinecraftServer server) {
        return clone(server.getStatusResponse());
    }

    private static ServerStatusResponse clone(ServerStatusResponse original) {
        ServerStatusResponse clone = new ServerStatusResponse();
        clone.setDescription(original.getDescription());
        if (original.getFavicon() != null) {
            ((StatusPingEvent.Response) clone).setFavicon(((StatusResponse) original).getFavicon().get());
        }

        clone.setPlayers(clone(original.getPlayers()));
        clone.setVersion(original.getVersion());
        return clone;
    }

    private static ServerStatusResponse.Players clone(ServerStatusResponse.Players original) {
        ServerStatusResponse.Players clone = new ServerStatusResponse.Players(original.getMax(), original.getOnline());
        clone.setPlayers(original.getPlayers());
        return clone;
    }

    private static String getFirstLine(String s) {
        int i = s.indexOf('\n');
        return i == -1 ? s : s.substring(0, i);
    }

    public static String getMotd(ServerStatusResponse response) {
        // TODO: ((StatusResponse) response).getDescription().toLegacy()
        return getFirstLine(response.getDescription().toUnformattedText());
    }

    private static final Pattern STRIP_FORMATTING = Pattern.compile("(?i)§[0-9A-FK-OR]?");

    public static String getUnformattedMotd(ServerStatusResponse response) {
        return getFirstLine(STRIP_FORMATTING.matcher(response.getDescription().toUnformattedText()).replaceAll(""));
    }

}
