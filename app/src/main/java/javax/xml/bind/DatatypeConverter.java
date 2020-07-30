package javax.xml.bind;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class DatatypeConverter {
    public static byte[] parseHexBinary(String data) {
        try {
            return Hex.decodeHex(data.toCharArray());
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static String printHexBinary(byte[] data) {
        return String.valueOf(Hex.encodeHex(data));
    }
}
