package com.mhook.localfrida.task.fridatask;

import android.content.Intent;
import android.support.annotation.Keep;
import android.text.TextUtils;

import com.mhook.libfridaapi.FridaApi;
import com.mhook.libfridaapi.OnFridaListener;
import com.mhook.localfrida.tool.Debug;
import com.mhook.localfrida.tool.app.App;
import com.mhook.localfrida.tool.go.Channel;
import com.mhook.localfrida.tool.go.Go;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.yawk.dbus.protocol.DbusChannel;
import at.yawk.dbus.protocol.DbusMessage;
import at.yawk.dbus.protocol.MessageConsumer;
import at.yawk.dbus.protocol.MessageFactory;
import at.yawk.dbus.protocol.MessageHeader;
import at.yawk.dbus.protocol.object.ArrayObject;
import at.yawk.dbus.protocol.object.BasicObject;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.StructObject;
import at.yawk.dbus.protocol.type.ArrayTypeDefinition;
import at.yawk.dbus.protocol.type.BasicType;
import at.yawk.dbus.protocol.type.StructTypeDefinition;
import at.yawk.dbus.protocol.type.TypeDefinition;
import lombok.RequiredArgsConstructor;


@Keep
@RequiredArgsConstructor
public class FridaTask implements MessageConsumer,FridaApi {
    public static final String TAG = "FridaTask";
    private DbusChannel channel;
    private Channel<DbusMessage> mChannelDbusMessage = new Channel<>();
    private final String process;
    private final String script;
    private final Integer port;
    private OnFridaListener fridaTaskListener;
    @Override
    public void setFridaTaskListener(OnFridaListener fridaTaskListener) {
        this.fridaTaskListener=fridaTaskListener;
    }

    @Keep
    public void start() {
        Go.go(() -> {
            try {
                channel = new TcpDbusConnector().connectTcp(App.FRIDA_SERVER_IP,port==-1?App.FRIDA_SERVER_PORT:port);
                //设置回调监听
                channel.setMessageConsumer(this);
                //注入代码
                inject(process, script);
                if (fridaTaskListener != null) {
                    fridaTaskListener.onStarted();
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

    private void inject(String process, String script) {
        Go.go(() -> {
            channel.write(MessageFactory.methodCall(
                    "/re/frida/HostSession",
                    "",
                    "re.frida.HostSession12",
                    "EnumerateProcesses"));
            DbusMessage message = this.mChannelDbusMessage.take();
            if (message.getBody() != null && message.getBody().getArguments() != null && message.getBody().getArguments().get(0) != null) {
                for (DbusObject dbusObject : message.getBody().getArguments().get(0).getValues()) {
                    if (dbusObject != null) {
                        int pid = dbusObject.get(0).intValue();
                        String packageName = dbusObject.get(1).stringValue();
                        Debug.LogI(TAG, String.format("pid:%s--process:%s", pid, packageName));
                        if (TextUtils.equals(packageName, process)) {
                            channel.write(MessageFactory.methodCall(
                                    "/re/frida/HostSession",
                                    "",
                                    "re.frida.HostSession12",
                                    "AttachTo",
                                    BasicObject.createUint32(pid)));
                            message = this.mChannelDbusMessage.take();
                            int sessionId = message.getBody() != null ? message.getBody().getArguments().get(0).get(0).intValue() : 0;
                            Debug.LogI(TAG, "sessionID:", sessionId);
                            String agentPath = "/re/frida/AgentSession/" + sessionId;
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
                            return;
                        }
                    }
                }
            }
        });

    }

    @Keep
    public void stop() {
        Go.go(() -> {
            if (channel != null) {
                channel.disconnect();
            }
            Debug.LogI(TAG, "stop...");
        });
    }


    @Override
    public boolean requireAccept(MessageHeader header) {
        return true;
    }

    @Override
    public void accept(DbusMessage message) {
        Debug.LogI(TAG, "accept:", message.getHeader().getSerial());
        this.mChannelDbusMessage.put(message);
        Debug.LogI(TAG, "accept:done:", message.getHeader().getSerial());
    }


}
