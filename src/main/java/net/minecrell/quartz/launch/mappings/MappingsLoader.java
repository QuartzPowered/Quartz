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

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableTable;
import net.minecrell.quartz.launch.util.ParsedAnnotation;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

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

public class MappingsLoader {

    public static final String PACKAGE = "net/minecraft/server";
    public static final String PACKAGE_CLASS = "net.minecraft.server.";
    private static final String PACKAGE_PREFIX = PACKAGE + '/';
    private static final String MAPPINGS_DIR = "mappings/";

    private final List<ClassNode> mappingClasses;

    protected MappingsLoader(List<ClassNode> mappingClasses) {
        this.mappingClasses = mappingClasses;
    }

    public static MappingsLoader load() throws IOException {
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

        return new MappingsLoader(mappingClasses);
    }

    public ImmutableBiMap<String, String> loadClasses() {
        ImmutableBiMap.Builder<String, String> classes = ImmutableBiMap.builder();

        for (ClassNode classNode : mappingClasses) {
            ParsedAnnotation mappingAnnotation = MappingsParser.getClassMapping(classNode);
            checkState(mappingAnnotation != null, "Class %s is missing the @Mapping annotation", classNode.name);
            String mapping = mappingAnnotation.getString("value", "");
            if (!mapping.isEmpty()) {
                classes.put(mapping, classNode.name);
            }
        }

        return classes.build();
    }

    public ImmutableTable<String, String, String> loadFields(Remapper remapper) {
        ImmutableTable.Builder<String, String, String> fields = ImmutableTable.builder();

        for (ClassNode classNode : mappingClasses) {
            for (FieldNode fieldNode : classNode.fields) {
                ParsedAnnotation mappingAnnotation = MappingsParser.getFieldMapping(fieldNode);
                //checkState(mappingAnnotation != null, "Field %s in %s is missing the @Mapping annotation", fieldNode.name, classNode.name);
                if (mappingAnnotation == null) continue; // TODO
                String mapping = mappingAnnotation.getString("value", "");
                if (!mapping.isEmpty()) {
                    fields.put(classNode.name, mapping + ':' + remapper.mapDesc(fieldNode.desc), fieldNode.name);
                }
            }
        }

        return fields.build();
    }

    public ImmutableTable<String, String, String> loadMethods(Remapper remapper) {
        ImmutableTable.Builder<String, String, String> methods = ImmutableTable.builder();

        for (ClassNode classNode : mappingClasses) {
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.charAt(0) == '<') continue;
                ParsedAnnotation mappingAnnotation = MappingsParser.getMethodMapping(methodNode);
                //checkState(mappingAnnotation != null, "Method %s in %s is missing the @Mapping annotation", methodNode.name, classNode.name);
                if (mappingAnnotation == null) continue; // TODO
                String mapping = mappingAnnotation.getString("value", "");
                if (!mapping.isEmpty()) {
                    methods.put(classNode.name, mapping + remapper.mapDesc(methodNode.desc), methodNode.name);
                }
            }
        }

        return methods.build();
    }

}
