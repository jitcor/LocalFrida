package com.mhook.localfrida.task.fridatask;

import android.content.Intent;
import android.support.annotation.Keep;
import android.text.TextUtils;

import com.mhook.libfridaapi.FridaApi;
import com.mhook.libfridaapi.OnFridaListener;
import com.mhook.localfrida.tool.Debug;
import com.mhook.localfrida.tool.app.App;
import com.mhook.localfrida.tool.dbus.DbusObjectEx;
import com.mhook.localfrida.tool.dbus.DbusTypeDef;
import com.mhook.localfrida.tool.go.Channel;
import com.mhook.localfrida.tool.go.Go;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.yawk.dbus.protocol.DbusChannel;
import at.yawk.dbus.protocol.DbusMessage;
import at.yawk.dbus.protocol.HeaderField;
import at.yawk.dbus.protocol.MessageConsumer;
import at.yawk.dbus.protocol.MessageFactory;
import at.yawk.dbus.protocol.MessageHeader;
import at.yawk.dbus.protocol.MessageType;
import at.yawk.dbus.protocol.object.ArrayObject;
import at.yawk.dbus.protocol.object.BasicObject;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.ObjectPathObject;
import at.yawk.dbus.protocol.object.StringObject;
import at.yawk.dbus.protocol.object.StructObject;
import at.yawk.dbus.protocol.type.ArrayTypeDefinition;
import at.yawk.dbus.protocol.type.BasicType;
import at.yawk.dbus.protocol.type.StructTypeDefinition;
import at.yawk.dbus.protocol.type.TypeDefinition;
import at.yawk.dbus.protocol.type.TypeParser;
import javas.util.Arrays_;
import lombok.RequiredArgsConstructor;


@Keep
@RequiredArgsConstructor
public class FridaTask implements MessageConsumer, FridaApi {
    public static final String TAG = "FridaTask";
    private DbusChannel channel;
    private Channel<DbusMessage> mChannelDbusMessage;
    private final String process;
    private final String script;
    private final Integer port;
    private final Boolean isReboot;
    private OnFridaListener fridaTaskListener;
    private String agentPath = "";
    private boolean stopped = false;

    @Override
    public void setFridaTaskListener(OnFridaListener fridaTaskListener) {
        this.fridaTaskListener = fridaTaskListener;
    }

    @Keep
    public void start() {
        if (stopped) {
            Debug.LogE(TAG, "current seesion is stopped");
            return;
        }
        if (channel != null) {
            Debug.LogE(TAG, "channel is connected");
            return;
        }
        Go.go(() -> {
            try {
                channel = new TcpDbusConnector().connectTcp(App.FRIDA_SERVER_IP, port == -1 ? App.FRIDA_SERVER_PORT : port);
                //设置回调监听
                channel.setMessageConsumer(this);
                //注入代码
                if (isReboot) {
                    rebootAndInject(process, script);
                } else {
                    inject(process, script);
                }

                channel.closeStage().toCompletableFuture().get();
            } catch (Exception e) {
                e.printStackTrace();
                stop();
            }

            if (fridaTaskListener != null) {
                fridaTaskListener.onStopped();
            }
        });
    }

