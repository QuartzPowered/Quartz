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
