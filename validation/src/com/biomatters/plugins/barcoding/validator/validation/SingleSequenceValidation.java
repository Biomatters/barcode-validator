package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;

/**
 * Represents validation tasks that validate a single sequence.
 *
 * @author Gen Li
 *         Created on 18/11/14 10:06 AM
 */
public abstract class SingleSequenceValidation extends Validation {
    /**
     * Validates a single sequence.
     *
     * @param sequence Sequence to validate.
     * @param options The matching options of the SingleSequenceValidation instance.
     * @return Validation result.
     */
    public abstract ValidationResult validate(NucleotideGraphSequenceDocument sequence, ValidationOptions options);
}