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
package net.minecraft.server;

import com.google.common.util.concurrent.ListenableFuture;
import net.minecraft.server.block.BlockLocation;
import net.minecraft.server.command.CommandSender;
import net.minecraft.server.status.ServerStatusResponse;
import net.minecraft.server.world.WorldType;
import net.minecrell.quartz.launch.mappings.Mapping;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

@Mapping
public abstract class MinecraftServer implements CommandSender {

    public static final String MINECRAFT_SERVER = "Lnet/minecraft/server/MinecraftServer;";

    @Mapping("i")
    protected abstract boolean startServer() throws IOException;

    @Mapping("a")
    public abstract void setManager(ServerManager manager);

    public static final String setManager = "setManager(Lnet/minecraft/server/ServerManager;)V";

    @Mapping("a")
    protected abstract void loadWorlds(String folder1, String folder2, long seed, WorldType type, String settings);

    public static final String loadWorlds = "loadWorlds(Ljava/lang/String;Ljava/lang/String;JLnet/minecraft/server/world/WorldType;"
            + "Ljava/lang/String;)V";


    @Mapping("a")
    private void loadFavicon(ServerStatusResponse response) {
    }

    public static final String loadFavicon = "loadFavicon(Lnet/minecraft/server/status/ServerStatusResponse;)V";

    @Mapping("u")
    public abstract boolean isRunning();

    @Mapping("an")
    public abstract boolean isStopped();

    @Mapping("a")
    protected abstract void stop(CrashReport crash);

    public static final String stop = "stop(Lnet/minecraft/server/CrashReport;)V";

    @Mapping("y")
    protected abstract void exit();

    public static final String exit = "exit()V";

    @Mapping("aF")
    public abstract ServerStatusResponse getStatusResponse();

    @Mapping("a")
    public abstract List<String> getTabCompletions(CommandSender sender, String input, BlockLocation location);

    @Mapping("a")
    public abstract <V> ListenableFuture<V> scheduleInMainThread(Callable<V> task);

}