    private void rebootAndInject(String process, String script) {
        Go.go(() -> {
            try {
                mChannelDbusMessage = new Channel<>();
                channel.write(MessageFactory.methodCall(
                        "/re/frida/HostSession",
                        "",
                        "re.frida.HostSession12",
                        "Spawn",
                        BasicObject.createString(process),
                        StructObject.create(
                                (StructTypeDefinition) TypeParser.parseTypeDefinition("(basbasbassiay)"),
                                Arrays.asList(
                                        BasicObject.createBoolean(false),
                                        DbusObjectEx.createEmptyArray(String.class),
                                        BasicObject.createBoolean(false),
                                        DbusObjectEx.createEmptyArray(String.class),
                                        BasicObject.createBoolean(false),
                                        DbusObjectEx.createEmptyArray(String.class),
                                        BasicObject.createString(""),
                                        BasicObject.createInt32(0),
                                        DbusObjectEx.createEmptyArray(Byte.class)
                                ))

                ));
                this.mChannelDbusMessage.take();
                this.mChannelDbusMessage.take();
                this.mChannelDbusMessage.take();
                DbusMessage message = this.mChannelDbusMessage.take();

                if (message.getBody() == null || message.getBody().getArguments() == null || message.getBody().getArguments().get(0) == null) {
                    Debug.LogE(TAG,"message body is null...");
                    if (fridaTaskListener != null) {
                        fridaTaskListener.onError("message body is null...");
                    }
                    return;
                }
                int pid = message.getBody().getArguments().get(0).intValue();
                channel.write(MessageFactory.methodCall(
                        "/re/frida/HostSession",
                        "",
                        "re.frida.HostSession12",
                        "AttachTo",
                        BasicObject.createUint32(pid)));
                message = this.mChannelDbusMessage.take();
                int sessionId = message.getBody() != null ? message.getBody().getArguments().get(0).get(0).intValue() : 0;
                Debug.LogI(TAG, "sessionID:", sessionId);
                agentPath = "/re/frida/AgentSession/" + sessionId;
                ArrayTypeDefinition type = new ArrayTypeDefinition(BasicType.BYTE);
                List<DbusObject> values = new ArrayList<>();
//                            values.add(BasicObject.createByte((byte)0));
                ArrayObject arrayObj = ArrayObject.create(type, values);

                List<TypeDefinition> types = new ArrayList<>();
                types.add(new ArrayTypeDefinition(BasicType.BYTE));
                StructTypeDefinition type2 = new StructTypeDefinition(types);
                List<DbusObject> values2 = new ArrayList<>();
                values2.add(arrayObj);
                channel.write(
                        MessageFactory.methodCall(
                                agentPath,
                                "",
                                "re.frida.AgentSession12",
                                "CreateScriptWithOptions",
                                BasicObject.createString(script),
//                                            BasicObject.createString("")
                                StructObject.create(type2, values2)

                        )
                );
                message = this.mChannelDbusMessage.take();
                int scriptID = message.getBody() != null ? message.getBody().getArguments().get(0).get(0).intValue() : 0;
                Debug.LogI(TAG, "scriptID:", scriptID);
                Debug.LogI(TAG, "CreateScript message:", message);
                channel.write(MessageFactory.methodCall(
                        agentPath,
                        "",
                        "re.frida.AgentSession12",
                        "LoadScript", StructObject.create(new StructTypeDefinition(Arrays.asList(BasicType.UINT32)), Arrays.asList(BasicObject.createUint32(scriptID)))));
                message = this.mChannelDbusMessage.take();
                Debug.LogI(TAG, "LoadScript message:", message);
                channel.write(MessageFactory.methodCall(
                        "/re/frida/HostSession",
                        "",
                        "re.frida.HostSession12",
                        "Resume",
                        BasicObject.createUint32(pid)));
                message = this.mChannelDbusMessage.take();
                Debug.LogI(TAG, "Resume message:", message);
                if (fridaTaskListener != null) {
                    fridaTaskListener.onStarted();
                }
                do {
                    message = this.mChannelDbusMessage.take();
                    Debug.LogI(TAG, "while message:", message);
                    if (message.getHeader().getMessageType() == MessageType.SIGNAL &&
                            TextUtils.equals((message.getHeader().getHeaderFields().get(HeaderField.MEMBER)).stringValue(), "MessageFromScript")) {
                        String msg = message.getBody() != null ? message.getBody().getArguments().get(1).stringValue() : "";
                        if (!TextUtils.isEmpty(msg)) {
                            if (fridaTaskListener != null) {
                                fridaTaskListener.onMessage(msg);
                            }
                        }
                    }
                }
                while (!stopped);
                stop();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                if (fridaTaskListener != null) {
                    fridaTaskListener.onError(throwable.getMessage());
                }
                stop();
            }
        });

    }

