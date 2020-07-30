/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.object;

import at.yawk.dbus.protocol.type.BasicType;
import at.yawk.dbus.protocol.type.TypeDefinition;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Basic objects are objects that do not enclose other dbus objects. <i>This does not include
 * {@link VariantObject}s.</i>
 *
 * @author yawkat
 */
@Getter
@EqualsAndHashCode
@ToString
public abstract class BasicObject implements DbusObject {
    static final long MASK_BYTE = 0xffL;
    static final long MASK_SHORT = 0xffffL;
    static final long MASK_INT = 0xffffffffL;

    static final int BOOLEAN_TRUE = 1;
    static final int BOOLEAN_FALSE = 0;

    private final BasicType type;

    BasicObject(BasicType type) {
        this.type = type;
    }

    /**
     * Get this object as a signed number. The returned number will be signed: For example, a {@link BasicType#UINT16}
     * with value {@code 0xffff} will return a {@link Short} of value {@code -1}.
     *
     * @throws UnsupportedOperationException if this is not numeric type.
     */
    public Number signedNumberValue() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Return this object cast to byte.
     *
     * @throws UnsupportedOperationException if this is not numeric type.
     */
    @Override
    public byte byteValue() {
        return (byte) longValue();
    }

    /**
     * Return this object cast to short.
     *
     * @throws UnsupportedOperationException if this is not numeric type.
     */
    @Override
    public short shortValue() {
        return (short) longValue();
    }

    /**
     * Return this object cast to int.
     *
     * @throws UnsupportedOperationException if this is not numeric type.
     */
    @Override
    public int intValue() {
        return (int) longValue();
    }

    /**
     * Return this object cast to long.
     *
     * @throws UnsupportedOperationException if this is not numeric type.
     */
    @Override
    public long longValue() {
        return signedNumberValue().longValue();
    }

    /**
     * Return this object cast to double.
     *
     * @throws UnsupportedOperationException if this is not numeric type.
     */
    @Override
    public double doubleValue() {
        return signedNumberValue().doubleValue();
    }

    /**
     * Get this object as a number. The returned may not be signed: For example, a {@link BasicType#UINT16}
     * with value {@code 0xffff} will return an {@link Integer} of value {@code 0xffff}.
     *
     * @throws UnsupportedOperationException if this is not numeric type.
     */
    public Number realNumberValue() throws UnsupportedOperationException {
        return signedNumberValue();
    }

    /**
     * @throws UnsupportedOperationException if this is not a {@link BasicType#BOOLEAN} type.
     */
    @Override
    public boolean booleanValue() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException if this is not a {@link BasicType#isStringLike()} type.
     */
    @Override
    public String stringValue() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException if this is not a {@link BasicType#SIGNATURE} type.
     */
    @Override
    public List<TypeDefinition> typeValue() {
        throw new UnsupportedOperationException();
    }

    ///// FACTORIES /////

    /**
     * Create a new boolean object.
     *
     * <b>Boolean objects are not numeric.</b>
     */
    public static BasicObject createBoolean(boolean value) {
        return new IntegerBasicObject(BasicType.BOOLEAN, value ? BOOLEAN_TRUE : BOOLEAN_FALSE);
    }

    public static BasicObject createByte(byte value) {
        return new IntegerBasicObject(BasicType.BYTE, value & MASK_BYTE);
    }

    public static BasicObject createInt16(short value) {
        return new IntegerBasicObject(BasicType.INT16, value & MASK_SHORT);
    }

    public static BasicObject createUint16(short value) {
        return new IntegerBasicObject(BasicType.UINT16, value & MASK_SHORT);
    }

    public static BasicObject createInt32(int value) {
        return new IntegerBasicObject(BasicType.INT32, value & MASK_INT);
    }

    public static BasicObject createUint32(int value) {
        return new IntegerBasicObject(BasicType.UINT32, value & MASK_INT);
    }

    public static BasicObject createUnixFd(int value) {
        return new IntegerBasicObject(BasicType.UNIX_FD, value & MASK_INT);
    }

    public static BasicObject createInt64(long value) {
        return new IntegerBasicObject(BasicType.INT64, value);
    }

    public static BasicObject createUint64(long value) {
        return new IntegerBasicObject(BasicType.UINT64, value);
    }

    public static BasicObject createDouble(double value) {
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        class DoubleBasicObject extends BasicObject {
            public DoubleBasicObject() {
                super(BasicType.DOUBLE);
            }

            @Override
            public void serialize(AlignableByteBuf buf) {
                buf.alignWrite(8);
                buf.getBuffer().writeDouble(value);
            }

            @Override
            public Number signedNumberValue() throws UnsupportedOperationException {
                return value;
            }

            @Override
            public Number realNumberValue() throws UnsupportedOperationException {
                return value;
            }

            @Override
            public double doubleValue() {
                return value;
            }
        }
        return new DoubleBasicObject();
    }

    /**
     * Create a new dbus object of type {@link BasicType#STRING} with the given string value.
     */
    public static StringObject createString(String value) {
        return StringObject.create(value);
    }

    public static BasicObject createSignature(List<TypeDefinition> definitions) {
        return SignatureObject.create(definitions);
    }

    public static BasicObject createObjectPath(String path) {
        return ObjectPathObject.create(path);
    }

    public static BasicObject deserialize(BasicType type, AlignableByteBuf buf) {
        if (type == BasicType.STRING) {
            return StringObject.deserialize(buf);
        } else if (type == BasicType.SIGNATURE) {
            return SignatureObject.deserialize(buf);
        } else if (type == BasicType.OBJECT_PATH) {
            return ObjectPathObject.deserialize(buf);
        } else if (type == BasicType.DOUBLE) {
            buf.alignRead(8);
            return createDouble(buf.readDouble());
        } else if (type.isNumeric()) {
            return IntegerBasicObject.deserialize0(type, buf);
        } else {
            throw new UnsupportedOperationException(type + " cannot be deserialized to a basic object");
        }
    }
}
