package vn.cybersoft.vlearning.data.register;

public final class RegisterInput {

    private RegisterInput() {
    }

    public static final class Correct {

        public static final String ACCOUNT_MAX_LENGTH = "AutoTest12345678";
        public static final String FULL_NAME = "Tester Group";
        public static final String PASSWORD = "AutoTest@123";
        public static final String PHONE = "0901234567";
        public static final String GROUP = "GP01";

        private Correct() {
        }
    }

    public static final class Wrong {

        public static final String ACCOUNT_TOO_SHORT = "a";
        public static final String ACCOUNT_TOO_LONG = "a".repeat(17);
        public static final String FULL_NAME_WITH_NUMBER = "a1";
        public static final String FULL_NAME_WITH_SPECIAL_CHARACTER = "Tester@";
        public static final String PASSWORD_INVALID = ".";
        public static final String PASSWORD_TOO_SHORT = "Aa1@abc";
        public static final String PASSWORD_MISSING_UPPERCASE = "autotest@123";
        public static final String PASSWORD_MISSING_LOWERCASE = "AUTOTEST@123";
        public static final String PASSWORD_MISSING_NUMBER = "AutoTest@abc";
        public static final String PASSWORD_MISSING_SPECIAL_CHARACTER = "AutoTest123";
        public static final String EMAIL_INVALID = "a@gm";
        public static final String PHONE_INVALID = "a";
        public static final String PHONE_TOO_SHORT = "090123456";
        public static final String PHONE_TOO_LONG = "09012345678";
        public static final String PHONE_WITH_LETTER = "090123456a";
        public static final String PHONE_WITH_SPECIAL_CHARACTER = "090123456@";
        public static final String GROUP_ARBITRARY = "ABC123";

        private Wrong() {
        }
    }

    public static final class Existing {

        public static final String ACCOUNT_PLACEHOLDER = "REPLACE_WITH_EXISTING_ACCOUNT";
        public static final String EMAIL_PLACEHOLDER = "REPLACE_WITH_EXISTING_EMAIL";

        public static final String ACCOUNT = "TranMinhDuc109";
        public static final String EMAIL = "Tranminhduc109@gmail.com";

        private Existing() {
        }
    }
}
