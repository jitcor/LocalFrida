/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol.auth.mechanism;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import at.yawk.dbus.protocol.DbusUtil;
import at.yawk.dbus.protocol.auth.AuthChannel;
import at.yawk.dbus.protocol.auth.AuthenticationException;
import at.yawk.dbus.protocol.auth.UnexpectedCommandException;
import at.yawk.dbus.protocol.auth.command.Auth;
import at.yawk.dbus.protocol.auth.command.Begin;
import at.yawk.dbus.protocol.auth.command.Data;
import at.yawk.dbus.protocol.auth.command.Ok;
import io.netty.handler.codec.DecoderException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java9.util.concurrent.CompletionStage;

/**
 * @author yawkat
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class DbusCookieSha1AuthMechanism implements AuthMechanism {

    private final SecureRandom rng;

    {
        try {
            rng = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public CompletionStage<?> startAuth(AuthChannel channel) throws Exception {
        String username = System.getProperty("user.name");
        return channel.send(new Auth("DBUS_COOKIE_SHA1", username.getBytes())).thenCompose(cmd -> {
            MechanismException.handleCommand(cmd);
            if (cmd instanceof Data) {
                String s = new String(((Data) cmd).getData());
                try {
                    return channel.send(doAuth(s)).thenAccept(cmd2 -> {
                        MechanismException.handleCommand(cmd2);
                        if ((cmd2 instanceof Ok)) {
                            channel.send(new Begin());
                        } else {
                            throw new UnexpectedCommandException(cmd2);
                        }
                    });
                } catch (IOException | NoSuchAlgorithmException e) {
                    throw new AuthenticationException(e);
                }
            } else {
                throw new UnexpectedCommandException(cmd);
            }
        });
    }
    private Data doAuth(String cookieData) throws IOException, NoSuchAlgorithmException {
        String[] parts = cookieData.split(" ");
        if (parts.length != 3) {
            throw new DecoderException("Expected exactly 3 parts, got " + parts.length);
        }
        String cookieContext = parts[0];
        String cookieId = parts[1];
        String serverChallenge = parts[2];
        String userHome = System.getProperty("user.home");
        Path cookiePath = Paths.get(userHome, ".dbus-keyrings", cookieContext);
        Optional<String> cookieOption = Files.lines(cookiePath)
                .map(line -> line.split(" "))
                .filter(arr -> arr.length == 3)
                .filter(arr -> arr[0].equals(cookieId))
                .map(arr -> arr[2])
                .findAny();
        String cookie = cookieOption.orElseThrow(() -> new DecoderException(
                "Could not find cookie with id " + cookieId + " in " + cookiePath));

        String clientChallenge = DbusUtil.printHex(rng.generateSeed(16));
        String blob = serverChallenge + ':' + clientChallenge + ':' + cookie;
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] hash = digest.digest(blob.getBytes());

        String response = clientChallenge + " " + DbusUtil.printHex(hash);
        return new Data(response.getBytes());
    }
}
