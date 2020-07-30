/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.codec;

import at.yawk.dbus.protocol.HeaderField;
import at.yawk.dbus.protocol.MessageHeader;
import at.yawk.dbus.protocol.MessageType;
import at.yawk.dbus.protocol.object.*;
import at.yawk.dbus.protocol.type.ArrayTypeDefinition;
import at.yawk.dbus.protocol.type.BasicType;
import at.yawk.dbus.protocol.type.StructTypeDefinition;
import at.yawk.dbus.protocol.type.TypeDefinition;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.DecoderException;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import javax.annotation.Nullable;

import java9.util.stream.Collectors;
import java9.util.stream.StreamSupport;
import javas.lang.Maths;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
class MessageHeaderCodec extends ByteToMessageCodec<MessageHeader> {
    // the headers are an array of byte:variant structs
    private static final StructTypeDefinition HEADER_FIELD_TYPE =
            new StructTypeDefinition(Arrays.asList(BasicType.BYTE, BasicType.VARIANT));
    private static final ArrayTypeDefinition HEADER_FIELD_LIST_TYPE = new ArrayTypeDefinition(HEADER_FIELD_TYPE);

    private static final int MIN_HEADER_LENGTH =
            12 + // static header
                    4 // 0 array length
            ;
    private static final byte PROTOCOL_VERSION = 1;

    // flags
    private static final byte NO_REPLY_EXPECTED = 0x1;
    private static final byte NO_AUTO_START = 0x2;
    private static final byte ALLOW_INTERACTIVE_AUTHORIZATION = 0x4;

