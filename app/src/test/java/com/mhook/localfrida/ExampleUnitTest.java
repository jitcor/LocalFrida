package com.mhook.localfrida;

import com.mhook.localfrida.task.fridatask.FridaTask;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.yawk.dbus.protocol.DbusMessage;
import at.yawk.dbus.protocol.HeaderField;
import at.yawk.dbus.protocol.MessageFactory;
import at.yawk.dbus.protocol.MessageHeader;
import at.yawk.dbus.protocol.MessageType;
import at.yawk.dbus.protocol.object.BasicObject;
import at.yawk.dbus.protocol.object.ObjectPathObject;
import at.yawk.dbus.protocol.object.SignatureObject;
import at.yawk.dbus.protocol.type.BasicType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import java9.util.concurrent.CompletableFuture;
import java9.util.function.Function;
import java9.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void thenApply() {
        String result = CompletableFuture.supplyAsync(() -> "hello").thenApply(s -> s + " world").join();
        System.out.println(result);
        result = (String) CompletableFuture.supplyAsync(new Supplier<Object>() {
            @Override
            public Object get() {
                return "supplyAsync";
            }
        }).thenApply(new Function<Object, Object>() {
            @Override
            public Object apply(Object o) {
                return o+"thenApply";
            }
        }).join();
        System.out.println(result);

    }
    @Test
    public void testDbusObject(){
      DbusMessage message= MessageFactory.methodCall(
                "/re/frida/HostSession",
                "",
                "re.frida.HostSession12",
                "AttachTo",
                BasicObject.createUint32(22539));
    }
    public void testMessageEncoder(){
        new FridaTask("","",0);
    }
}