package vn.cybersoft.vlearning.data.account;

import vn.cybersoft.vlearning.data.register.RegisterInput;
import vn.cybersoft.vlearning.data.register.RegisterSupport;
import vn.cybersoft.vlearning.pages.AccountPage.ProfileValues;

/** Dedicated ACCOUNT-006 credentials and alternatives to existing valid Register fixtures. */
public final class AccountInput {

    public static final String UPDATE_ACCOUNT = "testergroup7";
    public static final String UPDATE_PASSWORD = "Zxcvbnm123456789!";
    public static final String UPDATE_SUCCESS_TITLE = "Cập nhật thành công";

    private static final String ALTERNATIVE_FULL_NAME = "Group Tester";
    private static final String ALTERNATIVE_PHONE = "0901234568";

    private AccountInput() {}

    public static ProfileValues differentValidProfile(ProfileValues original) {
        String fullName = RegisterInput.Correct.FULL_NAME.equals(original.fullName())
                ? ALTERNATIVE_FULL_NAME : RegisterInput.Correct.FULL_NAME;
        String phone = RegisterInput.Correct.PHONE.equals(original.phone())
                ? ALTERNATIVE_PHONE : RegisterInput.Correct.PHONE;
        // Generate once and retain this exact value for submission and comparisons.
        String email = RegisterSupport.generateUniqueEmail();
        if (fullName.equals(original.fullName()) || email.equals(original.email())
                || phone.equals(original.phone())) {
            throw new IllegalStateException("ACCOUNT-006 requires three values different from the original profile.");
        }
        return new ProfileValues(fullName, email, phone);
    }
}
