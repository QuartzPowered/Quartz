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
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecrell.quartz.launch.util.Methods;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.MethodNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public final class MappingsLoader {

    private MappingsLoader() {}

    public static final String PACKAGE = "net/minecraft/server";
    public static final String PACKAGE_CLASS = "net.minecraft.server.";
    private static final String PACKAGE_PREFIX = PACKAGE + '/';
    private static final String MAPPINGS_DIR = "mappings/";

    public static Mappings load(Logger logger) throws IOException {
        InputStream in = MappingsLoader.class.getResourceAsStream("/mappings.json");
        checkState(in != null, "Failed to find mappings in mappings.json. Make sure you have the annotation processor configured properly");

        final Gson gson = new Gson();
        final Type mappingsType = new TypeToken<Map<String, MappingInfo>>(){}.getType();

        Map<String, MappingInfo> mappings;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            mappings = gson.fromJson(reader, mappingsType);
        }

        return fromJson(mappings);
    }

    private static Mappings fromJson(Map<String, MappingInfo> mappings) {
        ImmutableBiMap.Builder<String, String> classes = ImmutableBiMap.builder();
        ImmutableTable.Builder<String, String, String> methods = ImmutableTable.builder();
        ImmutableTable.Builder<String, String, String> fields = ImmutableTable.builder();

        ImmutableTable.Builder<String, String, AccessMapping> accessMappings = ImmutableTable.builder();

        ImmutableMultimap.Builder<String, MethodNode> constructors = ImmutableMultimap.builder();

        for (Map.Entry<String, MappingInfo> entry : mappings.entrySet()) {
            String internalName = entry.getKey();
            String className = internalName.replace('/', '.');

            MappingInfo mapping = entry.getValue();
            String mappedName = mapping.name;
            if (mappedName != null) {
                classes.put(mappedName, internalName);
            } else {
                mappedName = entry.getKey();
            }

            if (mapping.methods != null) {
                fillTable(methods, mappedName, mapping.methods);
            }

            if (mapping.fields != null) {
                fillTable(fields, mappedName, mapping.fields);
            }

            if (mapping.access != null) {
                fillTable(accessMappings, className, mapping.access);
            }

            if (mapping.constructors != null) {
                for (String constructor : mapping.constructors) {
                    MethodNode methodNode = new MethodNode(ACC_PUBLIC | ACC_STATIC, "create", constructor, null, null);
                    Methods.visitConstructor(methodNode, internalName);
                    constructors.put(className, methodNode);
                }
            }
        }

        return new Mappings(classes.build(), methods.build(), fields.build(), accessMappings.build(), constructors.build());
    }

    private static <R, C, V> void fillTable(ImmutableTable.Builder<R, C, V> builder, R row, Map<C, V> values) {
        for (Map.Entry<C, V> entry : values.entrySet()) {
            builder.put(row, entry.getKey(), entry.getValue());
        }
    }

    private static class MappingInfo {

        private String name;
        private Map<String, String> methods;
        private Map<String, String> fields;
        private Map<String, AccessMapping> access;
        private Set<String> constructors;

    }

}
