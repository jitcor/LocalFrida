/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.object;

import at.yawk.dbus.protocol.type.BasicType;
import java.math.BigInteger;

import javas.lang.Longs_;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author yawkat
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
final class IntegerBasicObject extends BasicObject {
    private final long value;

    /**
     * @param value The long value of this number. The lower bits will be used in writing.
     */
    IntegerBasicObject(BasicType type, long value) {
        super(type);
        this.value = value;
    }

    @Override
    public boolean booleanValue() throws UnsupportedOperationException {
        if (getType() == BasicType.BOOLEAN) {
            return value == BOOLEAN_TRUE;
        }
        return super.booleanValue();
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return longValue();
    }

    @Override
    public Number signedNumberValue() throws UnsupportedOperationException {
        switch (getType()) {
        case BYTE:
            return (byte) value;
        case INT16:
        case UINT16:
            return (short) value;
        case INT32:
        case UINT32:
        case UNIX_FD:
        case BOOLEAN:
            return (int) value;
        case INT64:
        case UINT64:
            return value;
        }
        return super.signedNumberValue();
    }

    static IntegerBasicObject deserialize0(BasicType type, AlignableByteBuf buf) {
        long value;
        switch (type.getLength()) {
        case 1:
            value = buf.readByte() & MASK_BYTE;
            break;
        case 2:
            buf.alignRead(2);
            value = buf.readShort() & MASK_SHORT;
            break;
        case 4:
            buf.alignRead(4);
            value = buf.readInt() & MASK_INT;
            break;
        case 8:
            buf.alignRead(8);
            value = buf.readLong();
            break;
        default:
            throw new AssertionError(type.name());
        }
        return new IntegerBasicObject(type, value);
    }

    @Override
    public Number realNumberValue() throws UnsupportedOperationException {
        switch (getType()) {
        case BYTE:
            return (byte) value;
        case INT16:
            return (short) value;
        case INT32:
        case UINT16:
            return (int) value;
        case INT64:
        case UINT32:
        case UNIX_FD:
            return value;
        case UINT64:
            return value < 0 ?
                    new BigInteger(Longs_.toUnsignedString(value)) :
                    BigInteger.valueOf(value);
        }
        return super.signedNumberValue();
    }

    @Override
    public void serialize(AlignableByteBuf buf) {
        switch (getType().getLength()) {
        case 1:
            buf.writeByte((int) value);
            break;
        case 2:
            buf.alignWrite(2);
            buf.writeShort((int) value);
            break;
        case 4:
            buf.alignWrite(4);
            buf.writeInt((int) value);
            break;
        case 8:
            buf.alignWrite(8);
            buf.writeLong(value);
            break;
        }
    }
}
