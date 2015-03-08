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
package net.minecrell.quartz.launch.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ParsedAnnotation {

    private final Type type;
    private final ImmutableMap<String, Object> values;

    private ParsedAnnotation(Type type, ImmutableMap<String, Object> values) {
        this.type = type;
        this.values = values;
    }

    public Type getType() {
        return type;
    }

    public ImmutableMap<String, Object> getValues() {
        return values;
    }

    @SuppressWarnings("unchecked")
    private <T> T get(String key) {
        return (T) values.get(key);
    }

    private static <T> T withDefault(T result, T def) {
        return result != null ? result : def;
    }

    private <T> T get(String key, T def) {
        return withDefault(get(key), def);
    }

    public Boolean getBoolean(String key) {
        return get(key);
    }

    public Boolean getBoolean(String key, Boolean def) {
        return get(key, def);
    }

    public <T extends Number> T getNumber(String key) {
        return get(key);
    }

    public <T extends Number> T getNumber(String key, T def) {
        return get(key, def);
    }

    public Character getCharacter(String key) {
        return get(key);
    }

    public Character getCharacter(String key, Character def) {
        return get(key, def);
    }

    public String getString(String key) {
        return get(key);
    }

    public String getString(String key, String def) {
        return get(key, def);
    }

    public Type getType(String key) {
        return get(key);
    }

    public Type getType(String key, Type def) {
        return get(key, def);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass) {
        String[] enumValue = get(key);
        if (enumValue != null) {
            checkArgument(enumValue[0].equals(Type.getDescriptor(enumClass)), "Mismatching enum %s <-> %s", enumValue[0], enumClass);
            return Enum.valueOf(enumClass, enumValue[1]);
        } else {
            return null;
        }
    }

    public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass, T def) {
        return withDefault(getEnum(key, enumClass), def);
    }

    public AnnotationNode getAnnotationNode(String key) {
        return get(key);
    }

    public ParsedAnnotation getAnnotation(String key) {
        AnnotationNode annotationNode = getAnnotationNode(key);
        return annotationNode != null ? ParsedAnnotation.parse(annotationNode) : null;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getList(String key) {
        List<T> result = (List<T>) values.get(key);
        return result != null ? result : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public List<Boolean> getBooleans(String key) {
        return getList(key);
    }

    @SuppressWarnings("unchecked")
    public <T extends Number> List<T> getNumbers(String key) {
        return getList(key);
    }

    public List<Character> getCharacters(String key) {
        return getList(key);
    }

    public List<String> getStrings(String key) {
        return getList(key);
    }

    public List<Type> getTypes(String key) {
        return getList(key);
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> List<T> getEnums(String key, Class<T> enumClass) {
        List<String[]> enumValues = getList(key);
        if (enumValues.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>(enumValues.size());

        for (String[] enumValue : enumValues) {
            checkArgument(enumValue[0].equals(Type.getDescriptor(enumClass)), "Mismatching enum %s <-> %s", enumValue[0], enumClass);
            result.add(Enum.valueOf(enumClass, enumValue[1]));
        }

        return result;
    }

    public List<AnnotationNode> getAnnotationNodes(String key) {
        return getList(key);
    }

    public List<ParsedAnnotation> getAnnotations(String key) {
        List<AnnotationNode> annotationNodes = getAnnotationNodes(key);
        if (annotationNodes.isEmpty()) {
            return Collections.emptyList();
        }

        List<ParsedAnnotation> result = new ArrayList<>(annotationNodes.size());

        for (AnnotationNode annotationNode : annotationNodes) {
            result.add(ParsedAnnotation.parse(annotationNode));
        }

        return result;
    }

    public static ParsedAnnotation parse(AnnotationNode annotationNode) {
        Type type = Type.getType(annotationNode.desc);
        ImmutableMap<String, Object> values;

        if (annotationNode.values != null) {
            ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
            Iterator<Object> itr = annotationNode.values.iterator();
            do {
                builder.put((String) itr.next(), itr.next());
            } while (itr.hasNext());

            values = builder.build();
        } else {
            values = ImmutableMap.of();
        }

        return new ParsedAnnotation(type, values);
    }

}
