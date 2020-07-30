/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.nio.ByteBuffer;
import lombok.extern.slf4j.Slf4j;

/**
 * Debug adapter that logs received and sent data to the trace log.
 *
 * @author yawkat
 */
@Slf4j
class LoggingInboundAdapter extends ChannelDuplexHandler {
    static boolean isEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (isEnabled() && msg instanceof ByteBuf) {
            selectStream(" IN");
            accept((ByteBuf) msg);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (isEnabled() && msg instanceof ByteBuf) {
            selectStream("OUT");
            accept((ByteBuf) msg);
        }
        super.write(ctx, msg, promise);
    }

    private String streamName;

    private void accept(ByteBuf msg) {
        msg.forEachByte(value -> {
            append(value);
            return true;
        });
        flush();
    }

    private ByteBuffer bytes = ByteBuffer.allocate(8);

    private void selectStream(String streamName) {
        if (this.streamName != null && !this.streamName.equals(streamName)) {
            flush();
        }
        this.streamName = streamName;
    }

    private void append(byte b) {
        bytes.put(b);
        if (!bytes.hasRemaining()) {
            flush();
        }
    }

    private void flush() {
        bytes.flip();
        if (!bytes.hasRemaining()) {
            bytes.clear();
            return;
        }
        StringBuilder out = new StringBuilder(bytes.remaining() * 3);
        while (bytes.hasRemaining()) {
            int i = bytes.get() & 0xff;
            String repr;
            if ((i >= 'a' && i <= 'z') ||
                (i >= 'A' && i <= 'Z') ||
                (i >= '0' && i <= '9') ||
                i == ' ' || i == '.' || i == '/' || i == '_') {
                repr = Character.toString((char) i);
            } else if (i == 0) {
                repr = "\\0";
            } else if (i == '\n') {
                repr = "\\n";
            } else if (i == '\r') {
                repr = "\\r";
            } else if (i == '\t') {
                repr = "\\t";
            } else {
                repr = Integer.toHexString(i);
                if (i < 0x10) { repr = '0' + repr; }
            }
            out.append(repr);
            for (int j = repr.length(); j < 3; j++) { out.append(' '); }
        }
        log.trace("[{}] {}", streamName, out);
        bytes.clear();
    }
}
