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

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import net.minecrell.quartz.launch.mappings.Access;
import net.minecrell.quartz.launch.mappings.AccessMapping;
import net.minecrell.quartz.launch.mappings.Mappings;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AccessTransformer extends MappingTreeTransformer {

    public AccessTransformer() {
    }

    protected AccessTransformer(Mappings mappings) {
        super(mappings);
    }

    @Override
    protected boolean transform(String name, String transformedName) {
        return mappings.getAccessMappings().containsRow(transformedName);
    }

    @Override
    protected void transform(String name, String transformedName, ClassNode classNode) {
        List<MethodNode> overridable = null;

        for (Map.Entry<String, AccessMapping> entry : mappings.getAccessMappings().row(transformedName).entrySet()) {
            String target = entry.getKey();
            AccessMapping accessMapping = entry.getValue();

            if (target.isEmpty()) {
                // Class mapping
                accessMapping.transform(classNode);
            } else if (target.indexOf('(') >= 0) {
                int len = target.length();

                // Method mapping
                for (MethodNode methodNode : classNode.methods) {
                    // Fast check before we look more intensively
                    if (methodNode.name.length() + methodNode.desc.length() != len
                            || !(target.startsWith(methodNode.name) && target.endsWith(methodNode.desc))) continue;

                    boolean wasPrivate = Access.PRIVATE.is(methodNode.access);
                    accessMapping.transform(methodNode);

                    // Constructors always use INVOKESPECIAL
                    // If we changed from private to something else we need to replace all INVOKESPECIAL calls to this method with INVOKEVIRTUAL
                    // So that overridden methods will be called. Only need to scan this class, because obviously the method was private.
                    if (wasPrivate && accessMapping.getAccess() != Access.PRIVATE && !methodNode.name.equals("<init>")) {
                        if (overridable == null) {
                            overridable = new ArrayList<>(3);
                        }

                        overridable.add(methodNode);
                    }

                    break;
                }
            } else {
                // Field mapping
                for (FieldNode fieldNode : classNode.fields) {
                    if (target.equals(fieldNode.name)) {
                        accessMapping.transform(fieldNode);
                        break;
                    }
                }
            }
        }

        if (overridable != null) {
            for (MethodNode methodNode : classNode.methods) {
                for (Iterator<AbstractInsnNode> itr = methodNode.instructions.iterator(); itr.hasNext(); ) {
                    AbstractInsnNode insn = itr.next();
                    if (insn.getOpcode() == INVOKESPECIAL) {
                        MethodInsnNode mInsn = (MethodInsnNode) insn;
                        for (MethodNode replace : overridable) {
                            if (replace.name.equals(mInsn.name) && replace.desc.equals(mInsn.desc)) {
                                mInsn.setOpcode(INVOKEVIRTUAL);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

}
