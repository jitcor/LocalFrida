package com.mhook.localfrida.tool.dbus;

import at.yawk.dbus.protocol.type.ArrayTypeDefinition;
import at.yawk.dbus.protocol.type.BasicType;

public class DbusTypeDef {
    public static ArrayTypeDefinition ARRAY_STRING=new ArrayTypeDefinition(BasicType.STRING);
    public static ArrayTypeDefinition ARRAY_BYTE=new ArrayTypeDefinition(BasicType.BYTE);
    public static ArrayTypeDefinition ARRAY_BOOLEAN=new ArrayTypeDefinition(BasicType.BOOLEAN);
    public static ArrayTypeDefinition ARRAY_INT32=new ArrayTypeDefinition(BasicType.INT32);
}
