package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.plugins.barcoding.validator.validation.utilities.ClassUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a task that validates a collection of traces belonging to a single barcode sequence
 *
 * @author Matthew Cheung
 *         Created on 21/08/14 4:50 PM
 */
public abstract class TraceValidation implements Validation {
    public static final String IMPLEMENTS_PAKCAGE = TraceValidation.class.getPackage().getName();
    private static List<TraceValidation> impls = null;

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
    public synchronized static List<TraceValidation> getTraceValidations() {
        return Arrays.<TraceValidation>asList(new SlidingWindowTraceValidation());
    }
}
