package vn.cybersoft.vlearning.data.register;

import java.util.UUID;

public final class RegisterSupport {

    private static String createdAccount;
    private static String createdEmail;

    private RegisterSupport() {
    }

    public static String generateUniqueAccount() {
        return "a" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 14);
    }

    public static String generateUniqueEmail() {
        return "auto" + UUID.randomUUID().toString().replace("-", "") + "@gmail.com";
    }

    public static void clearCreatedRegistration() {
        createdAccount = null;
        createdEmail = null;
    }

    // Call only after the successful registration popup has been verified.
    public static void saveCreatedRegistration(String account, String email) {
        createdAccount = account;
        createdEmail = email;
    }

    public static boolean hasCreatedAccount() {
        return createdAccount != null && !createdAccount.isBlank();
    }

    public static boolean hasCreatedEmail() {
        return createdEmail != null && !createdEmail.isBlank();
    }

    public static String getCreatedAccount() {
        return createdAccount;
    }

    public static String getCreatedEmail() {
        return createdEmail;
    }
}
