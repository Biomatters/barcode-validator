package com.biomatters.plugins.barcoding.validator.validation;

/**
 * Represents the result of a validation task.
 *
 * @author Matthew Cheung
 *         Created on 21/08/14 4:46 PM
 */
public class ValidationResult {

    private boolean passed;
    private String message;

    /**
     *
     * @param passed true if the validation was successful.  False if the input failed the validation process.
     * @param message Extra information to display to the user about why the validation failed.
     */
    public ValidationResult(boolean passed, String message) {
        this.passed = passed;
        this.message = message;
    }

    /**
     *
     * @return true if the validation passed.  False if not.
     */
    public boolean isPassed() {
        return passed;
    }

    /**
     *
     * @return The reason why the validation failed.  Or null if the validation succeeded.
     */
    public String getMessage() {
        return message;
    }
}