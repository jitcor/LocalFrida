/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol;

import at.yawk.dbus.protocol.object.DbusObject;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class MessageBody {
    private List<DbusObject> arguments;

    public void add(DbusObject object) {
        if (arguments == null) {
            arguments = new ArrayList<>();
        }
        arguments.add(object);
    }
}
