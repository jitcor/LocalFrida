/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.auth.command;

import at.yawk.dbus.protocol.DbusUtil;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class Data extends Command {
    public static final String NAME = "DATA";
    private final byte[] data;

    public Data(byte[] data) {
        super(null, NAME, DbusUtil.printHex(data));
        this.data = data;
    }

    public static Data parse(List<String> args) {
        if (args.size() != 1) { throw new IllegalArgumentException("Expected exactly one argument"); }
        return new Data(DatatypeConverter.parseHexBinary(args.get(0)));
    }
}
