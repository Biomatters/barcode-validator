package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 25/09/14 1:32 PM
 */
public class SlidingWindowTraceValidation extends TraceValidation {
    @Override
    public ValidationResult validate(List<NucleotideGraphSequenceDocument> traces, Options options) {
        if (!(options instanceof SlidingWindowValidationOptions))
            throw new IllegalArgumentException("Wrong options supplied: " +
                                               "Expected: SlidingWindowValidationOptions, " +
                                               "actual: " + options.getClass().getSimpleName() + ".");

        SlidingWindowValidationOptions SWVOptions = (SlidingWindowValidationOptions)options;

        List<String> failedTraceNames = new ArrayList<String>();

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

        return new ValidationResult(true, "Validation success.");
    }

    @Override
    public ValidationOptions getOptions() {
        return new SlidingWindowValidationOptions();
    }

    private static String getValidationFailureMessage(List<String> failedTraceNames) {
        return "Validation failure. Failed traces: " + StringUtilities.join(", ", failedTraceNames) + ".";
    }
}
