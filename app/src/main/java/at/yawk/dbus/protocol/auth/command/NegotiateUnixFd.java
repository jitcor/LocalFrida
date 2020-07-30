/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.auth.command;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class NegotiateUnixFd extends Command {
    public static final String NAME = "NEGOTIATE_UNIX_FD";

    public NegotiateUnixFd() {
        super(AuthDirection.FROM_CLIENT, NAME);
    }

    public static NegotiateUnixFd parse(List<String> args) {
        if (!args.isEmpty()) { throw new IllegalArgumentException("No arguments expected"); }
        return new NegotiateUnixFd();
    }
}
