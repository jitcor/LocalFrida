package com.mhook.localfrida.task.fridatask;

import java.net.InetSocketAddress;
import java.nio.file.Paths;

import at.yawk.dbus.protocol.DbusAddress;
import at.yawk.dbus.protocol.DbusChannel;
import at.yawk.dbus.protocol.DbusConnector;
import at.yawk.dbus.protocol.MessageFactory;
import at.yawk.dbus.protocol.auth.mechanism.AnonymousAuthMechanism;

public class TcpDbusConnector extends DbusConnector {
    public DbusChannel connectTcp(String ip,int port) throws Exception {
        //设置认证机制：匿名
        setAuthMechanism(new AnonymousAuthMechanism());
        return connect(DbusAddress.fromTcpAddress(new InetSocketAddress(ip, port)));
    }
}
