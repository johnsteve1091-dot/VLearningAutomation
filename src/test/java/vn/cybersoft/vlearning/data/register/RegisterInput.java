package vn.cybersoft.vlearning.data.register;

public final class RegisterInput {

    private RegisterInput() {
    }

    public static final class Correct {

        public static final String FULL_NAME = "Tester Group";
        public static final String PASSWORD = "AutoTest@123";
        public static final String PHONE = "0901234567";

        private Correct() {
        }
    }

    public static final class Wrong {

        public static final String ACCOUNT_TOO_SHORT = "a";
        public static final String ACCOUNT_TOO_LONG = "a".repeat(17);
        public static final String FULL_NAME_WITH_NUMBER = "a1";
        public static final String PASSWORD_INVALID = ".";
        public static final String EMAIL_INVALID = "a@gm";
        public static final String PHONE_INVALID = "a";

        private Wrong() {
        }
    }

    public static final class Existing {

        public static final String ACCOUNT_PLACEHOLDER = "REPLACE_WITH_EXISTING_ACCOUNT";
        public static final String EMAIL_PLACEHOLDER = "REPLACE_WITH_EXISTING_EMAIL";

        // Replace ACCOUNT and EMAIL with known database values for standalone duplicate tests.
        // Keep the placeholder markers above unchanged so the tests can reject unfilled data.
        public static final String ACCOUNT = ACCOUNT_PLACEHOLDER;
        public static final String EMAIL = EMAIL_PLACEHOLDER;

        private Existing() {
        }
    }
}
