package javas.lang;

import com.mhook.localfrida.tool.Debug;

public class Longs_ {
    public static String toUnsignedString(long i) {
        return toUnsignedString(i, 10);
    }

    public static String toUnsignedString(long i, int radix) {
        if (i >= 0)
            return Long.toString(i, radix);
        else {
            switch (radix) {
                case 10:
                    long quot = (i >>> 1) / 5;
                    long rem = i - quot * 10;
                    return Long.toString(quot) + rem;
                    default:
                        throw new IllegalArgumentException("this must be 10");
            }
        }
    }

}