    /**
     * How many bytes still need to be read in the current packet.
     */
    private int toRead;
    /**
     * Byte order to forward to the next decoders.
     */
    private ByteOrder byteOrder;

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageHeader msg, ByteBuf out)
            throws Exception {
        out = out.order(Local.OUTBOUND_ORDER);

        AlignableByteBuf alignedBuf = AlignableByteBuf.encoding(out);
        out.writeByte(Local.OUTBOUND_ORDER == ByteOrder.LITTLE_ENDIAN ? 'l' : 'B');

        out.writeByte(msg.getMessageType().getId());

        byte flags = 0;
        if (msg.isNoReplyExpected()) {
            flags |= NO_REPLY_EXPECTED;
        }
        if (msg.isNoAutoStart()) {
            flags |= NO_AUTO_START;
        }
        if (msg.isAllowInteractiveAuthorization()) {
            flags |= ALLOW_INTERACTIVE_AUTHORIZATION;
        }
        out.writeByte(flags);

        byte protocolVersion = msg.getMajorProtocolVersion();
        if (protocolVersion == 0) {
            protocolVersion = PROTOCOL_VERSION;
        }
        out.writeByte(protocolVersion);

        out.writeInt((int) msg.getMessageBodyLength());

        int serial = msg.getSerial();
        if (serial == 0) {
            serial = Local.generateSerial(ctx);
        }
        out.writeInt(serial);

        checkRequiredHeaderFieldsPresent(msg);

        ArrayObject headerObject = ArrayObject.create(
                HEADER_FIELD_LIST_TYPE,
                StreamSupport.stream(msg.getHeaderFields().entrySet())
                        .map(entry -> {
                            BasicObject id = BasicObject.createByte(entry.getKey().getId());
                            DbusObject value = entry.getValue();
                            return StructObject.create(
                                    HEADER_FIELD_TYPE,
                                    Arrays.asList(id, VariantObject.create(value))
                            );
                        })
                        .collect(Collectors.toList())
        );

        headerObject.serialize(alignedBuf);
        alignedBuf.alignWrite(8);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf rawBuf, List<Object> out)
            throws Exception {
        if (toRead != 0) {
            if (rawBuf.readableBytes() < toRead) {
                return;
            }
            ByteBuf slice = rawBuf.slice().order(byteOrder);
            slice.writerIndex(slice.readerIndex() + toRead);
            slice.retain();
            AlignableByteBuf decoding = AlignableByteBuf.decoding(slice);
            log.trace("INBOUND {}", decoding);
            out.add(decoding);

            rawBuf.readerIndex(rawBuf.readerIndex() + toRead);
            toRead = 0;
        }

        if (rawBuf.readableBytes() < MIN_HEADER_LENGTH) {
            return;
        }

        rawBuf.markReaderIndex();
        byte endianness = rawBuf.readByte();
        ByteOrder order;
        switch (endianness) {
            case 'l':
                order = ByteOrder.LITTLE_ENDIAN;
                break;
            case 'B':
                order = ByteOrder.BIG_ENDIAN;
                break;
            default:
                throw new DecoderException("Unknown byte order byte " + endianness);
        }

        AlignableByteBuf buf = AlignableByteBuf.decoding(
                rawBuf.resetReaderIndex().order(order));

        buf.getBuffer().markReaderIndex();
        buf.readByte(); // skip endianness byte we read above

        @Nullable MessageType type = MessageType.byId(buf.readByte());
        byte flags = buf.readByte();
        byte majorProtocolVersion = buf.readByte();
        if (majorProtocolVersion != PROTOCOL_VERSION) {
            throw new DecoderException("Unsupported major protocol version " + majorProtocolVersion);
        }
        long bodyLength = buf.readUnsignedInt();
        int serial = buf.readInt();

        MessageHeader header = new MessageHeader();
        header.setByteOrder(order);
        header.setMessageType(type);
        header.setNoReplyExpected((flags & NO_REPLY_EXPECTED) != 0);
        header.setNoAutoStart((flags & NO_AUTO_START) != 0);
        header.setAllowInteractiveAuthorization((flags & ALLOW_INTERACTIVE_AUTHORIZATION) != 0);
        header.setMajorProtocolVersion(majorProtocolVersion);
        header.setMessageBodyLength(bodyLength);
        header.setSerial(serial);
        header.setHeaderFields(new EnumMap<>(HeaderField.class));

        ArrayObject headers = (ArrayObject) tryDecode(HEADER_FIELD_LIST_TYPE, buf);
        if (headers == null) {
            // not enough data
            buf.getBuffer().resetReaderIndex();
            return;
        }
        for (DbusObject struct : headers.getValues()) {
            HeaderField field = HeaderField.byId(struct.get(0).byteValue());
            if (field != null) {
                DbusObject value = struct.get(1).getValue();
                if (!value.getType().equals(field.getType())) {
                    throw new DecoderException(
                            "Invalid header type on " + field + ": got " + value.getType() + " but expected " +
                                    field.getType()
                    );
                }
                header.getHeaderFields().put(field, value);
            }
        }

        if (type != null) {
            checkRequiredHeaderFieldsPresent(header);
        }

        if (!buf.canAlignRead(8)) {
            buf.getBuffer().resetReaderIndex();
            return;
        }
        buf.alignRead(8);
        toRead = Maths.toIntExact(header.getMessageBodyLength());
        byteOrder = order;
        out.add(header);
    }

    private void checkRequiredHeaderFieldsPresent(MessageHeader header) {
        for (HeaderField required : header.getMessageType().getRequiredHeaders()) {
            if (!header.getHeaderFields().containsKey(required)) {
                throw new DecoderException("Missing required header field " + required);
            }
        }
    }

    /**
     * @return the decoded object or {@code null} if we need more data.
     */
    @Nullable
    static DbusObject tryDecode(TypeDefinition definition, AlignableByteBuf buf) {
        try {
            return definition.deserialize(buf);
        } catch (IndexOutOfBoundsException e) {
            // not enough data

            // todo: don't catch such a broad exception
            // hack: ignore list out of bounds etc.
            if (e.getClass() != IndexOutOfBoundsException.class) {
                throw e;
            }

            log.trace("Need more data");

            return null;
        }
    }
}
