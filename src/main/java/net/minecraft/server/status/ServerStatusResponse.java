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
import net.minecrell.quartz.launch.mappings.Mapping;

@Mapping("jt")
public class ServerStatusResponse {

    @Mapping("a")
    private ChatComponent description;

    @Mapping("b")
    private Players players;

    @Mapping("c")
    private Version version;

    @Mapping("d")
    private String favicon;

    public ServerStatusResponse() {
    }

    @Mapping("a")
    public ChatComponent getDescription() {
        return description;
    }

    @Mapping("a")
    public void setDescription(ChatComponent description) {
    }

    @Mapping("b")
    public Players getPlayers() {
        return null;
    }

    @Mapping("a")
    public void setPlayers(Players players) {
    }

    @Mapping("c")
    public Version getVersion() {
        return null;
    }

    @Mapping("a")
    public void setVersion(Version version) {
    }

    @Mapping("d")
    public String getFavicon() {
        return null;
    }

    @Mapping("a")
    public void setFavicon(String favicon) {
    }

    @Mapping("jt$a")
    public static class Players {

        @Mapping("a")
        private final int max = 0;

        @Mapping("b")
        private final int online = 0;

        @Mapping("c")
        private GameProfile[] players;

        public Players(int max, int online) {
        }

        @Mapping("a")
        public int getMax() {
            return 0;
        }

        @Mapping("b")
        public int getOnline() {
            return 0;
        }

        @Mapping("c")
        public GameProfile[] getPlayers() {
            return null;
        }

        @Mapping("a")
        public void setPlayers(GameProfile[] players) {
        }

    }

    @Mapping("jt$c")
    public static class Version {

        @Mapping("a")
        private final String name = null;

        @Mapping("b")
        private final int protocol = 0;

        public Version(String name, int protocol) {
        }

        @Mapping("a")
        public String getName() {
            return null;
        }

        @Mapping("b")
        public int getProtocol() {
            return 0;
        }

    }

}
