package com.mhook.localfrida.tool.dbus;

import java.util.ArrayList;
import java.util.Arrays;

import at.yawk.dbus.protocol.object.ArrayObject;
import at.yawk.dbus.protocol.object.BasicObject;

public class DbusObjectEx {
    //ArrayObject.create(DbusTypeDef.ARRAY_STRING,Arrays.asList(BasicObject.createString(""))),
    public static <T> ArrayObject createSingeArray(T value) {
        if (value instanceof String) {
            return ArrayObject.create(DbusTypeDef.ARRAY_STRING, Arrays.asList(BasicObject.createString(String.valueOf(value))));
        }
        if (value instanceof Boolean) {
            return ArrayObject.create(DbusTypeDef.ARRAY_BOOLEAN, Arrays.asList(BasicObject.createBoolean((Boolean) value)));
        }
        if (value instanceof Integer) {
            return ArrayObject.create(DbusTypeDef.ARRAY_INT32, Arrays.asList(BasicObject.createInt32((Integer) value)));
        }
        if (value instanceof Byte) {
            return ArrayObject.create(DbusTypeDef.ARRAY_BYTE, Arrays.asList(BasicObject.createByte((Byte) value)));
        }
        return null;
    }

    public static <T> ArrayObject createEmptyArray(Class<?> cls) {
        if (cls == String.class) {
            return ArrayObject.create(DbusTypeDef.ARRAY_STRING, new ArrayList<>());
        }
        if (cls == Boolean.class) {
            return ArrayObject.create(DbusTypeDef.ARRAY_BOOLEAN, new ArrayList<>());
        }
        if (cls == Integer.class) {
            return ArrayObject.create(DbusTypeDef.ARRAY_INT32, new ArrayList<>());
        }
        if (cls == Byte.class) {
            return ArrayObject.create(DbusTypeDef.ARRAY_BYTE, new ArrayList<>());
        }
        return null;
    }

}
