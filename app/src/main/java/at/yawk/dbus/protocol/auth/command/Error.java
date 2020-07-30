/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.auth.command;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.StringJoiner;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class Error extends Command {
    public static final String NAME = "ERROR";
    private final String message;

    public Error(String message) {
        super(null, NAME, message);
        this.message = message;
    }

    public static Error parse(List<String> args) {
        return new Error(StringUtils.join(args," "));
    }
}
