/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol;

import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.SignatureObject;
import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.Map;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class MessageHeader {
    /**
     * Byte order of the message. Ignored for writing.
     */
    private ByteOrder byteOrder;
    private MessageType messageType;
    private boolean noReplyExpected;
    private boolean noAutoStart;
    private boolean allowInteractiveAuthorization;
    private byte majorProtocolVersion;
    private long messageBodyLength;
    private int serial;
    private Map<HeaderField, DbusObject> headerFields;

    /**
     * Add a header field, creating the header map if necessary.
     */
    public void addHeader(HeaderField field, DbusObject object) {
        if (headerFields == null) {
            headerFields = new EnumMap<>(HeaderField.class);
        }
        headerFields.put(field, object);
    }
}
