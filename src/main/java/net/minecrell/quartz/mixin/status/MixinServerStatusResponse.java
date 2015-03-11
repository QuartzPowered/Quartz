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
package net.minecrell.quartz.mixin.status;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import net.minecraft.server.chat.ChatComponent;
import net.minecraft.server.status.ServerStatusResponse;
import net.minecrell.quartz.status.QuartzFavicon;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.event.server.StatusPingEvent;
import org.spongepowered.api.status.Favicon;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

import javax.annotation.Nullable;

@Mixin(ServerStatusResponse.class)
public abstract class MixinServerStatusResponse implements StatusPingEvent.Response {

    @Shadow
    private ChatComponent description;
    private Message descriptionMessage;

    @Shadow
    @Nullable
    private ServerStatusResponse.Players players;

    @Nullable private ServerStatusResponse.Players playersBackup;

    @Shadow
    private ServerStatusResponse.Version version;

    @Shadow
    @Nullable
    private String favicon;

    private Optional<Favicon> faviconHandle;

    @Override
    public Message getDescription() {
        return descriptionMessage;
    }

    @Override
    public void setDescription(Message description) {
        this.descriptionMessage = requireNonNull(description, "description");
        //this.description = // TODO: Text API Implementation
    }

    @Overwrite
    public void setDescription(ChatComponent description) {
        this.description = requireNonNull(description, "description");
        //this.descriptionMessage = // TODO: Text API Implementation
    }

    @Override
    public Optional<Players> getPlayers() {
        return Optional.fromNullable((Players) players);
    }

    @Override
    public void setHidePlayers(boolean hide) {
        if ((this.players == null) != hide) {
            if (hide) {
                this.playersBackup = players;
                this.players = null;
            } else {
                this.players = playersBackup;
                this.playersBackup = null;
            }
        }
    }

    @Override
    public MinecraftVersion getVersion() {
        return (MinecraftVersion) version;
    }

    @Override
    public Optional<Favicon> getFavicon() {
        return faviconHandle;
    }

    @Override
    public void setFavicon(@Nullable Favicon favicon) {
        this.faviconHandle = Optional.fromNullable(favicon);
        if (this.faviconHandle.isPresent()) {
            this.favicon = ((QuartzFavicon) this.faviconHandle.get()).getEncoded();
        } else {
            this.favicon = null;
        }
    }

    @Overwrite
    public void setFavicon(@Nullable String favicon) {
        if (favicon == null) {
            this.favicon = null;
            this.faviconHandle = Optional.absent();
        } else {
            try {
                this.faviconHandle = Optional.of(new QuartzFavicon(favicon));
                this.favicon = favicon;
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }
    }

}
