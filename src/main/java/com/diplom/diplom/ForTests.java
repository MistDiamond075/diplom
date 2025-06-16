package com.diplom.diplom;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class ForTests {
    public static void main(String[] args) {
        String plaintextPassword = "0000";
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(16,32,2,65536,3);
        String argon2Hash = encoder.encode(plaintextPassword);
        System.out.println("Argon2 Hash: " + argon2Hash);
    }
}
