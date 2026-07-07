package com.app.shopsphere.security;

/**
 * Masks email addresses in logs so authentication-related events remain
 * partially redacted.
 */
public class LogMaskUtil {

    private LogMaskUtil() {
    }

    public static String maskEmail(String email) {

        if (email == null || !email.contains("@")) {
            return "***";
        }

        String[] parts = email.split("@", 2);
        String localPart = parts[0];
        String domain = parts[1];

        String maskedLocal = localPart.length() <= 2
                ? localPart.charAt(0) + "*"
                : localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1);

        return maskedLocal + "@" + domain;
    }
}