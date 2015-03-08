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

package net.minecrell.quartz.launch.transformers;

import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecrell.quartz.launch.mappings.Mappings;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.ClassNode;

public class MappingsTransformer extends Remapper implements IClassTransformer, IClassNameTransformer {

    private final Mappings mappings;

    public MappingsTransformer() {
        this(Mappings.getInstance());
    }

    // For custom transformers
    protected MappingsTransformer(Mappings mappings) {
        this.mappings = mappings;
    }

    @Override
    public String map(String typeName) {
        String name = mappings.classes.get(typeName);
        if (name != null) {
            return name;
        }

        int innerClassPos = typeName.lastIndexOf('$');
        if (innerClassPos >= 0) {
            name = mappings.classes.get(typeName.substring(0, innerClassPos));
            if (name != null) {
                return name + typeName.substring(innerClassPos);
            }
        }

        return typeName;
    }

    public String unmap(String typeName) {
        String name = mappings.classes.inverse().get(typeName);
        if (name != null) {
            return name;
        }

        int innerClassPos = typeName.lastIndexOf('$');
        if (innerClassPos >= 0) {
            name = mappings.classes.inverse().get(typeName.substring(0, innerClassPos));
            if (name != null) {
                return name + typeName.substring(innerClassPos);
            }
        }

        return typeName;
    }

    @Override
    public String remapClassName(String name) {
        return map(name.replace('.', '/')).replace('/', '.');
    }

    @Override
    public String unmapClassName(String name) {
        return unmap(name.replace('.', '/')).replace('/', '.');
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        // Special case for main class as our mapping class would overwrite it otherwise
        /*if (name.equals(QuartzTweaker.MAIN)) {
            bytes = QuartzTweaker.mainClass;
        } else if (bytes == null || (name.indexOf('.') != -1 && !name.startsWith(Mappings.PACKAGE))) {
            return bytes;
        }*/

        if (bytes == null) {
            return null;
        }

        ClassWriter writer = new ClassWriter(0);
        ClassReader reader = new ClassReader(bytes);
        reader.accept(new RemappingClassAdapter(writer, this), ClassReader.EXPAND_FRAMES);

        reader = new ClassReader(writer.toByteArray());
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        return writer.toByteArray();
    }

}
