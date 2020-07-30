/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.object;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author yawkat
 */
@ToString
@EqualsAndHashCode
public class AlignableByteBuf implements ReferenceCounted {
    @Getter private final ByteBuf buffer;
    private final int messageOffset;
    private final int baseAlignment;

    // visible for testing
    public AlignableByteBuf(ByteBuf buffer, int messageOffset, int baseAlignment) {
        this.buffer = buffer;
        this.messageOffset = messageOffset;
        this.baseAlignment = baseAlignment;
    }

    public static AlignableByteBuf decoding(ByteBuf wrapping) {
        return decoding(wrapping, 1 << 30);
    }

    public static AlignableByteBuf decoding(ByteBuf wrapping, int baseAlignment) {
        return new AlignableByteBuf(wrapping, -wrapping.readerIndex(), baseAlignment);
    }

    /**
     * Create a new {@link AlignableByteBuf} from the given message buffer. The message must start at {@code
     * buffer[0]}.
     */
    public static AlignableByteBuf encoding(ByteBuf wrapping) {
        return new AlignableByteBuf(wrapping, 0, 1 << 30);
    }

    /**
     * Create a new {@link AlignableByteBuf} from a buffer that is known to be aligned to a block boundary with the
     * given block size.
     *
     * @param existingAlignment the alignment of the given buffer.
     */
    public static AlignableByteBuf fromAlignedBuffer(ByteBuf buffer, int existingAlignment) {
        return new AlignableByteBuf(buffer, 0, existingAlignment);
    }

    private int calculateAlignmentOffset(int position, int alignment) {
        return (alignment - ((this.messageOffset + position) % alignment)) % alignment;
    }

    private boolean canAlign(int alignment) {
        return (baseAlignment % alignment) == 0;
    }

    private void checkAlign(int alignment) {
        if (!canAlign(alignment)) {
            throw new IllegalArgumentException(
                    "Cannot align to boundary " + alignment + ": base boundary is " + baseAlignment);
        }
    }

    public boolean canAlignRead(int alignment) {
        if (!canAlign(alignment)) { return false; }
        int toPad = calculateAlignmentOffset(getBuffer().readerIndex(), alignment);
        return getBuffer().readableBytes() >= toPad;
    }

    public void alignRead(int alignment) {
        checkAlign(alignment);
        int toPad = calculateAlignmentOffset(getBuffer().readerIndex(), alignment);
        for (int i = 0; i < toPad; i++) {
            if (getBuffer().readByte() != 0) {
                throw new DeserializerException("Non-null byte in alignment padding");
            }
        }
    }

    public boolean canAlignWrite(int alignment) {
        if (!canAlign(alignment)) { return false; }
        int toPad = calculateAlignmentOffset(getBuffer().writerIndex(), alignment);
        return getBuffer().writableBytes() >= toPad;
    }

    public void alignWrite(int alignment) {
        checkAlign(alignment);
        int toPad = calculateAlignmentOffset(getBuffer().writerIndex(), alignment);
        getBuffer().writeZero(toPad);
    }

    public boolean isReadable() {
        return getBuffer().isReadable();
    }

    public boolean isReadable(int size) {
        return getBuffer().isReadable(size);
    }

    public boolean isWritable() {
        return getBuffer().isWritable();
    }

    public boolean isWritable(int size) {
        return getBuffer().isWritable(size);
    }

    public int readableBytes() {
        return getBuffer().readableBytes();
    }

    public int writableBytes() {
        return getBuffer().writableBytes();
    }

    public int readerIndex() {
        return getBuffer().readerIndex();
    }

    public AlignableByteBuf readerIndex(int readerIndex) {
        getBuffer().readerIndex(readerIndex);
        return this;
    }

    public int writerIndex() {
        return getBuffer().writerIndex();
    }

    public AlignableByteBuf writerIndex(int writerIndex) {
        getBuffer().writerIndex(writerIndex);
        return this;
    }

    public int readInt() {
        return getBuffer().readInt();
    }

    public int readUnsignedMedium() {
        return getBuffer().readUnsignedMedium();
    }

    public int readMedium() {
        return getBuffer().readMedium();
    }

    public int readUnsignedShort() {
        return getBuffer().readUnsignedShort();
    }

    public short readShort() {
        return getBuffer().readShort();
    }

    public byte readByte() {
        return getBuffer().readByte();
    }

    public boolean readBoolean() {
        return getBuffer().readBoolean();
    }

    public long readUnsignedInt() {
        return getBuffer().readUnsignedInt();
    }

    public long readLong() {
        return getBuffer().readLong();
    }

    public char readChar() {
        return getBuffer().readChar();
    }

    public float readFloat() {
        return getBuffer().readFloat();
    }

    public double readDouble() {
        return getBuffer().readDouble();
    }

    public ByteBuf readBytes(int length) {
        return getBuffer().readBytes(length);
    }

    public AlignableByteBuf readBytes(byte[] dst) {
        getBuffer().readBytes(dst);
        return this;
    }

    public AlignableByteBuf writeBoolean(boolean value) {
        getBuffer().writeBoolean(value);
        return this;
    }

    public AlignableByteBuf writeByte(int value) {
        getBuffer().writeByte(value);
        return this;
    }

    public AlignableByteBuf writeShort(int value) {
        getBuffer().writeShort(value);
        return this;
    }

    public AlignableByteBuf writeMedium(int value) {
        getBuffer().writeMedium(value);
        return this;
    }

    public AlignableByteBuf writeInt(int value) {
        getBuffer().writeInt(value);
        return this;
    }

    public AlignableByteBuf writeLong(long value) {
        getBuffer().writeLong(value);
        return this;
    }

    public AlignableByteBuf writeChar(int value) {
        getBuffer().writeChar(value);
        return this;
    }

    public AlignableByteBuf writeFloat(float value) {
        getBuffer().writeFloat(value);
        return this;
    }

    public AlignableByteBuf writeDouble(double value) {
        getBuffer().writeDouble(value);
        return this;
    }

    public AlignableByteBuf writeBytes(ByteBuf src) {
        getBuffer().writeBytes(src);
        return this;
    }

    public AlignableByteBuf writeBytes(byte[] src) {
        getBuffer().writeBytes(src);
        return this;
    }

    @Override
    public int refCnt() {
        return getBuffer().refCnt();
    }

    @Override
    public ReferenceCounted retain() {
        getBuffer().retain();
        return this;
    }

    @Override
    public ReferenceCounted retain(int increment) {
        getBuffer().retain(increment);
        return this;
    }

    @Override
    public ReferenceCounted touch() {
        getBuffer().touch();
        return this;
    }

    @Override
    public ReferenceCounted touch(Object hint) {
        getBuffer().touch(hint);
        return this;
    }

    @Override
    public boolean release() {
        return getBuffer().release();
    }

    @Override
    public boolean release(int decrement) {
        return getBuffer().release(decrement);
    }
}
