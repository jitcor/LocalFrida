/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol;

import android.os.Build;
import android.support.annotation.RequiresApi;

import io.netty.buffer.ByteBuf;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
public class DbusUtil {
    /**
     * Parse a UUID without dashes.
     */
    public static UUID parseUuid(String hex) {
        ByteBuffer bytes = ByteBuffer.wrap(DatatypeConverter.parseHexBinary(hex));
        return new UUID(bytes.getLong(), bytes.getLong());
    }

    /**
     * Print a UUID without dashes.
     */
    public static String printUuid(UUID uuid) {
        return uuid.toString().replace("-", "");
    }

    /**
     * Get the hex representation of the given bytes.
     */
    public static String printHex(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }

    public static String printHex(ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        return printHex(bytes);
    }

    /**
     * @return The stdout of the command.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public static String callCommand(String... command) throws Exception {
        Process process = new ProcessBuilder(command)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start();
        if (process.waitFor() != 0) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    log.warn("Process ERR: {}", line);
                }
            }
            throw new IOException("Non-null exit status");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = process.getInputStream().read(buf)) != -1) {
            bos.write(buf, 0, len);
        }
        return bos.toString();
    }

    public static void validateConnectionName(@Nonnull String name) {
        if (!name.matches("([a-zA-Z_][\\w-]*\\.)+([a-zA-Z_][\\w-]*)") &&
            !name.matches(":[\\w-]+")) {
            throw new IllegalArgumentException("Invalid connection name: " + name);
        }
    }

    public static void validateInterfaceName(@Nonnull String name) {
        if (!name.matches("([a-zA-Z_][\\w-]*\\.)+([a-zA-Z_][\\w-]*)")) {
            throw new IllegalArgumentException("Invalid interface name: " + name);
        }
    }

    public static void validateMemberName(@Nonnull String name) {
        if (!name.matches("\\w+")) {
            throw new IllegalArgumentException("Invalid member name: " + name);
        }
    }
}
