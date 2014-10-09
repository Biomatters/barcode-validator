package com.biomatters.plugins.barcoding.validator.validation;

/**
 *
* @author Matthew Cheung
*         Created on 9/10/14 8:44 PM
*/
public enum ValidationGroup {
    TRACE_VALIDATION_GROUP("Trace Validation"),
    BARCODE_VALIDATION_GROUP("Barcode Validation");

    private String label;

    ValidationGroup(String label) {
        this.label = label;
    }

    /**
     *
     * @return A label intended to describe the category of validation to the user in a few short words
     */
    public String getLabel() {
        return label;
    }
}
