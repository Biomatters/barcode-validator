package com.biomatters.plugins.barcoding.validator.validation;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a task that validates a collection of barcode sequences
 *
 * @author Matthew Cheung
 *         Created on 21/08/14 4:52 PM
 */
public abstract class BarcodeValidation implements Validation {

    /**
     * @return List of BarcodeValidation objects.
     */
    public static List<BarcodeValidation> getBarcodeValidations() {
        BarcodeValidation[] barcodeValidations = {
                new BarcodeConsensusValidation()
        };

        return Arrays.asList(barcodeValidations);
    }
}