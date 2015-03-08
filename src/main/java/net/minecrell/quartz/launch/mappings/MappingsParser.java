package net.minecrell.quartz.launch.mappings;

import com.google.common.collect.ImmutableMap;
import net.minecrell.quartz.launch.util.ParsedAnnotation;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MappingsParser {

    private static final String MAPPING = Type.getDescriptor(Mapping.class);
    private static final String ACCESSIBLE = Type.getDescriptor(Accessible.class);

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

    public static ParsedAnnotation getMapping(List<AnnotationNode> annotationNodes) {
        return parseAnnotation(annotationNodes, MAPPING);
    }

    public static ParsedAnnotation getClassMapping(ClassNode classNode) {
        return getMapping(classNode.invisibleAnnotations);
    }

    public static boolean isMappingsClass(byte[] bytes) {
        return getClassMapping(loadClassStructure(bytes)) != null;
    }

    static ClassNode loadClassStructure(ClassReader reader) {
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return classNode;
    }

    static ClassNode loadClassStructure(byte[] bytes) {
        return loadClassStructure(new ClassReader(bytes));
    }

    static ClassNode loadClassStructure(InputStream in) throws IOException {
        return loadClassStructure(new ClassReader(in));
    }

}
