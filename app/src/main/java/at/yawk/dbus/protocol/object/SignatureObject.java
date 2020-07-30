/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.object;

import at.yawk.dbus.protocol.type.BasicType;
import at.yawk.dbus.protocol.type.MalformedTypeDefinitionException;
import at.yawk.dbus.protocol.type.TypeDefinition;
import at.yawk.dbus.protocol.type.TypeParser;
import java9.util.stream.StreamSupport;
import javas.nio.charset.StandardCharsets_;
import java.util.List;
import java9.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author yawkat
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SignatureObject extends BasicObject {
    private final List<TypeDefinition> definitions;

    SignatureObject(List<TypeDefinition> definitions) {
        super(BasicType.SIGNATURE);
        this.definitions = definitions;
    }

    public static SignatureObject create(List<TypeDefinition> definitions) {
        return new SignatureObject(definitions);
    }

    public static BasicObject deserialize(AlignableByteBuf buf) {
        String def = readSignatureString(buf);
        try {
            return TypeParser.parseTypeSignature(def);
        } catch (MalformedTypeDefinitionException e) {
            throw new DeserializerException(e);
        }
    }

    @Override
    public void serialize(AlignableByteBuf buf) {
        StringBuilder builder = new StringBuilder();
        for (TypeDefinition definition : definitions) {
            builder.append(definition.serialize());
        }
        writeSignatureString(buf, builder.toString());
    }

    static String readSignatureString(AlignableByteBuf buf) {
        byte len = buf.readByte();
        String def = buf.getBuffer().toString(buf.readerIndex(), len, StandardCharsets_.US_ASCII);
        buf.readerIndex(buf.readerIndex() + len);
        if (buf.readByte() != '\0') {
            throw new DeserializerException("Signature not followed by NUL byte");
        }
        return def;
    }

    static void writeSignatureString(AlignableByteBuf buf, String definition) {
        byte[] bytes = definition.getBytes(StandardCharsets_.US_ASCII);
        if (bytes.length > 0xff) {
            throw new IllegalStateException("Signature too long (is " + bytes.length + " bytes)");
        }
        buf.writeByte(bytes.length);
        buf.writeBytes(bytes);
        buf.writeByte('\0');
    }

    @Override
    public String stringValue() throws UnsupportedOperationException {
        return StreamSupport.stream(typeValue()) .map(TypeDefinition::serialize)
                .collect(Collectors.joining());
    }

    @Override
    public List<TypeDefinition> typeValue() {
        return definitions;
    }
}
