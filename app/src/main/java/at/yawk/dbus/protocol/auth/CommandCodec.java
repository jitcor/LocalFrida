/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.auth;

import at.yawk.dbus.protocol.auth.command.AgreeUnixFd;
import at.yawk.dbus.protocol.auth.command.Auth;
import at.yawk.dbus.protocol.auth.command.Begin;
import at.yawk.dbus.protocol.auth.command.Cancel;
import at.yawk.dbus.protocol.auth.command.Command;
import at.yawk.dbus.protocol.auth.command.Data;
import at.yawk.dbus.protocol.auth.command.Error;
import at.yawk.dbus.protocol.auth.command.NegotiateUnixFd;
import at.yawk.dbus.protocol.auth.command.Ok;
import at.yawk.dbus.protocol.auth.command.Rejected;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ByteProcessor;
import java.nio.charset.Charset;
import javas.nio.charset.StandardCharsets_;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java9.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
public class CommandCodec extends ChannelDuplexHandler {
    /*
     * This is a custom ChannelHandlerAdapter instead of a ByteToMessageCodec since ByteToMessageCodec has a bug
     * (https://github.com/netty/netty/issues/4087) where it will discard data on removal from pipeline.
     */

    private static final Charset CHARSET = StandardCharsets_.US_ASCII;
    private static final byte[] CRLF = new byte[]{ '\r', '\n' };
    private static final Map<String, Function<List<String>, Command>> FACTORIES = new HashMap<>();

    static {
        FACTORIES.put(AgreeUnixFd.NAME, AgreeUnixFd::parse);
        FACTORIES.put(Auth.NAME, Auth::parse);
        FACTORIES.put(Begin.NAME, Begin::parse);
        FACTORIES.put(Cancel.NAME, Cancel::parse);
        FACTORIES.put(Data.NAME, Data::parse);
        FACTORIES.put(Error.NAME, Error::parse);
        FACTORIES.put(NegotiateUnixFd.NAME, NegotiateUnixFd::parse);
        FACTORIES.put(Ok.NAME, Ok::parse);
        FACTORIES.put(Rejected.NAME, Rejected::parse);
    }

    private final ByteToMessageDecoder decoder = new ByteToMessageDecoder() {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            CommandCodec.this.decode(ctx, in, out);
        }
    };
    private final MessageToByteEncoder<Command> encoder = new MessageToByteEncoder<Command>() {
        @Override
        protected void encode(ChannelHandlerContext ctx, Command msg, ByteBuf out) throws Exception {
            CommandCodec.this.encode(ctx, msg, out);
        }
    };

    protected void encode(ChannelHandlerContext ctx, Command msg, ByteBuf out) throws Exception {
        out.writeBytes(msg.getSerialized().getBytes(CHARSET));
        out.writeBytes(CRLF);
        log.trace("Sent message {}", msg.getSerialized());
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int crlfPos = in.forEachByte(new CRLFFinder());
        if (crlfPos == -1) {
            return;
        }

        int start = in.readerIndex();
        int end = crlfPos - 1; // remove trailing crlf
        String commandString = in.toString(start, end - start, CHARSET);
        log.trace("Received message {}", commandString);
        in.readerIndex(crlfPos + 1);

        int sep = commandString.indexOf(' ');
        String commandName = sep == -1 ? commandString : commandString.substring(0, sep);
        Function<List<String>, Command> factory = FACTORIES.get(commandName);
        if (factory != null) {
            List<String> args;
            if (sep == -1) {
                args = Collections.emptyList();
            } else {
                args = Arrays.asList(commandString.substring(sep + 1).split(" "));
            }
            out.add(factory.apply(args));
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        decoder.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        encoder.write(ctx, msg, promise);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        decoder.channelInactive(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        decoder.handlerRemoved(ctx);
    }

    private static class CRLFFinder implements ByteProcessor {
        boolean hasCr = false;

        @Override
        public boolean process(byte value) throws Exception {
            if (hasCr) {
                if (value != '\n') {
                    throw new DecoderException("CR not followed by LF");
                } else {
                    return false;
                }
            } else {
                hasCr = value == '\r';
                return true;
            }
        }
    }
}
