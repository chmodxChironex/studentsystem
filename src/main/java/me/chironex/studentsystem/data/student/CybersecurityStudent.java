package me.chironex.studentsystem.data.student;

import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Represents a cybersecurity student with SHA-256 hashing skill.
 */
public class CybersecurityStudent extends Student {
    @Serial
    private static final long serialVersionUID = 3L;

    protected CybersecurityStudent(int id, String firstName, String lastName, int birthYear) {
        super(id, firstName, lastName, birthYear);
    }

    @Override
    public String executeSkill() {
        String fullName = getFirstName() + " " + getLastName();
        return generateHash(fullName);
    }

    private String generateHash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "Hashing error: " + e.getMessage();
        }
    }
}