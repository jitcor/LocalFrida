/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol;

import java.io.Closeable;
import java9.util.concurrent.CompletionStage;

/**
 * @author yawkat
 */
public interface DbusChannel extends Closeable {
    void setMessageConsumer(MessageConsumer consumer);

    /**
     * Create a unique serial for this channel.
     */
    int createSerial();

    /**
     * Send the given message to this channel. If no serial is set in the message, it will be created during this call.
     */
    void write(DbusMessage message);

    /**
     * Gracefully disconnect this channel.
     */
    void disconnect();

    /**
     * Forcibly close this channel.
     */
    @Override
    void close();

    /**
     * Get a {@link CompletionStage} that completes when this channel is closed.
     */
    CompletionStage<?> closeStage();
}
