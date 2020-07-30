/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.auth.mechanism;

import at.yawk.dbus.protocol.auth.AuthChannel;
import java9.util.concurrent.CompletionStage;

/**
 * @author yawkat
 */
public interface AuthMechanism {
    CompletionStage<?> startAuth(AuthChannel channel) throws Exception;
}
