/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol;

import android.os.Build;

import java.net.InetSocketAddress;
import javas.nio.charset.StandardCharsets_;
import java.nio.file.Path;
import java.util.*;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class DbusAddress {
    private static final byte[] UNESCAPED_CHARS;

    static {
        UNESCAPED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-/.\\"
                .getBytes(StandardCharsets_.US_ASCII);
        Arrays.sort(UNESCAPED_CHARS);
    }

    private final String protocol;
    private final Map<String, String> properties;

    private static boolean mustEscape(byte b) {
        return Arrays.binarySearch(UNESCAPED_CHARS, b) < 0;
    }

    public static Builder builder(String protocol) {
        return new Builder(protocol);
    }

    public static DbusAddress fromUnixSocket(Path location) {
        return builder("unix")
                .property("path", location.toString())
                .build();
    }

    public static DbusAddress fromTcpAddress(InetSocketAddress address) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return builder("tcp")
                    .property("host", address.getHostString())
                    .property("port", Integer.toString(address.getPort()))
                    .build();
        }else {
            return builder("tcp")
                    .property("host", address.getHostName())
                    .property("port", Integer.toString(address.getPort()))
                    .build();
        }
    }

    public static DbusAddress parse(String repr) {
        int protocolPart = repr.indexOf(':');

        String protocol = repr.substring(0, protocolPart);
        Map<String, String> properties = new LinkedHashMap<>();

        int i = protocolPart + 1;
        while (true) {
            int valueSep = repr.indexOf('=', i);
            String key = repr.substring(i, valueSep);
            i = valueSep + 1;
            int entrySep = repr.indexOf(',', i);
            String value = repr.substring(i, entrySep == -1 ? repr.length() : entrySep);
            properties.put(unescape(key), unescape(value));

            if (entrySep == -1) { break; }
            i = entrySep + 1;
        }

        return new DbusAddress(protocol, properties);
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public String getProperty(String key) {
        String value = properties.get(key);
        if (value == null) { throw new NoSuchElementException(key); }
        return value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append(protocol).append(':');
        for (String key:properties.keySet()){
            builder.append(escape(key)).append('=').append(escape(properties.get(key))).append(',');
        }
//        properties.forEach((k, v) -> builder.append(escape(k)).append('=').append(escape(v)).append(','));
        if (!properties.isEmpty()) {
            // remove trailing ,
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }

    static String unescape(String s) {
        byte[] bytes = s.getBytes(StandardCharsets_.US_ASCII);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            if (b == '%') {
                String code = "" + (char) bytes[++i] + (char) bytes[++i];
                builder.append((char) Integer.parseInt(code, 16));
            } else {
                builder.append((char) b);
            }
        }
        return builder.toString();
    }

    static String escape(String s) {
        byte[] bytes = s.getBytes(StandardCharsets_.US_ASCII);
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            if (mustEscape(b)) {
                builder.append('%');
                if ((b & 0xff) < 0x10) { builder.append('0'); }
                builder.append(Integer.toHexString(b & 0xff));
            } else {
                builder.append((char) b);
            }
        }
        return builder.toString();
    }

    public static class Builder {
        private final String protocol;
        private final Map<String, String> properties = new HashMap<>();

        Builder(String protocol) {
            this.protocol = protocol;
        }

        public Builder property(String key, String value) {
            properties.put(key, value);
            return this;
        }

        public DbusAddress build() {
            return new DbusAddress(protocol, properties);
        }
    }
}
