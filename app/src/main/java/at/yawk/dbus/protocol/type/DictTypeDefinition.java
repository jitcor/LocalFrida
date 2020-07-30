/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.type;

import at.yawk.dbus.protocol.object.AlignableByteBuf;
import at.yawk.dbus.protocol.object.DictObject;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class DictTypeDefinition implements TypeDefinition {
    private final TypeDefinition keyType;
    private final TypeDefinition valueType;

    @Override
    public String serialize() {
        return "a{" + keyType.serialize() + valueType.serialize() + '}';
    }

    @Override
    public DictObject deserialize(AlignableByteBuf buf) {
        return DictObject.deserialize(this, buf);
    }
}
