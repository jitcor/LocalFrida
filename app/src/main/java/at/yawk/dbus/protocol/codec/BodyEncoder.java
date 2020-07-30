/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.codec;

import at.yawk.dbus.protocol.DbusMessage;
import at.yawk.dbus.protocol.HeaderField;
import at.yawk.dbus.protocol.MessageBody;
import at.yawk.dbus.protocol.MessageHeader;
import at.yawk.dbus.protocol.object.AlignableByteBuf;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.SignatureObject;
import at.yawk.dbus.protocol.type.TypeDefinition;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
class BodyEncoder extends MessageToMessageEncoder<DbusMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, DbusMessage msg, List<Object> out) throws Exception {
        MessageHeader header = msg.getHeader();
        // note: we still modify the header below
        out.add(header);

        MessageBody body = msg.getBody();
        if (body != null && !body.getArguments().isEmpty()) {
            List<TypeDefinition> types = new ArrayList<>(body.getArguments().size());
            ByteBuf bodyBuffer = ctx.alloc().buffer().order(Local.OUTBOUND_ORDER);
            AlignableByteBuf aligned = AlignableByteBuf.fromAlignedBuffer(bodyBuffer, 8);
            for (DbusObject arg : body.getArguments()) {
                types.add(arg.getType());
                arg.serialize(aligned);
            }

            header.addHeader(HeaderField.SIGNATURE, SignatureObject.create(types));
            header.setMessageBodyLength(bodyBuffer.readableBytes());
            out.add(bodyBuffer);
            log.trace("Body: {}", body);
        }
        log.trace("Header: {}", header);
    }
}
