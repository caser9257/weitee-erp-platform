package cn.weitee.erp.module.system.framework.validation;

/**
 * Shared validation constants for system module.
 */
public final class SystemValidationConstants {

    private SystemValidationConstants() {
    }

    /**
     * Username supports Chinese characters, letters and digits, length 1-10.
     */
    public static final String USERNAME_PATTERN = "^[\\u4e00-\\u9fa5a-zA-Z0-9]{1,10}$";

}