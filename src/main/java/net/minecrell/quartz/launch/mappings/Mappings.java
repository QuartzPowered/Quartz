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
import static net.minecrell.quartz.launch.mappings.MappingsParser.ACCESSIBLE;
import static net.minecrell.quartz.launch.mappings.MappingsParser.CONSTRUCTOR;
import static net.minecrell.quartz.launch.mappings.MappingsParser.MAPPING;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;
import net.minecrell.quartz.launch.util.Methods;
import net.minecrell.quartz.launch.util.ParsedAnnotation;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.Map;

public class Mappings {

    protected final ImmutableBiMap<String, String> classes;
    protected final ImmutableTable<String, String, String> methods;
    protected final ImmutableTable<String, String, String> fields;

    protected final ImmutableMultimap<String, MethodNode> constructors;

    protected final ImmutableTable<String, String, AccessMapping> accessMappings;

    public Mappings(ImmutableBiMap<String, String> classes,
            ImmutableTable<String, String, String> methods, ImmutableTable<String, String, String> fields,
            ImmutableTable<String, String, AccessMapping> accessMappings, ImmutableMultimap<String, MethodNode> constructors) {
        this.classes = requireNonNull(classes, "classes");
        this.methods = requireNonNull(methods, "methods");
        this.fields = requireNonNull(fields, "fields");
        this.constructors = requireNonNull(constructors, "constructors");
        this.accessMappings = requireNonNull(accessMappings, "accessMappings");
    }

    protected Mappings(List<ClassNode> mappingClasses) {
        requireNonNull(mappingClasses, "mappingClasses");
        if (mappingClasses.isEmpty()) {
            this.classes = ImmutableBiMap.of();
            this.methods = ImmutableTable.of();
            this.fields = ImmutableTable.of();
            this.constructors = ImmutableMultimap.of();
            this.accessMappings = ImmutableTable.of();
            return;
        }

        ImmutableBiMap.Builder<String, String> classes = ImmutableBiMap.builder();

        for (ClassNode classNode : mappingClasses) {
            ParsedAnnotation annotation = MappingsParser.getAnnotation(classNode, MAPPING);
            checkState(annotation != null, "Class %s is missing the @Mapping annotation", classNode.name);
            String mapping = annotation.getString("value", "");
            if (!mapping.isEmpty()) {
                classes.put(mapping, classNode.name);
            }
        }

        this.classes = classes.build();

        // We need to remap the descriptors of the fields and methods, use ASM for convenience
        Remapper remapper = new Remapper() {

            @Override
            public String map(String className) {
                return unmap(className);
            }
        };

        // Load field, method and access mappings
        ImmutableTable.Builder<String, String, String> methods = ImmutableTable.builder();
        ImmutableTable.Builder<String, String, String> fields = ImmutableTable.builder();
        ImmutableMultimap.Builder<String, MethodNode> constructors = ImmutableMultimap.builder();
        ImmutableTable.Builder<String, String, AccessMapping> accessMappings = ImmutableTable.builder();

        for (ClassNode classNode : mappingClasses) {
            String className = classNode.name.replace('/', '.');
            String internalName = unmap(classNode.name);

            ParsedAnnotation annotation = MappingsParser.getAnnotation(classNode, ACCESSIBLE);
            if (annotation != null) {
                accessMappings.put(className, "", AccessMapping.of(classNode));
            }

            for (MethodNode methodNode : classNode.methods) {
                Map<String, ParsedAnnotation> annotations = MappingsParser.getAnnotations(methodNode);

                annotation = annotations.get(CONSTRUCTOR);
                if (annotation != null) {
                    // Generate constructor call code
                    Methods.visitConstructor(methodNode, classNode.name);
                    constructors.put(className, methodNode);
                    continue;
                }

                // TODO: Validation
                annotation = annotations.get(MAPPING);
                if (annotation != null) {
                    String mapping = annotation.getString("value", "");
                    if (!mapping.isEmpty()) {
                        methods.put(internalName, mapping + remapper.mapMethodDesc(methodNode.desc), methodNode.name);
                    }
                }

                annotation = annotations.get(ACCESSIBLE);
                if (annotation != null) {
                    accessMappings.put(className, methodNode.name + methodNode.desc, AccessMapping.of(methodNode));
                }
            }

            for (FieldNode fieldNode : classNode.fields) {
                Map<String, ParsedAnnotation> annotations = MappingsParser.getAnnotations(fieldNode);

                // TODO: Validation
                annotation = annotations.get(MAPPING);
                if (annotation != null) {
                    String mapping = annotation.getString("value", "");
                    if (!mapping.isEmpty()) {
                        fields.put(internalName, mapping + ':' + remapper.mapDesc(fieldNode.desc), fieldNode.name);
                    }
                }

                annotation = annotations.get(ACCESSIBLE);
                if (annotation != null) {
                    accessMappings.put(className, fieldNode.name, AccessMapping.of(fieldNode));
                }
            }
        }

        this.methods = methods.build();
        this.fields = fields.build();
        this.constructors = constructors.build();
        this.accessMappings = accessMappings.build();
    }

    public ImmutableBiMap<String, String> getClasses() {
        return classes;
    }

    public ImmutableTable<String, String, String> getMethods() {
        return methods;
    }

    public ImmutableTable<String, String, String> getFields() {
        return fields;
    }

    public ImmutableMultimap<String, MethodNode> getConstructors() {
        return constructors;
    }

    public ImmutableTable<String, String, AccessMapping> getAccessMappings() {
        return accessMappings;
    }

    public String map(String className) {
        String name = classes.get(className);
        if (name != null) {
            return name;
        }

        // We may have no name for the inner class directly, but it should be still part of the outer class
        int innerClassPos = className.lastIndexOf('$');
        if (innerClassPos >= 0) {
            name = map(className.substring(0, innerClassPos));
            if (name != null) {
                return name + className.substring(innerClassPos);
            }
        }

        return className; // Unknown class
    }

    public String unmap(String className) {
        String name = classes.inverse().get(className);
        if (name != null) {
            return name;
        }

        // We may have no name for the inner class directly, but it should be still part of the outer class
        int innerClassPos = className.lastIndexOf('$');
        if (innerClassPos >= 0) {
            name = unmap(className.substring(0, innerClassPos));
            if (name != null) {
                return name + className.substring(innerClassPos);
            }
        }

        return className; // Unknown class
    }

}
