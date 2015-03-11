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
package net.minecrell.quartz.launch.console;

import static com.google.common.base.Preconditions.checkState;
import static jline.TerminalFactory.JLINE_TERMINAL;
import static jline.TerminalFactory.OFF;
import static jline.console.ConsoleReader.RESET_LINE;

import com.mojang.util.QueueLogAppender;
import jline.console.ConsoleReader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.io.PrintStream;

public final class QuartzConsole {

    private QuartzConsole() {}

    private static ConsoleReader reader;

    public static ConsoleReader getReader() {
        checkState(reader != null, "Terminal was not initialized");
        return reader;
    }

    private static void disable() {
        System.setProperty(JLINE_TERMINAL, OFF);
    }

    private static void initialize() throws IOException {
        reader = new ConsoleReader();
        reader.setExpandEvents(false);
    }

    public static void initialize(boolean jline) throws IOException {
        // Initialize jline
        if (jline) {
            AnsiConsole.systemInstall();
        } else {
            disable();
        }

        initialize();
    }

    public static void initializeFallback() throws IOException {
        // Try again with jline disabled
        disable();
        initialize();
    }

    public static void start() {
        Thread thread = new Thread(new ConsoleWriterThread(), "Quartz Console Thread");
        thread.setDaemon(true);
        thread.start();

        Logger logger = LogManager.getRootLogger();
        System.setOut(new PrintStream(new LoggingOutputStream(logger, Level.INFO), true));
        System.setErr(new PrintStream(new LoggingOutputStream(logger, Level.ERROR), true));
    }

    private static class ConsoleWriterThread implements Runnable {

        @Override
        public void run() {
            String message;

            while (true) {
                message = QueueLogAppender.getNextLogEvent("QuartzConsole");
                if (message == null) {
                    continue;
                }

                try {
                    reader.print(RESET_LINE + message);
                    reader.drawLine();
                    reader.flush();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

}
