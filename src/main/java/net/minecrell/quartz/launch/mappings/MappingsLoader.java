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

package net.minecrell.quartz.launch.mappings;

import static java.util.Objects.requireNonNull;

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class MappingsLoader {

    private MappingsLoader() {}

    public static final String PACKAGE = "net/minecraft/server";
    public static final String PACKAGE_CLASS = "net.minecraft.server.";
    private static final String PACKAGE_PREFIX = PACKAGE + '/';
    private static final String MAPPINGS_DIR = "mappings/";

    public static Mappings load() throws IOException {
        URI source;
        try {
            source = requireNonNull(Mapping.class.getProtectionDomain().getCodeSource(), "Unable to find class source").getLocation().toURI();
        } catch (URISyntaxException e) {
            throw new IOException("Failed to find class source", e);
        }

        Path location = Paths.get(source);
        System.out.println(location);

        List<ClassNode> mappingClasses = new ArrayList<>();

        // Load the classes from the source
        if (Files.isDirectory(location)) {
            // We're probably in development environment or something similar
            // Search for the class files
            Files.walkFileTree(location.resolve(PACKAGE), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".class")) {
                        try (InputStream in = Files.newInputStream(file)) {
                            ClassNode classNode = MappingsParser.loadClassStructure(in);
                            mappingClasses.add(classNode);
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            // Go through the JAR file
            try (ZipFile zip = new ZipFile(location.toFile())) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = StringUtils.removeStart(entry.getName(), MAPPINGS_DIR);
                    if (entry.isDirectory() || !name.endsWith(".class") || !name.startsWith(PACKAGE_PREFIX)) {
                        continue;
                    }

                    // Ok, we found something
                    try (InputStream in = zip.getInputStream(entry)) {
                        ClassNode classNode = MappingsParser.loadClassStructure(in);
                        mappingClasses.add(classNode);
                    }
                }
            }
        }

        return new Mappings(mappingClasses);
    }

}
