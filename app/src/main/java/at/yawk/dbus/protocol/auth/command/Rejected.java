/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.auth.command;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class Rejected extends Command {
    public static final String NAME = "REJECTED";
    private final List<String> mechanisms;

    public Rejected(List<String> mechanisms) {
        super(AuthDirection.FROM_SERVER, NAME, StringUtils.join(mechanisms," "));
        this.mechanisms = mechanisms;
    }

    public static Rejected parse(List<String> args) {
        return new Rejected(args);
    }
}
