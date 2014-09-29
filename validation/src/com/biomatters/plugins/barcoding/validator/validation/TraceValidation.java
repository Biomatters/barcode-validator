package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.Options;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a task that validates a collection of traces belonging to a single barcode sequence
 *
 * @author Matthew Cheung
 *         Created on 21/08/14 4:50 PM
 */
public abstract class TraceValidation implements Validation {

    /**
     * Validates a set of traces.
     *
     * @param traces The user supplied traces
     * @param options Options obtained from calling {@link #getOptions()}
     * @return a {@link com.biomatters.plugins.barcoding.validator.validation.ValidationResult}
     */
    public abstract ValidationResult validate(List<NucleotideGraphSequenceDocument> traces, ValidationOptions options);

    /**
     * @return List of TraceValidation objects.
     */
    public static List<TraceValidation> getTraceValidations() { // todo: load using classpath?
        TraceValidation[] traceValidations = {
                new SlidingWindowTraceValidation()
        };

        return Arrays.asList(traceValidations);
    }
}
