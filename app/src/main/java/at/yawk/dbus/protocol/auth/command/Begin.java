/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.auth.command;

import java.util.List;

/**
 * @author yawkat
 */
public class Begin extends Command {
    public static final String NAME = "BEGIN";

    public Begin() {
        super(AuthDirection.FROM_CLIENT, NAME);
    }

    public static Begin parse(List<String> args) {
        if (!args.isEmpty()) { throw new IllegalArgumentException("No arguments expected"); }
        return new Begin();
    }
}
