/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.auth.mechanism;

import at.yawk.dbus.protocol.auth.AuthChannel;
import at.yawk.dbus.protocol.auth.UnexpectedCommandException;
import at.yawk.dbus.protocol.auth.command.Auth;
import at.yawk.dbus.protocol.auth.command.Begin;
import at.yawk.dbus.protocol.auth.command.Ok;
import java9.util.concurrent.CompletionStage;

/**
 * @author yawkat
 */
public class AnonymousAuthMechanism implements AuthMechanism {
    @Override
    public CompletionStage<?> startAuth(AuthChannel channel) throws Exception {
        return channel.send(new Auth("ANONYMOUS", null))
                .thenAccept(cmd -> {
                    MechanismException.handleCommand(cmd);
                    if (cmd instanceof Ok) {
                        channel.send(new Begin());
                    } else {
                        throw new UnexpectedCommandException(cmd);
                    }
                });
    }
}
