/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.auth;

import at.yawk.dbus.protocol.auth.command.Command;
import at.yawk.dbus.protocol.auth.mechanism.AuthMechanism;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java9.util.concurrent.CompletableFuture;
import java9.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
class AuthClientHandler extends SimpleChannelInboundHandler<Command> {
    private CompletableFuture<Command> currentCommandFuture = null;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return msg instanceof Command;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
        // may be modified in .complete
        CompletableFuture<Command> future = currentCommandFuture;
        if (future == null) { throw new UnexpectedCommandException(msg); }
        currentCommandFuture = null;
        future.complete(msg);
    }

    CompletionStage<?> startAuth(Channel channel, AuthMechanism mechanism) throws Exception {
        return mechanism.startAuth(command -> {
            currentCommandFuture = new CompletableFuture<>();
            channel.writeAndFlush(command);
            return currentCommandFuture;
        });
    }

    private ChannelFuture write(Channel channel, Command msg) {
        return channel.writeAndFlush(msg, channel.voidPromise());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception in channel", cause);

        CompletableFuture<Command> future = currentCommandFuture;
        if (future != null) {
            // may be modified in .completeExceptionally
            currentCommandFuture = null;
            future.completeExceptionally(cause);
        }
    }
}