    private void inject(String process, String script) {
        Go.go(() -> {
            try {
                mChannelDbusMessage = new Channel<>();
                channel.write(MessageFactory.methodCall(
                        "/re/frida/HostSession",
                        "",
                        "re.frida.HostSession12",
                        "EnumerateProcesses"));
                DbusMessage message = this.mChannelDbusMessage.take();
                if (message.getBody() != null && message.getBody().getArguments() != null && message.getBody().getArguments().get(0) != null) {
                    for (DbusObject dbusObject : message.getBody().getArguments().get(0).getValues()) {
                        if (dbusObject == null) {
                            continue;
                        }
                        int pid = dbusObject.get(0).intValue();
                        String packageName = dbusObject.get(1).stringValue();
//                        Debug.LogI(TAG, String.format("pid:%s--process:%s", pid, packageName));
                        if (!TextUtils.equals(packageName, process)) {
                            continue;
                        }
                        channel.write(MessageFactory.methodCall(
                                "/re/frida/HostSession",
                                "",
                                "re.frida.HostSession12",
                                "AttachTo",
                                BasicObject.createUint32(pid)));
                        message = this.mChannelDbusMessage.take();
                        int sessionId = message.getBody() != null ? message.getBody().getArguments().get(0).get(0).intValue() : 0;
                        Debug.LogI(TAG, "sessionID:", sessionId);
                        agentPath = "/re/frida/AgentSession/" + sessionId;
                        ArrayTypeDefinition type = new ArrayTypeDefinition(BasicType.BYTE);
                        List<DbusObject> values = new ArrayList<>();
//                            values.add(BasicObject.createByte((byte)0));
                        ArrayObject arrayObj = ArrayObject.create(type, values);

                        List<TypeDefinition> types = new ArrayList<>();
                        types.add(new ArrayTypeDefinition(BasicType.BYTE));
                        StructTypeDefinition type2 = new StructTypeDefinition(types);
                        List<DbusObject> values2 = new ArrayList<>();
                        values2.add(arrayObj);
                        channel.write(
                                MessageFactory.methodCall(
                                        agentPath,
                                        "",
                                        "re.frida.AgentSession12",
                                        "CreateScriptWithOptions",
                                        BasicObject.createString(script),
//                                            BasicObject.createString("")
                                        StructObject.create(type2, values2)

                                )
                        );
                        message = this.mChannelDbusMessage.take();
                        int scriptID = message.getBody() != null ? message.getBody().getArguments().get(0).get(0).intValue() : 0;
                        Debug.LogI(TAG, "scriptID:", scriptID);
                        Debug.LogI(TAG, "CreateScript message:", message);
                        channel.write(MessageFactory.methodCall(
                                agentPath,
                                "",
                                "re.frida.AgentSession12",
                                "LoadScript", StructObject.create(new StructTypeDefinition(Arrays.asList(BasicType.UINT32)), Arrays.asList(BasicObject.createUint32(scriptID)))));
                        message = this.mChannelDbusMessage.take();
                        Debug.LogI(TAG, "LoadScript message:", message);
                        if (fridaTaskListener != null) {
                            fridaTaskListener.onStarted();
                        }
                        do {
                            message = this.mChannelDbusMessage.take();
                            Debug.LogI(TAG, "while message:", message);
                            if (message.getHeader().getMessageType() == MessageType.SIGNAL &&
                                    TextUtils.equals(((StringObject) message.getHeader().getHeaderFields().get(HeaderField.MEMBER)).stringValue(), "MessageFromScript")) {
                                String msg = message.getBody() != null ? message.getBody().getArguments().get(1).stringValue() : "";
                                if (!TextUtils.isEmpty(msg)) {
                                    if (fridaTaskListener != null) {
                                        fridaTaskListener.onMessage(msg);
                                    }
                                }
                            }
                        }
                        while (!stopped);
                        return;
                    }
                    if (fridaTaskListener != null) {
                        fridaTaskListener.onError("not found process:" + process);
                    }
                    stop();
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                if (fridaTaskListener != null) {
                    fridaTaskListener.onError(throwable.getMessage());
                }
                stop();
            }
        });

    }

    @Keep
    public void stop() {
        Go.go(() -> {
            stopped = true;
            if (channel != null) {
                channel.disconnect();
                channel = null;
                agentPath = null;
            }
            Debug.LogI(TAG, "stop...");
        });
    }


    @Override
    public boolean requireAccept(MessageHeader header) {
        if (TextUtils.isEmpty(agentPath)) {
            return true;
        }
        if (header == null) {
            return false;
        }
        DbusObject dbusObject = header.getHeaderFields().get(HeaderField.PATH);
        if (dbusObject instanceof ObjectPathObject) {
            String path = ((ObjectPathObject) dbusObject).stringValue();
            if (!TextUtils.equals(path, agentPath)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void accept(DbusMessage message) {
        if (message == null) {
            return;
        }
        Debug.LogI(TAG, "accept:", message);
        if (mChannelDbusMessage != null) {
            this.mChannelDbusMessage.put(message);
        }
    }


}
