package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 25/09/14 1:32 PM
 */
public class SlidingWindowTraceValidation implements TraceValidation {
    private int winSize;
    private int stepSize;
    private int minQuality;
    private double minRatioSatisfied;

    public SlidingWindowTraceValidation(int winSize, int stepSize, int minQuality, double minRatioSatisfied) {
        this.winSize = winSize;
        this.stepSize = stepSize;
        this.minQuality = minQuality;
        this.minRatioSatisfied = minRatioSatisfied;
    }

    @Override
    public ValidationResult validate(List<DefaultNucleotideGraphSequence> traces) {
        List<String> failedTraceNames = new ArrayList<String>();

        for (DefaultNucleotideGraphSequence trace : traces) {
            try {
                if (!SlidingWindowValidator.validate(trace, winSize, stepSize, minQuality, minRatioSatisfied)) {
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

    private static String getValidationFailureMessage(List<String> failedTraceNames) {
        return "Validation failure. Failed traces: " + StringUtilities.join(", ", failedTraceNames) + ".";
    }
}
