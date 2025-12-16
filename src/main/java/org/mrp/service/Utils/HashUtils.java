package org.mrp.service.Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public class HashUtils {

    //hashes String then returns both hash and salt
    public static HashResult hashWithSalt(String inputString) throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        md.update(inputString.getBytes(StandardCharsets.UTF_8));
        byte[] hashedBytes = md.digest();

        String saltHex = bytesToHex(salt);
        String hashHex = bytesToHex(hashedBytes);

        return new HashResult(hashHex, saltHex);
    }

    // verify a password against stored hash and salt
    public static boolean verify(String inputString, String storedHash, String storedSalt)
            throws NoSuchAlgorithmException {
        byte[] salt = hexToBytes(storedSalt);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        md.update(inputString.getBytes(StandardCharsets.UTF_8));
        byte[] testHash = md.digest();

        String testHashHex = bytesToHex(testHash);
        return testHashHex.equals(storedHash);
    }

    private static String bytesToHex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }

    private static byte[] hexToBytes(String hex) {
        return HexFormat.of().parseHex(hex);
    }

    public static record HashResult(String hash, String salt) {}
}