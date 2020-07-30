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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author yawkat
 */
@Getter
@EqualsAndHashCode
@ToString
public class VariantObject implements DbusObject {
    private final DbusObject value;

    VariantObject(DbusObject value) {
        this.value = value;
    }

    @Override
    public TypeDefinition getType() {
        return BasicType.VARIANT;
    }

    public static VariantObject create(DbusObject value) {
        return new VariantObject(value);
    }

    public static VariantObject deserialize(AlignableByteBuf buf) {
        String signature = SignatureObject.readSignatureString(buf);
        TypeDefinition type;
        try {
            type = TypeParser.parseTypeDefinition(signature);
        } catch (MalformedTypeDefinitionException e) {
            throw new DeserializerException(e);
        }
        return new VariantObject(type.deserialize(buf));
    }

    @Override
    public void serialize(AlignableByteBuf buf) {
        SignatureObject.writeSignatureString(buf, value.getType().serialize());
        value.serialize(buf);
    }
}
