/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol;

import at.yawk.dbus.protocol.object.BasicObject;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.ObjectPathObject;
import java.util.Arrays;
import javax.annotation.Nullable;
import lombok.experimental.UtilityClass;

/**
 * @author yawkat
 */
@UtilityClass
public class MessageFactory {
    public static DbusMessage methodCall(
            String path,
            @Nullable String destination,
            String interfaceName,
            String memberName,
            DbusObject... arguments
    ) {
        return methodCall(
                ObjectPathObject.create(path),
                destination == null ? null : BasicObject.createString(destination),
                BasicObject.createString(interfaceName),
                BasicObject.createString(memberName),
                arguments
        );
    }

    public static DbusMessage methodCall(
            ObjectPathObject path,
            @Nullable BasicObject destination,
            BasicObject interfaceName,
            BasicObject memberName,
            DbusObject... arguments
    ) {
        DbusMessage message = new DbusMessage();

        MessageHeader header = new MessageHeader();
        header.setMessageType(MessageType.METHOD_CALL);
        header.addHeader(HeaderField.PATH, path);
        if (destination != null) {
            header.addHeader(HeaderField.DESTINATION, destination);
        }
        header.addHeader(HeaderField.INTERFACE, interfaceName);
        header.addHeader(HeaderField.MEMBER, memberName);
        message.setHeader(header);

        MessageBody body = new MessageBody();
        body.setArguments(Arrays.asList(arguments));
        message.setBody(body);

        return message;
    }
}
