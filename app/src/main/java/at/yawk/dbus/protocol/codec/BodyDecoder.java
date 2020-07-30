/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.codec;

import at.yawk.dbus.protocol.HeaderField;
import at.yawk.dbus.protocol.MessageBody;
import at.yawk.dbus.protocol.MessageHeader;
import at.yawk.dbus.protocol.object.AlignableByteBuf;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.type.TypeDefinition;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
class BodyDecoder extends MessageToMessageDecoder<AlignableByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, AlignableByteBuf in, List<Object> out) throws Exception {
        MessageHeader header = ctx.channel().attr(Local.CURRENT_HEADER).get();
        if (header == null) {
            // message should be skipped
            return;
        }

        DbusObject signature = header.getHeaderFields().get(HeaderField.SIGNATURE);
        if (signature == null) { throw new DecoderException("Non-empty body but missing signature header"); }

        List<TypeDefinition> types = signature.typeValue();
        List<DbusObject> bodyObjects = new ArrayList<>();

        for (TypeDefinition type : types) {
            DbusObject object = type.deserialize(in);
            bodyObjects.add(object);

            log.trace("Decoded object {}", object);
        }

        MessageBody body = new MessageBody();
        body.setArguments(bodyObjects);
        out.add(body);
    }
}
