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

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public enum Access {

    PRIVATE (ACC_PRIVATE),
    PACKAGE_LOCAL (0),
    PROTECTED (ACC_PROTECTED),
    PUBLIC (ACC_PUBLIC);

    private static final Access[] modifiers = values();
    private final int modifier;

    private Access(int modifier) {
        this.modifier = modifier;
    }

    public int getModifier() {
        return modifier;
    }

    public boolean is(int access) {
        return (access & modifier) != 0;
    }

    public int transform(int access) {
        Access current = Access.of(access);
        if (this != current) {
            // Don't lower access
            if (current.compareTo(this) < 0) {
                access &= ~current.modifier;
                access |= modifier;
            }
        }

        return access;
    }

    public static Access of(int access) {
        for (Access modifier : modifiers) {
            if (modifier.is(access)) {
                return modifier;
            }
        }

        throw new AssertionError();
    }

    public static boolean isFinal(int access) {
        return (access & ACC_FINAL) != 0;
    }

    public static int setFinal(int access, boolean finalMod) {
        if (finalMod != isFinal(access)) {
            if (finalMod) {
                access |= ACC_FINAL;
            } else {
                access &= ~ACC_FINAL;
            }
        }

        return access;
    }

}
