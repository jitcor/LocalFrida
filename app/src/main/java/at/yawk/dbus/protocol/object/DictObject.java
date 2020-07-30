/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.object;

import at.yawk.dbus.protocol.type.DictTypeDefinition;
import java.util.HashMap;
import java.util.Map;

import java9.util.Maps;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import javas.lang.Maths;

/**
 * @author yawkat
 */
@Getter
@EqualsAndHashCode
@ToString
public class DictObject implements DbusObject {
    private final DictTypeDefinition type;
    private final Map<DbusObject, DbusObject> entries;

    DictObject(DictTypeDefinition type, Map<DbusObject, DbusObject> entries) {
        this.type = type;
        this.entries = entries;
    }

    public static DictObject create(DictTypeDefinition type, Map<DbusObject, DbusObject> entries) {
        Maps.forEach(entries,(k, v) -> {
            if (!k.getType().equals(type.getKeyType())) {
                throw new IllegalArgumentException(
                        "Mismatched key type: got " + k.getType() + " but was " + type.getKeyType());
            }
            if (!v.getType().equals(type.getValueType())) {
                throw new IllegalArgumentException(
                        "Mismatched key type: got " + v.getType() + " but was " + type.getValueType());
            }
        });
        return new DictObject(type, entries);
    }

    public static DictObject deserialize(DictTypeDefinition type, AlignableByteBuf buf) {
        Map<DbusObject, DbusObject> values = new HashMap<>();
        buf.alignRead(4);
        int bytes = Maths.toIntExact(buf.readUnsignedInt());
        if (bytes > 0) {
            buf.alignRead(8);
            int start = buf.readerIndex();
            int end = start + bytes;
            while (buf.readerIndex() < end) {
                if (!buf.canAlignRead(8)) break;
                buf.alignRead(8);
                if (buf.readerIndex() >= end) break;
                DbusObject key = type.getKeyType().deserialize(buf);
                DbusObject value = type.getValueType().deserialize(buf);
                values.put(key, value);
            }
        }
        return new DictObject(type, values);
    }

    @Override
    public void serialize(AlignableByteBuf buf) {
        AlignableByteBuf tempBuffer = AlignableByteBuf.fromAlignedBuffer(
                ArrayObject.allocateBufferForWrite(buf), 8);
        Maps.forEach(entries,(k, v) -> {
            tempBuffer.alignWrite(8);
            k.serialize(tempBuffer);
            v.serialize(tempBuffer);
        });

        buf.alignWrite(4);
        buf.writeInt(tempBuffer.writerIndex());
        if (tempBuffer.isReadable()) {
            buf.alignWrite(8);
            buf.writeBytes(tempBuffer.getBuffer());
        }

        tempBuffer.release();
    }
}
