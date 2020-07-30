/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.auth.command;

import at.yawk.dbus.protocol.DbusUtil;
import java.util.List;
import javax.annotation.Nullable;
import javax.xml.bind.DatatypeConverter;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class Auth extends Command {
    public static final String NAME = "AUTH";

    @Nullable private final String mechanism;
    @Nullable private final byte[] initialResponse;

    public Auth(@Nullable String mechanism, @Nullable byte[] initialResponse) {
        super(AuthDirection.FROM_CLIENT,
              NAME,
              mechanism,
              initialResponse == null ? null : DbusUtil.printHex(initialResponse));
        if (mechanism == null && initialResponse != null) {
            throw new IllegalArgumentException("initialResponse requires mechanism");
        }
        this.mechanism = mechanism;
        this.initialResponse = initialResponse;
    }

    public Auth() {
        this(null, null);
    }

    public static Auth parse(List<String> args) {
        if (args.size() > 2) { throw new IllegalArgumentException("Too many arguments"); }
        return new Auth(args.isEmpty() ? null : args.get(0),
                        args.size() <= 1 ? null : DatatypeConverter.parseHexBinary(args.get(1)));
    }
}
