package com.library.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private PasswordUtil() {}
    public static String hash(String plain) { return BCrypt.hashpw(plain, BCrypt.gensalt(12)); }
    public static boolean matches(String plain, String hash) {
        try { return BCrypt.checkpw(plain, hash); } catch (IllegalArgumentException e) { return false; }
    }
}
