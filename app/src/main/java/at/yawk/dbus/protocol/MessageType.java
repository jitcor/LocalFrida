/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol;

import javas.util.Arrays_;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.Getter;

/**
 * @author yawkat
 */
public enum MessageType {
    METHOD_CALL(1, "method_call", HeaderField.PATH, HeaderField.MEMBER),
    METHOD_RETURN(2, "method_return", HeaderField.REPLY_SERIAL),
    ERROR(3, "error", HeaderField.REPLY_SERIAL, HeaderField.ERROR_NAME),
    SIGNAL(4, "signal", HeaderField.PATH, HeaderField.MEMBER, HeaderField.INTERFACE);

    private static final MessageType[] BY_ID;

    @Getter private final byte id;
    @Getter private final String name;
    @Getter private final Set<HeaderField> requiredHeaders;

    MessageType(int id, String name, HeaderField... requiredHeaders) {
        this.id = (byte) id;
        this.name = name;
        this.requiredHeaders = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(requiredHeaders)));
    }

    @Nullable
    public static MessageType byId(byte id) {
        return ((id >= 0) && (id < BY_ID.length)) ? BY_ID[id] : null;
    }

    static {
        int maxId = Arrays_.stream(values()).mapToInt(MessageType::getId).max().getAsInt();
        BY_ID = new MessageType[maxId + 1];
        for (MessageType messageType : values()) {
            BY_ID[messageType.getId()] = messageType;
        }
    }
}
