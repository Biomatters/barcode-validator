package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation task on trace quality using sliding window based algorithm.
 *
 * @author Gen Li
 *         Created on 25/09/14 1:32 PM
 */
public class SlidingWindowTraceValidation extends TraceValidation {
    /**
     * Validates traces.
     *
     * @param traces The user supplied traces.
     * @param options Options obtained from calling {@link #getOptions()}.
     * @return Validation result.
     */
    @Override
    public ValidationResult validate(List<NucleotideGraphSequenceDocument> traces, ValidationOptions options) {
        if (!(options instanceof SlidingWindowValidationOptions)) {
            throw new IllegalArgumentException(
                    "Wrong options supplied: " +
                    "Expected: SlidingWindowValidationOptions, " +
                    "actual: " + options.getClass().getSimpleName() + "."
            );
        }

        SlidingWindowValidationOptions SWVOptions = (SlidingWindowValidationOptions)options;
        List<String> failedTraceNames = new ArrayList<String>();

        /* Validate traces and accumulate results. */
        for (NucleotideGraphSequenceDocument trace : traces) {
            try {
                if (!SlidingWindowValidator.validate(trace,
                                                     SWVOptions.getWindowSize(),
                                                     SWVOptions.getStepSize(),
                                                     SWVOptions.getMinimumQuality(),
                                                     SWVOptions.getMinimumRatioSatisfied())) {
                    failedTraceNames.add(trace.getName());
                }
            } catch (DocumentOperationException e) {
                return new ValidationResult(false, e.getMessage());
            }
        }

        if (!failedTraceNames.isEmpty()) {
            return new ValidationResult(false, getValidationFailureMessage(failedTraceNames));
        }

        return new ValidationResult(true, null);
    }

    /**
     * @return Associated options.
     */
    @Override
    public ValidationOptions getOptions() {
        return new SlidingWindowValidationOptions(SlidingWindowTraceValidation.class);
    }

    /**
     * Returns message for when one or more traces fail validation.
     *
     * @param failedTraceNames Names of traces that failed validation.
     * @return Message.
     */
    private static String getValidationFailureMessage(List<String> failedTraceNames) {
        return "Failed traces: " + StringUtilities.join(", ", failedTraceNames) + ".";
    }
}