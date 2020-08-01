package com.mhook.sample.tool.common.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Created by Louis on 2018/6/20.
 */

public class ReflectUtil {
    private static final HashMap<String, Field> fieldCache = new HashMap();
    private static final HashMap<String, Method> methodCache = new HashMap();

    public static Class<?> findMyClass(String className, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        try {
            return ClassUtils.getClass(classLoader, className, false);
        } catch (ClassNotFoundException var3) {
            throw new MyClassNotFoundError(var3.getCause());
        }
    }

    public static Object callMyMethod(Object obj, String methodName, Object... args) {
        try {
            return findMyMethodBestMatch(obj.getClass(), methodName, args).invoke(obj, args);
        } catch (IllegalAccessException var4) {
            throw new IllegalAccessError(var4.getMessage());
        } catch (IllegalArgumentException var5) {
            throw var5;
        } catch (InvocationTargetException var6) {
            throw new MyInvocationTargetError(var6.getCause());
        }
    }

    public static Object callMyMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            return findMyMethodBestMatch(obj.getClass(), methodName, parameterTypes, args).invoke(obj, args);
        } catch (IllegalAccessException var5) {
            throw new IllegalAccessError(var5.getMessage());
        } catch (IllegalArgumentException var6) {
            throw var6;
        } catch (InvocationTargetException var7) {
            throw new MyInvocationTargetError(var7.getCause());
        }
    }

    public static Object callMyStaticMethod(Class<?> clazz, String methodName, Object... args) {
        try {
            return findMyMethodBestMatch(clazz, methodName, args).invoke((Object) null, args);
        } catch (IllegalAccessException var4) {
            throw new IllegalAccessError(var4.getMessage());
        } catch (IllegalArgumentException var5) {
            throw var5;
        } catch (InvocationTargetException var6) {
            throw new MyInvocationTargetError(var6.getCause());
        }
    }

    public static Object callMyStaticMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            return findMyMethodBestMatch(clazz, methodName, parameterTypes, args).invoke((Object) null, args);
        } catch (IllegalAccessException var5) {
            throw new IllegalAccessError(var5.getMessage());
        } catch (IllegalArgumentException var6) {
            throw var6;
        } catch (InvocationTargetException var7) {
            throw new MyInvocationTargetError(var7.getCause());
        }
    }
    /**
     * Thrown when a class loader is unable to find a class. Unlike {@link ClassNotFoundException},
     * callers are not forced to explicitly catch this. If uncaught, the error will be passed to the
     * next caller in the stack.
     */
    public static final class MyClassNotFoundError extends Error {
        private static final long serialVersionUID = -1070936889459514628L;

        /** @hide */
        public MyClassNotFoundError(Throwable cause) {
            super(cause);
        }

        /** @hide */
        public MyClassNotFoundError(String detailMessage, Throwable cause) {
            super(detailMessage, cause);
        }
    }
    /**
     * This class provides a wrapper for an exception thrown by a method invocation.
     *
     * @see #callMyMethod(Object, String, Object...)
     * @see #callMyStaticMethod(Class, String, Object...)
     */
    public static final class MyInvocationTargetError extends Error {
        private static final long serialVersionUID = -1070936889459514628L;

        /** @hide */
        public MyInvocationTargetError(Throwable cause) {
            super(cause);
        }
    }
    public static Method findMyMethodBestMatch(Class<?> clazz, String methodName, Object... args) {
        return findMyMethodBestMatch(clazz, methodName, getMyParameterTypes(args));
    }

    /**
     *
     * Look up a method in a class and set it to accessible.
     *
     * <p>See {@link #findMyMethodBestMatch(Class, String, Class...)} for details. This variant
     * determines the parameter types from the classes of the given objects. For any item that is
     * {@code null}, the type is taken from {@code parameterTypes} instead.
     */
    public static Method findMyMethodBestMatch(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object[] args) {
        Class<?>[] argsClasses = null;
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] != null)
                continue;
            if (argsClasses == null)
                argsClasses = getMyParameterTypes(args);
            parameterTypes[i] = argsClasses[i];
        }
        return findMyMethodBestMatch(clazz, methodName, parameterTypes);
    }

    public static Method findMyMethodBestMatch(Class<?> clazz, String methodName, Class... parameterTypes) {
        StringBuilder sb = new StringBuilder(clazz.getName());
        sb.append('#');
        sb.append(methodName);
        sb.append(getMyParametersString(parameterTypes));
        sb.append("#bestmatch");
        String fullMethodName = sb.toString();
        Method bestMatch;
        if (methodCache.containsKey(fullMethodName)) {
            bestMatch = (Method) methodCache.get(fullMethodName);
            if (bestMatch == null) {
                throw new NoSuchMethodError(fullMethodName);
            } else {
                return bestMatch;
            }
        } else {
            try {
                bestMatch = findMyMethodExact(clazz, methodName, parameterTypes);
                methodCache.put(fullMethodName, bestMatch);
                return bestMatch;
            } catch (NoSuchMethodError var12) {
                bestMatch = null;
                Class<?> clz = clazz;
                boolean considerPrivateMethods = true;

                do {
                    Method[] var11;
                    int var10 = (var11 = clz.getDeclaredMethods()).length;

                    for (int var9 = 0; var9 < var10; ++var9) {
                        Method method = var11[var9];
                        if ((considerPrivateMethods || !Modifier.isPrivate(method.getModifiers()))
                                && method.getName().equals(methodName)
                                && ClassUtils.isAssignable(parameterTypes, method.getParameterTypes(), true)
                                && (bestMatch == null || MyMemberUtils.compareMyParameterTypes(method.getParameterTypes(), bestMatch.getParameterTypes(), parameterTypes) < 0)) {
                            bestMatch = method;
                        }
                    }

                    considerPrivateMethods = false;
                } while ((clz = clz.getSuperclass()) != null);

                if (bestMatch != null) {
                    bestMatch.setAccessible(true);
                    methodCache.put(fullMethodName, bestMatch);
                    return bestMatch;
                } else {
                    NoSuchMethodError e = new NoSuchMethodError(fullMethodName);
                    methodCache.put(fullMethodName, (Method) null);
                    throw e;
                }
            }
        }
    }

    private static String getMyParametersString(Class... clazzes) {
        StringBuilder sb = new StringBuilder("(");
        boolean first = true;
        Class[] var6 = clazzes;
        int var5 = clazzes.length;

        for (int var4 = 0; var4 < var5; ++var4) {
            Class<?> clazz = var6[var4];
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            if (clazz != null) {
                sb.append(clazz.getCanonicalName());
            } else {
                sb.append("null");
            }
        }

        sb.append(")");
        return sb.toString();
    }

    private static Class<?>[] getMyParameterTypes(Object... args) {
        Class[] clazzes = new Class[args.length];

        for (int i = 0; i < args.length; ++i) {
            clazzes[i] = args[i] != null ? args[i].getClass() : null;
        }

        return clazzes;
    }

    public static Method findMyMethodExact(Class<?> clazz, String methodName, Object... parameterTypes) {
        return findMyMethodExact(clazz, methodName, getMyParameterClasses(clazz.getClassLoader(), parameterTypes));
    }

    public static Method findMyMethodExact(String className, ClassLoader classLoader, String methodName, Object... parameterTypes) {
        return findMyMethodExact(findMyClass(className, classLoader), methodName, getMyParameterClasses(classLoader, parameterTypes));
    }

    public static Method findMyMethodExact(Class<?> clazz, String methodName, Class... parameterTypes) {
        StringBuilder sb = new StringBuilder(clazz.getName());
        sb.append('#');
        sb.append(methodName);
        sb.append(getMyParametersString(parameterTypes));
        sb.append("#exact");
        String fullMethodName = sb.toString();
        Method method;
        if (methodCache.containsKey(fullMethodName)) {
            method = (Method) methodCache.get(fullMethodName);
            if (method == null) {
                throw new NoSuchMethodError(fullMethodName);
            } else {
                return method;
            }
        } else {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                methodCache.put(fullMethodName, method);
                return method;
            } catch (NoSuchMethodException var6) {
                methodCache.put(fullMethodName, (Method) null);
                throw new NoSuchMethodError(fullMethodName);
            }
        }
    }

    /**
     * Look up a field in a class and set it to accessible. The result is cached.
     * If the field was not found, a {@link NoSuchFieldError} will be thrown.
     */
    public static Field findMyField(Class<?> clazz, String fieldName) {
        StringBuilder sb = new StringBuilder(clazz.getName());
        sb.append('#');
        sb.append(fieldName);
        String fullFieldName = sb.toString();

        if (fieldCache.containsKey(fullFieldName)) {
            Field field = fieldCache.get(fullFieldName);
            if (field == null)
                throw new NoSuchFieldError(fullFieldName);
            return field;
        }

        try {
            Field field = findMyFieldRecursiveImpl(clazz, fieldName);
            field.setAccessible(true);
            fieldCache.put(fullFieldName, field);
            return field;
        } catch (NoSuchFieldException e) {
            fieldCache.put(fullFieldName, null);
            throw new NoSuchFieldError(fullFieldName);
        }
    }
