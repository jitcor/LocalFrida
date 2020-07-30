/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.auth;

import at.yawk.dbus.protocol.auth.command.AuthDirection;
import at.yawk.dbus.protocol.auth.mechanism.AuthMechanism;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import java.util.Arrays;
import java.util.List;
import java9.util.concurrent.CompletionStage;

/**
 * @author yawkat
 */
public class AuthClient extends ChannelHandlerAdapter {
    private final AuthClientHandler clientHandler = new AuthClientHandler();

    private final List<ChannelHandler> handlers = Arrays.asList(
            new CommandCodec(),
            new DirectionValidatorAdapter(AuthDirection.FROM_SERVER, AuthDirection.FROM_CLIENT),
            clientHandler
    );

    public CompletionStage<?> startAuth(Channel channel, AuthMechanism mechanism) throws Exception {
        return clientHandler.startAuth(channel, mechanism);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        for (ChannelHandler handler : handlers) {
            ctx.pipeline().addBefore(ctx.executor(), ctx.name(), null, handler);
        }
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        for (ChannelHandler handler : handlers) {
            ctx.pipeline().remove(handler);
        }
        super.handlerRemoved(ctx);
    }
}
