package vn.cybersoft.vlearning.data.login;

import java.util.UUID;

public final class LoginSupport {

    private LoginSupport() {
    }

    public static String generateInvalidAccount() {
        // This synthetic account also exceeds the current 16-character registration limit.
        return "invaliduser" + UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateInvalidPassword() {
        return "Invalid!" + UUID.randomUUID();
    }
}
