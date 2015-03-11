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

import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

import net.minecrell.quartz.launch.mappings.Mappings;
import org.objectweb.asm.tree.ClassNode;

public class ConstructorTransformer extends MappingTreeTransformer {

    public ConstructorTransformer() {
    }

    protected ConstructorTransformer(Mappings mappings) {
        super(mappings);
    }

    @Override
    protected int writerFlags() {
        return COMPUTE_MAXS;
    }

    @Override
    protected boolean transform(String name, String transformedName) {
        return mappings.getConstructors().containsKey(transformedName);
    }

    @Override
    protected void transform(String name, String transformedName, ClassNode classNode) {
        classNode.methods.addAll(mappings.getConstructors().get(transformedName));
    }

}
