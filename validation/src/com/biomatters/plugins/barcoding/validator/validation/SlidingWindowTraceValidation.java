package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
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
    private double minQuality;
    private double minRatioSatisfied;

    public SlidingWindowTraceValidation(int winSize, int stepSize, double minQuality, double minRatioSatisfied) {
        this.winSize = winSize;
        this.stepSize = stepSize;
        this.minQuality = minQuality;
        this.minRatioSatisfied = minRatioSatisfied;
    }

    @Override
    public ValidationResult validate(List<NucleotideGraphSequenceDocument> traces) {
        List<String> failedTraceNames = new ArrayList<String>();

        for (NucleotideGraphSequenceDocument trace : traces) {
            try {
                if (!SlidingWindowValidator.validate(trace, winSize, stepSize, minQuality, minRatioSatisfied)) {
                    failedTraceNames.add(trace.getName());
                }
            } catch (DocumentOperationException e) {
                return new ValidationResult(false, e.getMessage());
            }
        }

        if (!failedTraceNames.isEmpty()) {
            return new ValidationResult(false, getValidatationFailureMessage(failedTraceNames));
        }

        return new ValidationResult(true, "Validation success.");
    }

    private static String getValidatationFailureMessage(List<String> failedTraceNames) {
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("Validation failure. ")
                      .append("Failed traces: ")
                      .append(StringUtilities.join(", ", failedTraceNames)).append(".");

        return messageBuilder.toString();
    }
}
