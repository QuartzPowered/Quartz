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

import com.google.common.collect.ImmutableMap;
import net.minecrell.quartz.launch.util.ParsedAnnotation;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MappingsParser {

    public static final String MAPPING = Type.getDescriptor(Mapping.class);
    public static final String ACCESSIBLE = Type.getDescriptor(Accessible.class);

    private MappingsParser() {}

    private static ParsedAnnotation parseAnnotation(List<AnnotationNode> annotationNodes, String annotationDesc) {
        if (annotationNodes == null) {
            return null;
        }

        for (AnnotationNode annotationNode : annotationNodes) {
            if (annotationNode.desc.equals(annotationDesc)) {
                return ParsedAnnotation.parse(annotationNode);
            }
        }

        return null;
    }

    public static ParsedAnnotation getAnnotation(ClassNode classNode, String annotationDesc) {
        return parseAnnotation(classNode.invisibleAnnotations, annotationDesc);
    }

    public static ParsedAnnotation getAnnotation(MethodNode methodNode, String annotationDesc) {
        return parseAnnotation(methodNode.invisibleAnnotations, annotationDesc);
    }

    public static ParsedAnnotation getAnnotation(FieldNode fieldNode, String annotationDesc) {
        return parseAnnotation(fieldNode.invisibleAnnotations, annotationDesc);
    }

    public static boolean isMappingsClass(byte[] bytes) {
        return getAnnotation(loadClassStructure(bytes), MAPPING) != null;
    }

    private static Map<String, ParsedAnnotation> parseAnnotations(List<AnnotationNode> annotationNodes) {
        if (annotationNodes == null || annotationNodes.isEmpty()) {
            return Collections.emptyMap();
        }

        ImmutableMap.Builder<String, ParsedAnnotation> builder = ImmutableMap.builder();
        for (AnnotationNode annotationNode : annotationNodes) {
            builder.put(annotationNode.desc, ParsedAnnotation.parse(annotationNode));
        }

        return builder.build();
    }

    public static Map<String, ParsedAnnotation> getAnnotations(ClassNode classNode) {
        return parseAnnotations(classNode.invisibleAnnotations);
    }

    public static Map<String, ParsedAnnotation> getAnnotations(MethodNode methodNode) {
        return parseAnnotations(methodNode.invisibleAnnotations);
    }

    public static Map<String, ParsedAnnotation> getAnnotations(FieldNode fieldNode) {
        return parseAnnotations(fieldNode.invisibleAnnotations);
    }

    public static ClassNode loadClassStructure(ClassReader reader) {
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return classNode;
    }

    public static ClassNode loadClassStructure(byte[] bytes) {
        return loadClassStructure(new ClassReader(bytes));
    }

    public static ClassNode loadClassStructure(InputStream in) throws IOException {
        return loadClassStructure(new ClassReader(in));
    }

}
