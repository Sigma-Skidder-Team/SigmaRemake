package io.github.sst.remake.util.java;

import java.util.regex.Pattern;

public class RegexUtils {

    public static boolean isEmailAValidEmailFormat(String email) {
        Pattern var3 = Pattern.compile("[a-zA-Z0-9_]{2,16}");
        return var3.matcher(email).matches();
    }

    public static boolean isPossibleRefreshToken(String token) {
        if (token.length() > 100) {
            return true;
        }

        return token.matches("^[A-Za-z0-9+/=]+$");
    }

    public static String fixUUID(String uuidString) {
        return uuidString.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"
        );
    }

}
