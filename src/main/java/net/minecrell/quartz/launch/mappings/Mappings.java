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
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Mappings {

    private static final String MAPPING_DESCRIPTOR = Type.getDescriptor(Mapping.class);
    private static final String ACCESSIBLE_DESCRIPTOR = Type.getDescriptor(Accessible.class);

    public static final String PACKAGE = "net/minecraft/server";
    private static final String PACKAGE_PREFIX = PACKAGE + '/';

    private static Mappings instance;

    public static Mappings getInstance() {
        checkState(instance != null, "Not initialized yet");
        return instance;
    }

    public static void initialize(LaunchClassLoader loader) throws IOException {
        if (instance != null) {
            return;
        }
        instance = new Mappings();
        loader.registerTransformer(Mappings.class.getPackage().getName() + ".MappingsTransformer");
    }

    protected final ImmutableBiMap<String, String> classes;
    protected final ImmutableBiMap<String, String> methods;
    protected final ImmutableBiMap<String, String> fields;

    public Mappings() throws IOException {
        ImmutableBiMap.Builder<String, String> classes = ImmutableBiMap.builder();
        ImmutableBiMap.Builder<String, String> methods = ImmutableBiMap.builder();
        ImmutableBiMap.Builder<String, String> fields = ImmutableBiMap.builder();

        loadMappings(classes, methods, fields);

        this.classes = classes.build();
        this.methods = methods.build();
        this.fields = fields.build();

        System.out.println(this.classes);
    }

    private static void loadMappings(ImmutableBiMap.Builder<String, String> classes,
            ImmutableBiMap.Builder<String, String> methods, ImmutableBiMap.Builder<String, String> fields) throws IOException {
        URI source;
        try {
            source = requireNonNull(Mappings.class.getProtectionDomain().getCodeSource(), "Unable to find class source").getLocation().toURI();
        } catch (URISyntaxException e) {
            throw new IOException("Failed to find class source", e);
        }

        Path location = Paths.get(source);
        System.out.println(location);

        Map<String, ClassNode> mappingClasses = new HashMap<>();

        // Load the classes from the source
        if (Files.isDirectory(location)) {
            // We're probably in development environment or something similar
            // Search for the class files
            Files.walkFileTree(location.resolve(PACKAGE), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".class")) {
                        try (InputStream in = Files.newInputStream(file)) {
                            ClassNode classNode = loadClassStructure(in);
                            mappingClasses.put(classNode.name, classNode);
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
                    if (entry.isDirectory() || !entry.getName().endsWith(".class") || !entry.getName().startsWith(PACKAGE_PREFIX)) {
                        continue;
                    }

                    // Ok, we found something
                    try (InputStream in = zip.getInputStream(entry)) {
                        ClassNode classNode = loadClassStructure(in);
                        mappingClasses.put(classNode.name, classNode);
                    }
                }
            }
        }

        for (ClassNode classNode : mappingClasses.values()) {
            if (classNode.invisibleAnnotations != null) {
                for (AnnotationNode annotation : classNode.invisibleAnnotations) {
                    if (annotation.desc.equals(MAPPING_DESCRIPTOR)) {
                        Iterator<Object> values = annotation.values.iterator();
                        checkState(values.next().equals("value"), "Invalid annotation tag in %s", classNode.name);
                        classes.put((String) values.next(), classNode.name);
                        break;
                    }
                }
            }
        }
    }

    private static ClassNode loadClassStructure(InputStream in) throws IOException {
        ClassReader reader = new ClassReader(in);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return classNode;
    }

}
