package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;

/**
 * Represents a task that validates a barcode sequence
 *
 * @author Matthew Cheung
 *         Created on 21/08/14 4:49 PM
 */
public abstract class BarcodeCompareValidation extends BarcodeValidation {

    /**
     * Validate the users supplied barcode sequence.
     *
     * @param originalSequence The user supplied barcode sequence to validate
     * @param generatedSequence The barcode sequence generated from the user supplied traces
     * @param options Options obtained from calling {@link #getOptions()}
     * @return a {@link com.biomatters.plugins.barcoding.validator.validation.ValidationResult}
     */
    public abstract ValidationResult validate(SequenceDocument originalSequence, SequenceDocument generatedSequence, ValidationOptions options);
}