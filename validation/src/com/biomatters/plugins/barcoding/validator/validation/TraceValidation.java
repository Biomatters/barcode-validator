package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;

import java.util.List;

/**
 * Represents a task that validates a collection of traces belonging to a single barcode sequence
 *
 * @author Matthew Cheung
 *         Created on 21/08/14 4:50 PM
 */
public interface TraceValidation {

    /**
     * Validates a set of traces.
     *
     * @param traces The user supplied traces
     * @return a {@link com.biomatters.plugins.barcoding.validator.validation.ValidationResult}
     */
    public ValidationResult validate(List<NucleotideGraphSequenceDocument> traces);
}
