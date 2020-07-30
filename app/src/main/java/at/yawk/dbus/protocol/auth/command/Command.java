/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.auth.command;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author yawkat
 */
@EqualsAndHashCode
@ToString
@Getter
public abstract class Command {
    @Nullable
    private final AuthDirection direction;
    private final String serialized;

    Command(@Nullable AuthDirection direction, String name) {
        this.serialized = name;
        this.direction = direction;
    }

    Command(AuthDirection direction, String name, String... serializeParts) {
        this(direction, name + " " + join(serializeParts));
    }

    private static String join(String[] parts) {
        StringBuilder builder = new StringBuilder();
        boolean hitNull = false;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part == null) {
                hitNull = true;
                continue;
            } else if (hitNull) {
                throw new IllegalArgumentException("Unexpected string after null item");
            }
            if (i > 0) { builder.append(' '); }
            builder.append(part);
        }
        return builder.toString();
    }
}
