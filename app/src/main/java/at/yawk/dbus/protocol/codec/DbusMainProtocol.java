/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.codec;

import at.yawk.dbus.protocol.MessageConsumer;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yawkat
 */
public class DbusMainProtocol extends ChannelDuplexHandler {
    private final MessageConsumer consumer;

    public DbusMainProtocol(MessageConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        add(ctx, new ByteCollector());

        add(ctx, new MessageHeaderCodec());

        add(ctx, new BodyDecoder());
        add(ctx, new BodyEncoder());

        add(ctx, new IncomingMessageAdapter(consumer));

        ctx.pipeline().remove(this);
    }

    private void add(ChannelHandlerContext ctx, ChannelHandler handler) {
        ctx.pipeline().addBefore(ctx.executor(), ctx.name(), null, handler);
    }
}
