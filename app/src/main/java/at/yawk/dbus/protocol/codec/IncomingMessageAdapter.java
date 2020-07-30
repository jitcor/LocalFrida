/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.codec;

import at.yawk.dbus.protocol.DbusMessage;
import at.yawk.dbus.protocol.MessageBody;
import at.yawk.dbus.protocol.MessageConsumer;
import at.yawk.dbus.protocol.MessageHeader;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
@RequiredArgsConstructor
class IncomingMessageAdapter extends SimpleChannelInboundHandler<Object> {
    private final MessageConsumer consumer;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Attribute<MessageHeader> headerAttribute = ctx.channel().attr(Local.CURRENT_HEADER);
        if (msg instanceof MessageHeader) {
            if (((MessageHeader) msg).getMessageBodyLength() == 0) {
                DbusMessage message = new DbusMessage();
                message.setHeader((MessageHeader) msg);
                consumer.accept(message);
            } else {
                if (consumer.requireAccept((MessageHeader) msg)) {
                    headerAttribute.set((MessageHeader) msg);
                } else {
                    headerAttribute.set(null);
                }
            }
        } else if (msg instanceof MessageBody) {
            MessageHeader header = headerAttribute.get();
            if (header != null) {
                DbusMessage message = new DbusMessage();
                message.setHeader(header);
                message.setBody((MessageBody) msg);
                consumer.accept(message);
                headerAttribute.set(null);
            }
        } else {
            log.warn("Did not handle {}", msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Error in dbus pipeline", cause);
        ctx.close();
    }
}
