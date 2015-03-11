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
package net.minecraft.server.status;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.chat.ChatComponent;
import net.minecrell.quartz.mappings.Constructor;
import net.minecrell.quartz.mappings.Mapping;

@Mapping("jt")
public abstract class ServerStatusResponse {

    @Mapping("a")
    private ChatComponent description;

    @Mapping("b")
    private Players players;

    @Mapping("c")
    private Version version;

    @Mapping("d")
    private String favicon;

    @Constructor
    public static ServerStatusResponse create() {
        return null;
    }

    @Mapping("a")
    public abstract ChatComponent getDescription();

    @Mapping("a")
    public abstract void setDescription(ChatComponent description);

    @Mapping("b")
    public abstract Players getPlayers();

    @Mapping("a")
    public abstract void setPlayers(Players players);

    @Mapping("c")
    public abstract Version getVersion();

    @Mapping("a")
    public abstract void setVersion(Version version);

    @Mapping("d")
    public abstract String getFavicon();

    @Mapping("a")
    public abstract void setFavicon(String favicon);

    @Mapping("jt$a")
    public static abstract class Players {

        @Mapping("a")
        private final int max = 0;

        @Mapping("b")
        private final int online = 0;

        @Mapping("c")
        private GameProfile[] players;

        @Constructor
        public static Players create(int max, int online) {
            return null;
        }

        @Mapping("a")
        public abstract int getMax();

        @Mapping("b")
        public abstract int getOnline();

        @Mapping("c")
        public abstract GameProfile[] getPlayers();

        @Mapping("a")
        public abstract void setPlayers(GameProfile[] players);

    }

    @Mapping("jt$c")
    public static abstract class Version {

        @Mapping("a")
        private final String name = null;

        @Mapping("b")
        private final int protocol = 0;

        @Constructor
        public static Version create(String name, int protocol) {
            return null;
        }

        @Mapping("a")
        public abstract String getName();

        @Mapping("b")
        public abstract int getProtocol();

    }

}
