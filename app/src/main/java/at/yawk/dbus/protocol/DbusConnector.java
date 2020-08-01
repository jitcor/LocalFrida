/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.yawk.dbus.protocol.auth.AuthClient;
import at.yawk.dbus.protocol.auth.mechanism.AuthMechanism;
import at.yawk.dbus.protocol.auth.mechanism.ExternalAuthMechanism;
import at.yawk.dbus.protocol.codec.DbusMainProtocol;
import at.yawk.dbus.protocol.object.DbusObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;

import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java9.util.concurrent.CompletionStage;

import lombok.Setter;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
public class DbusConnector {
    private final Logger log = LoggerFactory.getLogger(DbusConnector.class);
    private final Bootstrap bootstrap;
    /**
     * The consumer to use for initial messages.
     */
    @Setter
    private MessageConsumer initialConsumer = MessageConsumer.DISCARD;
    @Setter
    private AuthMechanism authMechanism;

    public DbusConnector() {
        bootstrap = new Bootstrap();
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.config().setAutoRead(false);
            }
        });
    }

    /**
     * Connect to the dbus server at the given {@link SocketAddress}.
     */
    public DbusChannel connect(SocketAddress address) throws Exception {
        Bootstrap localBootstrap = bootstrap.clone();
        if (address instanceof DomainSocketAddress) {
            localBootstrap.group(new EpollEventLoopGroup());
            localBootstrap.channel(EpollDomainSocketChannel.class);
        } else {
            localBootstrap.group(new NioEventLoopGroup());
            localBootstrap.channel(NioSocketChannel.class);
        }

        Channel channel = localBootstrap.connect(address).sync().channel();

        AuthClient authClient = new AuthClient();
        if (LoggingInboundAdapter.isEnabled()) {
            channel.pipeline().addLast(new LoggingInboundAdapter());
        }

        channel.pipeline().addLast("auth", authClient);
        channel.config().setAutoRead(true);
        log.trace("Pipeline is now {}", channel.pipeline());

        // I really don't get why dbus does this
        channel.write(Unpooled.wrappedBuffer(new byte[]{0}));

        if (authMechanism == null) {
            authMechanism = new ExternalAuthMechanism();
        }
        CompletionStage<?> completionPromise = authClient.startAuth(channel, authMechanism);

        SwappableMessageConsumer swappableConsumer = new SwappableMessageConsumer(initialConsumer);
        completionPromise.toCompletableFuture().thenRun(() -> {
            channel.pipeline().replace("auth", "main", new DbusMainProtocol(swappableConsumer));
            log.trace("Pipeline is now {}", channel.pipeline());
        }).get();

        DbusChannelImpl dbusChannel = new DbusChannelImpl(channel, swappableConsumer);

        return dbusChannel;
    }

    public DbusChannel connect(DbusAddress address) throws Exception {
        log.info("Connecting to dbus server at {}", address);

        switch (address.getProtocol()) {
            case "tcp":
                String host = address.getProperty("host");
                int port = Integer.parseInt(address.getProperty("port"));
                return connect(new InetSocketAddress(host, port));
            default:
                throw new UnsupportedOperationException("Unsupported protocol " + address.getProtocol());
        }
    }


}
