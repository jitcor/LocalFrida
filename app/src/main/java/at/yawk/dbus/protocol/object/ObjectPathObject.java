/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.object;

import at.yawk.dbus.protocol.type.BasicType;
import io.netty.util.AsciiString;
import javas.nio.charset.StandardCharsets_;

import javas.lang.Maths;
import lombok.EqualsAndHashCode;

/**
 * @author yawkat
 */
@EqualsAndHashCode(callSuper = true)
public class ObjectPathObject extends BasicObject {
    private final byte[] bytes;

    ObjectPathObject(byte[] bytes) {
        super(BasicType.OBJECT_PATH);
        this.bytes = bytes;
    }

    public static ObjectPathObject create(String path) {
        byte[] bytes = path.getBytes(StandardCharsets_.US_ASCII);
        checkPath(bytes);
        return new ObjectPathObject(bytes);
    }

    static void checkPath(byte[] path) {
        if (path.length == 0) {
            throw new IllegalArgumentException("Object path must not be empty");
        }
        if (path[0] != '/') {
            throw new IllegalArgumentException("Object path must start with slash");
        }
        for (int i = 1; i < path.length; i++) {
            byte c = path[i];
            if (c == '/') {
                if (path[i - 1] == '/') {
                    throw new IllegalArgumentException("Object path components may not be empty");
                }
            } else {
                if ((c < 'a' || c > 'z') &&
                    (c < 'A' || c > 'Z') &&
                    (c < '0' || c > '9') &&
                    c != '_') {
                    throw new IllegalArgumentException("Illegal character in object path: " + c);
                }
            }
        }
    }

    public static ObjectPathObject deserialize(AlignableByteBuf buf) {
        buf.alignRead(4);
        int len = Maths.toIntExact(buf.readUnsignedInt());
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        checkPath(bytes);
        if (buf.readByte() != '\0') {
            throw new DeserializerException("Object path not followed by NUL byte");
        }
        return new ObjectPathObject(bytes);
    }

    public CharSequence getSequence() {
        return new AsciiString(bytes, false);
    }

    @Override
    public String stringValue() throws UnsupportedOperationException {
        return new String(bytes, StandardCharsets_.US_ASCII);
    }

    @Override
    public void serialize(AlignableByteBuf buf) {
        buf.alignWrite(4);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        buf.writeByte('\0');
    }

    public String toString() {
        return "ObjectPathObject(" + stringValue() + ")";
    }
}
