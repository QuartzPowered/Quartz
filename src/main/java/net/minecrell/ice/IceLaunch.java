package net.minecrell.ice;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

import javax.annotation.Nullable;

public final class IceLaunch {
    private IceLaunch() {}

    @Nullable private static Path gameDir;

    public static void initialize(Path gameDir) {
        IceLaunch.gameDir = requireNonNull(gameDir, "gameDir");
        // TODO: Options
    }

    public static Path getGameDirectory() {
        checkState(gameDir != null, "Ice was not initialized");
        return gameDir;
    }

}
