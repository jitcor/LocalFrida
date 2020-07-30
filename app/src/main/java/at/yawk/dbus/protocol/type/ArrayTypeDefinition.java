/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.type;

import at.yawk.dbus.protocol.object.AlignableByteBuf;
import at.yawk.dbus.protocol.object.ArrayObject;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class ArrayTypeDefinition implements TypeDefinition {
    private final TypeDefinition memberType;

    @Override
    public String serialize() {
        return 'a' + memberType.serialize();
    }

    @Override
    public ArrayObject deserialize(AlignableByteBuf buf) {
        return ArrayObject.deserialize(this, buf);
    }
}
