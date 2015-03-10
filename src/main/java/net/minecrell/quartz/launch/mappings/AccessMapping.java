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

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class AccessMapping {

    private final Access access;
    private final boolean finalModifier;

    public AccessMapping(Access access, boolean finalModifier) {
        this.access = access;
        this.finalModifier = finalModifier;
    }

    public Access getAccess() {
        return access;
    }

    public boolean isFinal() {
        return finalModifier;
    }

    public int transform(int access) {
        return Access.setFinal(this.access.transform(access), finalModifier);
    }

    public void transform(ClassNode classNode) {
        classNode.access = transform(classNode.access);
    }

    public void transform(MethodNode methodNode) {
        methodNode.access = transform(methodNode.access);
    }

    public void transform(FieldNode fieldNode) {
        fieldNode.access = transform(fieldNode.access);
    }

    public static AccessMapping of(int access) {
        return new AccessMapping(Access.of(access), Access.isFinal(access));
    }

    public static AccessMapping of(ClassNode classNode) {
        return of(classNode.access);
    }

    public static AccessMapping of(MethodNode methodNode) {
        return of(methodNode.access);
    }

    public static AccessMapping of(FieldNode fieldNode) {
        return of(fieldNode.access);
    }

}