//#################################################################################################
    /** Sets the value of an object field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static void setMyObjectField(Object obj, String fieldName, Object value) {
        try {
            findMyField(obj.getClass(), fieldName).set(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a {@code boolean} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static void setMyBooleanField(Object obj, String fieldName, boolean value) {
        try {
            findMyField(obj.getClass(), fieldName).setBoolean(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a {@code byte} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static void setMyByteField(Object obj, String fieldName, byte value) {
        try {
            findMyField(obj.getClass(), fieldName).setByte(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a {@code char} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static void setMyCharField(Object obj, String fieldName, char value) {
        try {
            findMyField(obj.getClass(), fieldName).setChar(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a {@code double} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static void setMyDoubleField(Object obj, String fieldName, double value) {
        try {
            findMyField(obj.getClass(), fieldName).setDouble(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a {@code float} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static void setMyFloatField(Object obj, String fieldName, float value) {
        try {
            findMyField(obj.getClass(), fieldName).setFloat(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of an {@code int} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static void setMyIntField(Object obj, String fieldName, int value) {
        try {
            findMyField(obj.getClass(), fieldName).setInt(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a {@code long} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static void setMyLongField(Object obj, String fieldName, long value) {
        try {
            findMyField(obj.getClass(), fieldName).setLong(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a {@code short} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static void setMyShortField(Object obj, String fieldName, short value) {
        try {
            findMyField(obj.getClass(), fieldName).setShort(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    //#################################################################################################
    /** Returns the value of an object field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static Object getMyObjectField(Object obj, String fieldName) {
        try {
            return findMyField(obj.getClass(), fieldName).get(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** For inner classes, returns the surrounding instance, i.e. the {@code this} reference of the surrounding class. */
    public static Object getMySurroundingThis(Object obj) {
        return getMyObjectField(obj, "this$0");
    }

    /** Returns the value of a {@code boolean} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean getMyBooleanField(Object obj, String fieldName) {
        try {
            return findMyField(obj.getClass(), fieldName).getBoolean(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Returns the value of a {@code byte} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static byte getMyByteField(Object obj, String fieldName) {
        try {
            return findMyField(obj.getClass(), fieldName).getByte(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Returns the value of a {@code char} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static char getMyCharField(Object obj, String fieldName) {
        try {
            return findMyField(obj.getClass(), fieldName).getChar(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Returns the value of a {@code double} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static double getMyDoubleField(Object obj, String fieldName) {
        try {
            return findMyField(obj.getClass(), fieldName).getDouble(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Returns the value of a {@code float} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static float getMyFloatField(Object obj, String fieldName) {
        try {
            return findMyField(obj.getClass(), fieldName).getFloat(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Returns the value of an {@code int} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static int getMyIntField(Object obj, String fieldName) {
        try {
            return findMyField(obj.getClass(), fieldName).getInt(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Returns the value of a {@code long} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static long getMyLongField(Object obj, String fieldName) {
        try {
            return findMyField(obj.getClass(), fieldName).getLong(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Returns the value of a {@code short} field in the given object instance. A class reference is not sufficient! See also {@link #findMyField}. */
    public static short getMyShortField(Object obj, String fieldName) {
        try {
            return findMyField(obj.getClass(), fieldName).getShort(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    //#################################################################################################
    /** Sets the value of a static object field in the given class. See also {@link #findMyField}. */
    public static void setMyStaticObjectField(Class<?> clazz, String fieldName, Object value) {
        try {
            findMyField(clazz, fieldName).set(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a static {@code boolean} field in the given class. See also {@link #findMyField}. */
    public static void setMyStaticBooleanField(Class<?> clazz, String fieldName, boolean value) {
        try {
            findMyField(clazz, fieldName).setBoolean(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a static {@code byte} field in the given class. See also {@link #findMyField}. */
    public static void setMyStaticByteField(Class<?> clazz, String fieldName, byte value) {
        try {
            findMyField(clazz, fieldName).setByte(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a static {@code char} field in the given class. See also {@link #findMyField}. */
    public static void setMyStaticCharField(Class<?> clazz, String fieldName, char value) {
        try {
            findMyField(clazz, fieldName).setChar(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a static {@code double} field in the given class. See also {@link #findMyField}. */
    public static void setMyStaticDoubleField(Class<?> clazz, String fieldName, double value) {
        try {
            findMyField(clazz, fieldName).setDouble(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a static {@code float} field in the given class. See also {@link #findMyField}. */
    public static void setMyStaticFloatField(Class<?> clazz, String fieldName, float value) {
        try {
            findMyField(clazz, fieldName).setFloat(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a static {@code int} field in the given class. See also {@link #findMyField}. */
    public static void setMyStaticIntField(Class<?> clazz, String fieldName, int value) {
        try {
            findMyField(clazz, fieldName).setInt(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a static {@code long} field in the given class. See also {@link #findMyField}. */
    public static void setMyStaticLongField(Class<?> clazz, String fieldName, long value) {
        try {
            findMyField(clazz, fieldName).setLong(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a static {@code short} field in the given class. See also {@link #findMyField}. */
    public static void setMyStaticShortField(Class<?> clazz, String fieldName, short value) {
        try {
            findMyField(clazz, fieldName).setShort(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    //#################################################################################################
    /** Returns the value of a static object field in the given class. See also {@link #findMyField}. */
    public static Object getMyStaticObjectField(Class<?> clazz, String fieldName) {
        try {
            return findMyField(clazz, fieldName).get(null);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Returns the value of a static {@code boolean} field in the given class. See also {@link #findMyField}. */
    public static boolean getMyStaticBooleanField(Class<?> clazz, String fieldName) {
        try {
            return findMyField(clazz, fieldName).getBoolean(null);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a static {@code byte} field in the given class. See also {@link #findMyField}. */
    public static byte getMyStaticByteField(Class<?> clazz, String fieldName) {
        try {
            return findMyField(clazz, fieldName).getByte(null);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a static {@code char} field in the given class. See also {@link #findMyField}. */
    public static char getMyStaticCharField(Class<?> clazz, String fieldName) {
        try {
            return findMyField(clazz, fieldName).getChar(null);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a static {@code double} field in the given class. See also {@link #findMyField}. */
    public static double getMyStaticDoubleField(Class<?> clazz, String fieldName) {
        try {
            return findMyField(clazz, fieldName).getDouble(null);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a static {@code float} field in the given class. See also {@link #findMyField}. */
    public static float getMyStaticFloatField(Class<?> clazz, String fieldName) {
        try {
            return findMyField(clazz, fieldName).getFloat(null);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a static {@code int} field in the given class. See also {@link #findMyField}. */
    public static int getMyStaticIntField(Class<?> clazz, String fieldName) {
        try {
            return findMyField(clazz, fieldName).getInt(null);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a static {@code long} field in the given class. See also {@link #findMyField}. */
    public static long getMyStaticLongField(Class<?> clazz, String fieldName) {
        try {
            return findMyField(clazz, fieldName).getLong(null);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /** Sets the value of a static {@code short} field in the given class. See also {@link #findMyField}. */
    public static short getMyStaticShortField(Class<?> clazz, String fieldName) {
        try {
            return findMyField(clazz, fieldName).getShort(null);
        } catch (IllegalAccessException e) {
            // should not happen
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }
    //###########################内部方法，请勿调用及修改#################
    private static Field findMyFieldRecursiveImpl(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            while (true) {
                clazz = clazz.getSuperclass();
                if (clazz == null || clazz.equals(Object.class))
                    break;

                try {
                    return clazz.getDeclaredField(fieldName);
                } catch (NoSuchFieldException ignored) {
                }
            }
            throw e;
        }
    }

    private static Class<?>[] getMyParameterClasses(ClassLoader classLoader, Object[] parameterTypesAndCallback) {
        Class[] parameterClasses = null;

        for (int i = parameterTypesAndCallback.length - 1; i >= 0; --i) {
            Object type = parameterTypesAndCallback[i];
            if (type == null) {
                throw new MyClassNotFoundError("parameter type must not be null", (Throwable) null);
            }

            if (!(findMyClass("de.robv.android.xposed.XC_MethodHook",classLoader).isInstance(type))) {
                if (parameterClasses == null) {
                    parameterClasses = new Class[i + 1];
                }

                if (type instanceof Class) {
                    parameterClasses[i] = (Class) type;
                } else {
                    if (!(type instanceof String)) {
                        throw new MyClassNotFoundError("parameter type must either be specified as Class or String", (Throwable) null);
                    }

                    parameterClasses[i] = findMyClass((String) type, classLoader);
                }
            }
        }

        if (parameterClasses == null) {
            parameterClasses = new Class[0];
        }

        return parameterClasses;
    }

    static abstract class MyMemberUtils {
        private static final int ACCESS_TEST = 7;
        private static final Class<?>[] ORDERED_PRIMITIVE_TYPES;

        MyMemberUtils() {
        }

        static {
            Class[] clsArr = new Class[ACCESS_TEST];
            clsArr[0] = Byte.TYPE;
            clsArr[1] = Short.TYPE;
            clsArr[2] = Character.TYPE;
            clsArr[3] = Integer.TYPE;
            clsArr[4] = Long.TYPE;
            clsArr[5] = Float.TYPE;
            clsArr[6] = Double.TYPE;
            ORDERED_PRIMITIVE_TYPES = clsArr;
        }

        static void setAccessibleWorkaround(AccessibleObject o) {
            if (o != null && !o.isAccessible()) {
                Member m = (Member) o;
                if (Modifier.isPublic(m.getModifiers()) && isPackageAccess(m.getDeclaringClass().getModifiers())) {
                    try {
                        o.setAccessible(true);
                    } catch (SecurityException e) {
                    }
                }
            }
        }

        static boolean isPackageAccess(int modifiers) {
            return (modifiers & ACCESS_TEST) == 0;
        }

        static boolean isAccessible(Member m) {
            return (m == null || !Modifier.isPublic(m.getModifiers()) || m.isSynthetic()) ? false : true;
        }

        public static int compareMyParameterTypes(Class<?>[] left, Class<?>[] right, Class<?>[] actual) {
            float leftCost = getTotalTransformationCost(actual, left);
            float rightCost = getTotalTransformationCost(actual, right);
            if (leftCost < rightCost) {
                return -1;
            }
            return rightCost < leftCost ? 1 : 0;
        }

        private static float getTotalTransformationCost(Class<?>[] srcArgs, Class<?>[] destArgs) {
            float totalCost = 0.0f;
            for (int i = 0; i < srcArgs.length; i++) {
                totalCost += getObjectTransformationCost(srcArgs[i], destArgs[i]);
            }
            return totalCost;
        }

        private static float getObjectTransformationCost(Class<?> srcClass, Class<?> destClass) {
            if (destClass.isPrimitive()) {
                return getPrimitivePromotionCost(srcClass, destClass);
            }
            float cost = 0.0f;
            Class destClass2 = null;
            while (destClass2 != null && !destClass2.equals(srcClass)) {
                if (destClass2.isInterface() && ClassUtils.isAssignable((Class) srcClass, destClass2)) {
                    cost += 0.25f;
                    break;
                }
                cost += 1.0f;
                destClass2 = destClass2.getSuperclass();
            }
            if (destClass2 == null) {
                return cost + 1.5f;
            }
            return cost;
        }

        private static float getPrimitivePromotionCost(Class<?> srcClass, Class<?> destClass) {
            float cost = 0.0f;
            Class<?> cls = srcClass;
            if (!cls.isPrimitive()) {
                cost = 0.0f + 0.1f;
                cls = ClassUtils.wrapperToPrimitive(cls);
            }
            int i = 0;
            while (cls != destClass && i < ORDERED_PRIMITIVE_TYPES.length) {
                if (cls == ORDERED_PRIMITIVE_TYPES[i]) {
                    cost += 0.1f;
                    if (i < ORDERED_PRIMITIVE_TYPES.length - 1) {
                        cls = ORDERED_PRIMITIVE_TYPES[i + 1];
                    }
                }
                i++;
            }
            return cost;
        }
    }
}
