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

import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NEW;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

public final class Methods {

    private Methods() {}

    public static void visitConstructor(MethodNode methodNode, String target) {
        methodNode.visitTypeInsn(NEW, target);
        methodNode.visitInsn(DUP);

        forward(methodNode, target, "<init>");
    }

    private static void forward(MethodNode methodNode, String owner, String name) {
        Type[] parameters = Type.getArgumentTypes(methodNode.desc);
        for (int i = 0; i < parameters.length; i++) {
            methodNode.visitVarInsn(parameters[i].getOpcode(ILOAD), i);
        }

        methodNode.visitMethodInsn(INVOKESPECIAL, owner, name, Type.getMethodDescriptor(Type.VOID_TYPE, parameters), false);
        methodNode.visitInsn(Type.getReturnType(methodNode.desc).getOpcode(IRETURN));
    }

}
