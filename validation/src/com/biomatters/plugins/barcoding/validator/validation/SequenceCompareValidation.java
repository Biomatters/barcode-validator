package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.plugins.barcoding.validator.validation.results.ResultFact;

/**
 * Represents a task that validates a barcode sequence
 *
 * @author Matthew Cheung
 *         Created on 21/08/14 4:49 PM
 */
public abstract class SequenceCompareValidation extends Validation {

    /**
     * Validate the user supplied barcode sequence against the one generated by the system from the user's traces.
     *
     * @param originalSequence The user supplied barcode sequence to validate
     * @param generatedSequence The barcode sequence generated from the user supplied traces
     * @param options Options to run the validation with. Obtained from calling {@link #getOptions()}
     * @return a {@link com.biomatters.plugins.barcoding.validator.validation.results.ResultFact}
     */
    public abstract ResultFact validate(SequenceDocument originalSequence, SequenceDocument generatedSequence, ValidationOptions options, ValidationCallback callback);
}