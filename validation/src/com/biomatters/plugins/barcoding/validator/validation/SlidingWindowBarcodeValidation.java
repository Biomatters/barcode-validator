package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;

/**
 * @author Gen Li
 *         Created on 29/09/14 3:03 PM
 */
public class SlidingWindowBarcodeValidation extends BarcodeValidation {
    @Override
    public ValidationResult validate(SequenceDocument originalSequence, SequenceDocument generatedSequence, ValidationOptions options) {
        return null;
    }

    /**
     * @return Associated options.
     */
    @Override
    public ValidationOptions getOptions() {
        return new SlidingWindowValidationOptions(SlidingWindowBarcodeValidation.class);
    }
}