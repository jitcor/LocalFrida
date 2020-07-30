/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.auth;

import at.yawk.dbus.protocol.auth.command.AuthDirection;
import at.yawk.dbus.protocol.auth.command.Command;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DecoderException;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
class DirectionValidatorAdapter extends ChannelDuplexHandler {
    private final AuthDirection inbound;
    private final AuthDirection outbound;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        validate(msg, inbound);
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        validate(msg, outbound);
        super.write(ctx, msg, promise);
    }

    private void validate(Object msg, AuthDirection expectedProtocol) {
        if (msg instanceof Command) {
            Command command = (Command) msg;
            if (command.getDirection() != null && command.getDirection() != expectedProtocol) {
                throw new DecoderException("Invalid command (wrong protocol direction): " + command);
            }
        }
    }
}
