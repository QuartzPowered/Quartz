package net.minecrell.quartz.mappings.processor;

import com.google.common.collect.ImmutableBiMap;
import org.objectweb.asm.commons.Remapper;

public class ClassMappings {

    private final ImmutableBiMap<String, String> classes;
    private Remapper mapper;
    private Remapper unmapper;

    public ClassMappings(ImmutableBiMap<String, String> classes) {
        this.classes = classes;
    }

    public String map(String className) {
        if (className == null) return null;

        String name = classes.get(className);
        if (name != null) {
            return name;
        }

        // We may have no name for the inner class directly, but it should be still part of the outer class
        int innerClassPos = className.lastIndexOf('$');
        if (innerClassPos >= 0) {
            return map(className.substring(0, innerClassPos)) + className.substring(innerClassPos);
        }

        return className; // Unknown class
    }

    public Remapper getMapper() {
        if (mapper == null) {
            this.mapper = new Remapper() {

                @Override
                public String map(String typeName) {
                    return ClassMappings.this.map(typeName);
                }
            };
        }

        return mapper;
    }

    public String unmap(String className) {
        String name = classes.inverse().get(className);
        if (name != null) {
            return name;
        }

        // We may have no name for the inner class directly, but it should be still part of the outer class
        int innerClassPos = className.lastIndexOf('$');
        if (innerClassPos >= 0) {
            return unmap(className.substring(0, innerClassPos)) + className.substring(innerClassPos);
        }

        return className; // Unknown class
    }

    public Remapper getUnmapper() {
        if (unmapper == null) {
            this.unmapper = new Remapper() {

                @Override
                public String map(String typeName) {
                    return unmap(typeName);
                }
            };
        }

        return unmapper;
    }

}
