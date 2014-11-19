package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.validation.results.QualityValidationResult;

import java.util.List;

/**
 * Validates the quality of sequences using a sliding window approach.
 *
 * @author Gen Li
 *         Created on 25/09/14 1:32 PM
 */
public class SlidingWindowValidation extends SingleSequenceValidation {
    /**
     * Validates the quality of the supplied sequence using a sliding window approach.
     *
     * @param sequence Sequence to validate.
     * @param options Options obtained from calling {@link #getOptions()}.
     * @return Validation result.
     */
    @Override
    public ValidationResult validate(NucleotideGraphSequenceDocument sequence, ValidationOptions options) {
        if (!(options instanceof SlidingWindowValidationOptions)) {
            throw new IllegalArgumentException(
                    "Wrong options supplied: " +
                    "Expected: SlidingWindowValidationOptions, " +
                    "actual: " + options.getClass().getSimpleName() + "."
            );
        }

        SlidingWindowValidationOptions SWVOptions = (SlidingWindowValidationOptions)options;
        ValidationResult result = new ValidationResult(true, null);
        QualityValidationResult entry = new QualityValidationResult();

        /* Validate traces and accumulate results. */
        try {
            QualityValidationResult.StatsFact fact = SlidingWindowValidationAlgorithm.run(
                    sequence,
                    SWVOptions.getWindowSize(),
                    SWVOptions.getStepSize(),
                    SWVOptions.getMinimumQuality(),
                    SWVOptions.getMinimumRatioSatisfied()
            );

            fact.setFactName(sequence.getName());

            entry.addStatsFact(fact);
        } catch (DocumentOperationException e) {
            result = new ValidationResult(false, e.getMessage());

            entry.addStatsFact(new QualityValidationResult.StatsFact());
        }

        result.setEntry(entry);

        return result;
    }

    /**
     * @return Associated options.
     */
    @Override
    public ValidationOptions getOptions() {
        return new SlidingWindowValidationOptions(SlidingWindowValidation.class);
    }
}