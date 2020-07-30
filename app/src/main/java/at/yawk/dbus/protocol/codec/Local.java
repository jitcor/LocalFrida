/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.codec;

import at.yawk.dbus.protocol.MessageHeader;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.nio.ByteOrder;

/**
 * Channel properties used by the decoders
 *
 * @author yawkat
 */
interface Local {
    String PREFIX = Local.class.getName() + '.';

    /**
     * Byte order to use for outbound messages. Using {@link ByteOrder#BIG_ENDIAN} because netty buffers follow this
     * order by default.
     */
    ByteOrder OUTBOUND_ORDER = ByteOrder.BIG_ENDIAN;

    /**
     * The last message serial.
     */
    AttributeKey<Integer> LAST_SERIAL = AttributeKey.newInstance(PREFIX + "serial");
    /**
     * The current decoding header. If this is unset or null, the current data should not be decoded.
     */
    AttributeKey<MessageHeader> CURRENT_HEADER = AttributeKey.newInstance(PREFIX + "header");

    /**
     * Generate a serial for the given channel.
     */
    static int generateSerial(ChannelHandlerContext ctx) {
        int serial;Attribute<Integer> attr = ctx.attr(LAST_SERIAL);
        Integer lastSerial;
        do {
            lastSerial = attr.get();
            serial = (lastSerial == null ? 0 : lastSerial) + 1;
            if (serial == 0) { serial = 1; }
        } while (!attr.compareAndSet(lastSerial, serial));
        return serial;
    }
}
