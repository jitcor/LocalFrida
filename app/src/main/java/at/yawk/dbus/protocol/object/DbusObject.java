/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.object;

import at.yawk.dbus.protocol.type.TypeDefinition;
import java.util.List;
import java.util.Map;

/**
 * @author yawkat
 */
public interface DbusObject {
    TypeDefinition getType();

    /**
     * Serialize this object to the given buffer.
     *
     * @param buf The output buffer.
     */
    void serialize(AlignableByteBuf buf);

    // shortcuts - these are implemented by one or two classes to make using DbusObject less casty
    // default implementations throw UnsupportedOperationException

    /**
     * @see VariantObject#getValue()
     */
    default DbusObject getValue() {
        throw new UnsupportedOperationException(toString());
    }

    /**
     * @see ArrayObject#getValues()
     * @see StructObject#getValues()
     */
    default List<DbusObject> getValues() {
        throw new UnsupportedOperationException(toString());
    }

    /**
     * @see ArrayObject#get(int)
     * @see StructObject#get(int)
     */
    default DbusObject get(int i) {
        throw new UnsupportedOperationException(toString());
    }

    /**
     * @see DictObject#getEntries()
     */
    default Map<DbusObject, DbusObject> getEntries() {
        throw new UnsupportedOperationException(toString());
    }

    /**
     * @see BasicObject
     */
    default String stringValue() {
        throw new UnsupportedOperationException(toString());
    }

    /**
     * @see BasicObject
     */
    default List<TypeDefinition> typeValue() {
        throw new UnsupportedOperationException(toString());
    }

    /**
     * @see BasicObject
     */
    default boolean booleanValue() {
        throw new UnsupportedOperationException(toString());
    }

    /**
     * @see BasicObject
     */
    default byte byteValue() {
        throw new UnsupportedOperationException(toString());
    }

    /**
     * @see BasicObject
     */
    default short shortValue() {
        throw new UnsupportedOperationException(toString());
    }

    /**
     * @see BasicObject
     */
    default int intValue() {
        throw new UnsupportedOperationException(toString());
    }

    /**
     * @see BasicObject
     */
    default long longValue() {
        throw new UnsupportedOperationException(toString());
    }

    /**
     * @see BasicObject
     */
    default double doubleValue() {
        throw new UnsupportedOperationException(toString());
    }
}
