package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
import com.biomatters.plugins.barcoding.validator.validation.results.QualityValidationResult;

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
        ValidationResult result = new ValidationResult(true, null);
        QualityValidationResult entry = new QualityValidationResult();
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
        int index = 0;
        for (NucleotideGraphSequenceDocument trace : traces) {
            try {
                QualityValidationResult.StatsFact fact = SlidingWindowValidator.validate(trace,
                                                                                         SWVOptions.getWindowSize(),
                                                                                         SWVOptions.getStepSize(),
                                                                                         SWVOptions.getMinimumQuality(),
                                                                                         SWVOptions.getMinimumRatioSatisfied());
                fact.setFactName("Stat" + ++index);
                entry.addStatsFact(fact);
        } catch (DocumentOperationException e) {
                result = new ValidationResult(false, e.getMessage());
                entry.addStatsFact(new QualityValidationResult.StatsFact());
            }
        }

        QualityValidationResult.TRACE_NUM = QualityValidationResult.TRACE_NUM > index ? QualityValidationResult.TRACE_NUM : index;
        result.setEntry(entry);
        return result;
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