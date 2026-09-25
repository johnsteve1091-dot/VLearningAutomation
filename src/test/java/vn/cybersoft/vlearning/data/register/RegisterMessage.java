package vn.cybersoft.vlearning.data.register;

import java.util.List;
import java.util.regex.Pattern;

public final class RegisterMessage {

    private RegisterMessage() {
    }

    public static final class Empty {

        public static final String ACCOUNT = "Tài khoản không được để trống";
        public static final String FULL_NAME = "Tên không được để trống";
        // Preserve the required Password wording while the application names the wrong field.
        public static final String PASSWORD = "Mật khẩu không được để trống";
        public static final String EMAIL = "Email không được để trống";
        public static final String PHONE = "Số điện thoại không được để trống";

        private Empty() {
        }
    }

    public static final class Format {

        public static final String ACCOUNT_TOO_SHORT = "Tài khoản quá ít kí tự";
        public static final String ACCOUNT_TOO_LONG = "Tài khoản quá 16 kí tự";
        public static final String FULL_NAME_INVALID = "Chỉ nhập kí tự chữ";
        public static final String EMAIL_INVALID = "Email không hợp lệ";
        // The application's current "định đạng" typo must still fail this expectation.
        public static final String PHONE_INVALID = "Số điện thoại chưa đúng định dạng";

        private Format() {
        }
    }

    public static final class PasswordRequirement {

        public static final Pattern MINIMUM_LENGTH_PATTERN =
                Pattern.compile("(?s).*(?:ít nhất|tối thiểu)\\s+8\\b.*");
        public static final List<String> UPPERCASE_WORDING =
                List.of("chữ hoa", "chữ in hoa", "viết hoa");
        public static final List<String> LOWERCASE_WORDING =
                List.of("chữ thường", "chữ in thường", "viết thường");
        public static final String NUMBER_WORDING = "số";
        public static final List<String> SPECIAL_CHARACTER_WORDING =
                List.of("kí tự đặc biệt", "ký tự đặc biệt");

        private PasswordRequirement() {
        }
    }
}
