/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol;

import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.ObjectPathObject;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java9.util.Maps;
import java9.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Data
@Slf4j
public class MatchRule {
    @Nullable MessageType messageType;
    @Nullable String sender;
    @Nullable String interfaceName;
    @Nullable String member;
    @Nullable ObjectPathObject path;
    @Nullable ObjectPathObject pathNamespace;
    @Nullable String destination;
    @Nullable Map<Integer, String> arguments;
    @Nullable Map<Integer, ObjectPathObject> argumentPaths;
    @Nullable ObjectPathObject arg0Namespace;
    boolean eavesdrop = false;

    public void setSender(@Nonnull String sender) {
        DbusUtil.validateConnectionName(sender);
        this.sender = sender;
    }

    public void setInterfaceName(@Nonnull String interfaceName) {
        DbusUtil.validateConnectionName(interfaceName);
        this.interfaceName = interfaceName;
    }

    public void setMember(@Nonnull String member) {
        DbusUtil.validateMemberName(member);
        this.member = member;
    }

    public void setPath(@Nullable ObjectPathObject path) {
        this.path = path;
    }

    public void setPathNamespace(@Nullable ObjectPathObject pathNamespace) {
        this.pathNamespace = pathNamespace;
    }

    public void setDestination(@Nonnull String destination) {
        DbusUtil.validateConnectionName(destination);
        this.destination = destination;
    }

    public void setArguments(@Nullable Map<Integer, String> arguments) {
        this.arguments = arguments;
    }

    public void setArgumentPaths(@Nullable Map<Integer, ObjectPathObject> argumentPaths) {
        this.argumentPaths = argumentPaths;
    }

    public void setArg0Namespace(@Nullable ObjectPathObject arg0Namespace) {
        this.arg0Namespace = arg0Namespace;
    }

    public void setEavesdrop(boolean eavesdrop) {
        this.eavesdrop = eavesdrop;
    }

    public String serialize() {
        StringBuilder builder = new StringBuilder();
        append(builder, "type", messageType, MessageType::getName);
        append(builder, "sender", sender);
        append(builder, "interface", interfaceName);
        append(builder, "member", member);
        append(builder, "path", path, ObjectPathObject::getSequence);
        append(builder, "path_namespace", pathNamespace, ObjectPathObject::getSequence);
        append(builder, "destination", destination);
        if (arguments != null) {
            Maps.forEach(arguments,(i, v) -> {
                if (i < 0) { throw new IllegalArgumentException("Negative argument index"); }
                if (i > 63) { throw new IllegalArgumentException("Argument index too large"); }
                append(builder, "arg" + i, v);
            });
        }
        if (argumentPaths != null) {
            Maps.forEach(argumentPaths,(i, v) -> {
                if (i < 0) { throw new IllegalArgumentException("Negative argument index"); }
                if (i > 63) { throw new IllegalArgumentException("Argument index too large"); }
                append(builder, "arg" + i + "path", v.getSequence());
            });
        }
        append(builder, "arg0Namespace", arg0Namespace, ObjectPathObject::getSequence);
        append(builder, "eavesdrop", eavesdrop, Object::toString);

        // remove trailing comma
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }

        return builder.toString();
    }

    public boolean matches(DbusMessage message) {
        MessageHeader header = message.getHeader();
        MessageBody bodyObj = message.getBody();
        List<DbusObject> body = bodyObj == null ? Collections.emptyList() : bodyObj.getArguments();

        if (messageType != null &&
            header.getMessageType() != messageType) {
            log.trace("type: {} != {}", messageType, header.getMessageType());
            return false;
        }

        if (sender != null) {
            DbusObject senderObject = header.getHeaderFields().get(HeaderField.SENDER);
            if (senderObject != null &&
                !sender.equals(senderObject.stringValue())) {
                log.trace("sender: {} != {}", sender, senderObject);
                return false;
            }
        }

        if (interfaceName != null) {
            DbusObject interfaceObject = header.getHeaderFields().get(HeaderField.INTERFACE);
            if (interfaceObject != null &&
                !interfaceName.equals(interfaceObject.stringValue())) {
                log.trace("interface: {} != {}", interfaceName, interfaceObject);
                return false;
            }
        }

        if (member != null) {
            DbusObject memberObject = header.getHeaderFields().get(HeaderField.MEMBER);
            if (memberObject != null &&
                !member.equals(memberObject.stringValue())) {
                log.trace("member: {} != {}", member, memberObject);
                return false;
            }
        }

        if (path != null) {
            DbusObject pathObject = header.getHeaderFields().get(HeaderField.PATH);
            if (pathObject != null &&
                !path.equals(pathObject)) {
                log.trace("path: {} != {}", path, pathObject);
                return false;
            }
        }

        if (pathNamespace != null) {
            DbusObject pathObject = header.getHeaderFields().get(HeaderField.PATH);
            if (pathObject != null &&
                !matchesNamespace(pathNamespace, (ObjectPathObject) pathObject)) {
                log.trace("pathNamespace: {} != {}", pathNamespace, pathObject);
                return false;
            }
        }

        /*
        if (destination != null) {
            DbusObject destinationObject = header.getHeaderFields().get(HeaderField.DESTINATION);
            if (destinationObject != null &&
                !destination.equals(destinationObject.stringValue())) {
                log.trace("destination: {} != {}", destination, destinationObject);
                return false;
            }
        }
        */

        if (this.arguments != null) {
            for (Map.Entry<Integer, String> entry : this.arguments.entrySet()) {
                int i = entry.getKey();
                if (i >= body.size()) {
                    return false;
                }
                if (!body.get(i).stringValue().equals(entry.getValue())) {
                    return false;
                }
            }
        }

        if (this.argumentPaths != null) {
            for (Map.Entry<Integer, ObjectPathObject> entry : this.argumentPaths.entrySet()) {
                int i = entry.getKey();
                if (i >= body.size()) {
                    return false;
                }
                // string value compare since we need to be able to match strings too
                if (!body.get(i).stringValue().equals(entry.getValue().stringValue())) {
                    return false;
                }
            }
        }

        if (this.arg0Namespace != null) {
            if (body.isEmpty() ||
                !matchesNamespace(arg0Namespace, (ObjectPathObject) body.get(0))) {
                return false;
            }
        }

        return true;
    }

    private static boolean matchesNamespace(ObjectPathObject namespace, ObjectPathObject member) {
        CharSequence namespaceSequence = namespace.getSequence();
        CharSequence memberSequence = member.getSequence();
        if (namespaceSequence.length() > memberSequence.length()) { return false; }
        for (int i = 0; i < namespaceSequence.length(); i++) {
            if (namespaceSequence.charAt(i) != memberSequence.charAt(i)) {
                return false;
            }
        }
        if (memberSequence.length() > namespaceSequence.length() &&
            memberSequence.charAt(namespaceSequence.length()) != '/') {
            return false;
        }

        return true;
    }

    private static void append(StringBuilder builder, String key, @Nullable CharSequence item) {
        append(builder, key, item, Function.identity());
    }

    private static <T> void append(StringBuilder builder, String key, @Nullable T item, Function<T, CharSequence>
            toString) {
        if (item != null) {
            builder.append(key).append('=');
            escape(toString.apply(item), builder);
            builder.append(',');
        }
    }

    static void escape(CharSequence input, StringBuilder output) {
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
            case '\\':
                output.append("'\\'");
                break;
            case ',':
                output.append("','");
                break;
            case '\'':
                output.append("\\'");
                break;
            default:
                output.append(c);
                break;
            }
        }
    }
}
