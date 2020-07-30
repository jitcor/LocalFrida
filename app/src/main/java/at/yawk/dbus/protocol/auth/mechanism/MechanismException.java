/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.auth.mechanism;

import at.yawk.dbus.protocol.auth.command.Command;
import at.yawk.dbus.protocol.auth.command.Error;
import at.yawk.dbus.protocol.auth.command.Rejected;

/**
 * Exception in a mechanism that prevents this mechanism from being used but still allows other authentication
 * attempts.
 *
 * @author yawkat
 */
public class MechanismException extends RuntimeException {
    public MechanismException(String message) {
        super(message);
    }

    public MechanismException(Error error) {
        this(error.getMessage());
    }

    public MechanismException(Rejected error) {
        this("Accepted mechanisms: " + error.getMechanisms());
    }

    public static void handleCommand(Command cmd) {
        if (cmd instanceof Error) {
            throw new MechanismException((Error) cmd);
        }
        if (cmd instanceof Rejected) {
            throw new MechanismException((Rejected) cmd);
        }
    }
}
