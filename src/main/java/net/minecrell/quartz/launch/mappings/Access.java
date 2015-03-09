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
