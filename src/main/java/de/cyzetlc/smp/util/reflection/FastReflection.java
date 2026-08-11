package de.cyzetlc.smp.util.reflection;

import org.bukkit.Bukkit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;

public final class FastReflection {
    private static final String NM_PACKAGE = "net.minecraft";
    public static final String OBC_PACKAGE = "org.bukkit.craftbukkit";
    public static final String NMS_PACKAGE = "net.minecraft.server";
    public static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().substring("org.bukkit.craftbukkit".length() + 1);
    private static final MethodType VOID_METHOD_TYPE;
    private static final boolean NMS_REPACKAGED;
    private static volatile Object theUnsafe;

    private FastReflection() {
        throw new UnsupportedOperationException();
    }

    public static boolean isRepackaged() {
        return NMS_REPACKAGED;
    }

    public static String nmsClassName(String post1_17package, String className) {
        if (NMS_REPACKAGED) {
            String classPackage = post1_17package == null ? "net.minecraft" : "net.minecraft." + post1_17package;
            return classPackage + '.' + className;
        } else {
            return "net.minecraft.server." + VERSION + '.' + className;
        }
    }

    public static Class<?> nmsClass(String post1_17package, String className) throws ClassNotFoundException {
        return Class.forName(nmsClassName(post1_17package, className));
    }

    public static Optional<Class<?>> nmsOptionalClass(String post1_17package, String className) {
        return optionalClass(nmsClassName(post1_17package, className));
    }

    public static String obcClassName(String className) {
        return "org.bukkit.craftbukkit." + VERSION + '.' + className;
    }

    public static Class<?> obcClass(String className) throws ClassNotFoundException {
        return Class.forName(obcClassName(className));
    }

    public static Optional<Class<?>> obcOptionalClass(String className) {
        return optionalClass(obcClassName(className));
    }

    public static Optional<Class<?>> optionalClass(String className) {
        try {
            return Optional.of(Class.forName(className));
        } catch (ClassNotFoundException var2) {
            return Optional.empty();
        }
    }

    public static Object enumValueOf(Class<?> enumClass, String enumName) {
        return Enum.valueOf(enumClass.asSubclass(Enum.class), enumName);
    }

    public static Object enumValueOf(Class<?> enumClass, String enumName, int fallbackOrdinal) {
        try {
            return enumValueOf(enumClass, enumName);
        } catch (IllegalArgumentException var5) {
            Object[] constants = enumClass.getEnumConstants();
            if (constants.length > fallbackOrdinal) {
                return constants[fallbackOrdinal];
            } else {
                throw var5;
            }
        }
    }

    static Class<?> innerClass(Class<?> parentClass, Predicate<Class<?>> classPredicate) throws ClassNotFoundException {
        Class<?>[] var2 = parentClass.getDeclaredClasses();
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Class<?> innerClass = var2[var4];
            if (classPredicate.test(innerClass)) {
                return innerClass;
            }
        }

        throw new ClassNotFoundException("No class in " + parentClass.getCanonicalName() + " matches the predicate.");
    }

    public static PacketConstructor findPacketConstructor(Class<?> packetClass, Lookup lookup) throws Exception {
        try {
            MethodHandle constructor = lookup.findConstructor(packetClass, VOID_METHOD_TYPE);
            return constructor::invoke;
        } catch (IllegalAccessException | NoSuchMethodException var7) {
            if (theUnsafe == null) {
                Class<?> var2 = FastReflection.class;
                synchronized(FastReflection.class) {
                    if (theUnsafe == null) {
                        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                        Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
                        theUnsafeField.setAccessible(true);
                        theUnsafe = theUnsafeField.get((Object)null);
                    }
                }
            }

            Method allocateMethod = theUnsafe.getClass().getMethod("allocateInstance", Class.class);
            return () -> allocateMethod.invoke(theUnsafe, packetClass);
        }
    }

    static {
        VOID_METHOD_TYPE = MethodType.methodType(Void.TYPE);
        NMS_REPACKAGED = optionalClass("net.minecraft.network.protocol.Packet").isPresent();
    }

    @FunctionalInterface
    public interface PacketConstructor {
        Object invoke() throws Throwable;
    }
}