/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.object;

/**
 * @author yawkat
 */
public class DeserializerException extends RuntimeException {
    public DeserializerException(String message) {
        super(message);
    }

    public DeserializerException(Throwable cause) {
        super(cause);
    }
}
