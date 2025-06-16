package com.diplom.diplom.misc.utils;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import java.io.File;
import java.util.Objects;

public class Generator {
    public static String generateCryptedPassword(String password) {
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(16,32,2,65536,3);
        //System.out.println(hashedPassword);
        return encoder.encode(password);
    }

    public static String generateETag(File file) {
        long lastModified = file.lastModified();
        long length = file.length();
        return "\"" + Integer.toHexString(Objects.hash(lastModified, length)) + "\"";
    }
}
