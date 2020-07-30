/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.object;

import at.yawk.dbus.protocol.type.StructTypeDefinition;
import at.yawk.dbus.protocol.type.TypeDefinition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author yawkat
 */
@Getter
@EqualsAndHashCode
@ToString
public class StructObject implements DbusObject {
    private final StructTypeDefinition type;
    private final List<DbusObject> values;

    StructObject(StructTypeDefinition type, List<DbusObject> values) {
        this.type = type;
        this.values = values;
    }

    public static StructObject create(StructTypeDefinition type, List<DbusObject> values) {
        if (values.size() != type.getMembers().size()) {
            throw new IllegalArgumentException("Mismatched member count");
        }

        Iterator<DbusObject> valueIterator = values.iterator();
        Iterator<TypeDefinition> typeIterator = type.getMembers().iterator();

        while (valueIterator.hasNext()) {
            TypeDefinition valueType = valueIterator.next().getType();
            TypeDefinition structType = typeIterator.next();

            if (!valueType.equals(structType)) {
                throw new IllegalArgumentException(
                        "Mismatched value type " + valueType + ", expected " + structType);
            }
        }
        return new StructObject(type, values);
    }

    public static StructObject deserialize(StructTypeDefinition type, AlignableByteBuf buf) {
        buf.alignRead(8);
        List<DbusObject> values = new ArrayList<>(type.getMembers().size());
        //noinspection Convert2streamapi
        for (TypeDefinition member : type.getMembers()) {
            values.add(member.deserialize(buf));
        }
        return new StructObject(type, values);
    }

    @Override
    public void serialize(AlignableByteBuf buf) {
        buf.alignWrite(8);
        for (DbusObject value : values) {
            value.serialize(buf);
        }
    }

    @Override
    public DbusObject get(int i) {
        return getValues().get(i);
    }
}
