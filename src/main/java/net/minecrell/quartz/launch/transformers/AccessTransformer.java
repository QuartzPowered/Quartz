package net.minecrell.quartz.launch.transformers;

import static java.util.Objects.requireNonNull;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecrell.quartz.launch.mappings.Access;
import net.minecrell.quartz.launch.mappings.AccessMapping;
import net.minecrell.quartz.launch.mappings.Mappings;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AccessTransformer implements IClassTransformer {

    private final Mappings mappings;

    public AccessTransformer() {
        this((Mappings) Launch.blackboard.get("quartz.mappings"));
    }

    protected AccessTransformer(Mappings mappings) {
        this.mappings = requireNonNull(mappings, "mappings");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        if (!mappings.getAccessMappings().containsRow(transformedName)) {
            return bytes;
        }

        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(classNode, 0);

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

        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
