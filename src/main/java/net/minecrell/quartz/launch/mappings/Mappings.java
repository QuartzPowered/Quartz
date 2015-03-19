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

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;
import org.objectweb.asm.tree.MethodNode;

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
        if (className == null) {
            return null;
        }

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

    public String unmap(String className) {
        if (className == null) {
            return null;
        }

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

}
