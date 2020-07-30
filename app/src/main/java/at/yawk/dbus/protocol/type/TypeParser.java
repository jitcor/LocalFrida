/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.type;

import at.yawk.dbus.protocol.object.BasicObject;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
@UtilityClass
public class TypeParser {
    /**
     * Parse a type signature. Type signatures are essentially lists of zero or more type definitions.
     *
     * @param string The type definition string representation.
     * @return The parsed type definitions.
     * @throws MalformedTypeDefinitionException if there are issues in the structure of the type signature
     * @throws BufferUnderflowException         if the input sequence does not represent a full type signature
     */
    public static BasicObject parseTypeSignature(CharSequence string)
            throws MalformedTypeDefinitionException,
                   BufferUnderflowException {
        CharBuffer buffer = CharBuffer.wrap(string);

        List<TypeDefinition> definitions = new ArrayList<>();
        while (buffer.hasRemaining()) {
            definitions.add(parseTypeDefinitionPart(buffer));
        }
        return BasicObject.createSignature(definitions);
    }

    /**
     * Parse a type definition.
     *
     * @param string The type definition string representation.
     * @return The parsed type definition.
     * @throws MalformedTypeDefinitionException if there are issues in the structure of the type definition
     * @throws BufferOverflowException          if the end of the type definition was reached before all input was
     *                                          consumed
     * @throws BufferUnderflowException         if the input sequence does not represent a full type definition
     */
    public static TypeDefinition parseTypeDefinition(CharSequence string)
            throws MalformedTypeDefinitionException,
                   BufferOverflowException,
                   BufferUnderflowException {

        CharBuffer buffer = CharBuffer.wrap(string);
        TypeDefinition def = parseTypeDefinitionPart(buffer);
        if (buffer.hasRemaining()) {
            throw new BufferOverflowException();
        }
        return def;
    }

    private static TypeDefinition parseTypeDefinitionPart(CharBuffer buffer)
            throws MalformedTypeDefinitionException,
                   BufferUnderflowException {

        char code = buffer.get();
        switch (code) {
        case '(':
            List<TypeDefinition> members = new ArrayList<>();
            while (peek(buffer) != ')') {
                members.add(parseTypeDefinitionPart(buffer));
            }
            buffer.get(); // skip )
            return new StructTypeDefinition(members);
        case 'a':
            // peek
            char following = peek(buffer);
            if (following == '{') {
                buffer.get(); // skip {
                TypeDefinition key = parseTypeDefinitionPart(buffer);
                TypeDefinition value = parseTypeDefinitionPart(buffer);
                char closing = buffer.get();
                if (closing != '}') {
                    throw new MalformedTypeDefinitionException(
                            "Dict not closed properly: got '" + escapeChar(closing) + "', expected '}'");
                }
                return new DictTypeDefinition(key, value);
            } else {
                TypeDefinition member = parseTypeDefinitionPart(buffer);
                return new ArrayTypeDefinition(member);
            }
        default:
            BasicType basicType = BasicType.byCode(code);
            if (basicType != null) {
                return basicType;
            }
        }
        throw new MalformedTypeDefinitionException("Unsupported type code: '" + escapeChar(code) + "'");
    }

    private static char peek(CharBuffer buffer) {
        if (!buffer.hasRemaining()) {
            throw new BufferUnderflowException();
        }
        return buffer.get(buffer.position());
    }

    private static String escapeChar(char c) {
        if ((c >= 'a' && c <= 'z') ||
            (c >= 'A' && c <= 'Z') ||
            (c >= '0' && c <= '9') ||
            c == '.' || c == '-' || c == '(' || c == ')' || c == '{' || c == '}') {
            return String.valueOf(c);
        } else {
            return String.format("\\u%04x", (int) c);
        }
    }
}
