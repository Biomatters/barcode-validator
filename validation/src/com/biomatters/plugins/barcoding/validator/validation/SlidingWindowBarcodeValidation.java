package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

/**
 * @author Gen Li
 *         Created on 29/09/14 3:03 PM
 */
public class SlidingWindowBarcodeValidation extends SingleBarcodeValidaton {
    /**
     * @param barcodeSequence The user supplied barcode.
     * @param options options Options obtained from calling {@link #getOptions()}
     * @return a {@link com.biomatters.plugins.barcoding.validator.validation.ValidationResult}.
     */
    @Override
    public ValidationResult validate(NucleotideGraphSequenceDocument barcodeSequence, ValidationOptions options) {
        if (!(options instanceof SlidingWindowValidationOptions)) {
            throw new IllegalArgumentException(
                    "Wrong options supplied: " +
                    "Expected: SlidingWindowValidationOptions, " +
                    "actual: " + options.getClass().getSimpleName() + "."
            );
        }

        SlidingWindowValidationOptions SWVOptions = (SlidingWindowValidationOptions)options;

        /* Validate barcodes and accumulate results. */
        try {
            if (!SlidingWindowValidator.validate(barcodeSequence,
                                                 SWVOptions.getWindowSize(),
                                                 SWVOptions.getStepSize(),
                                                 SWVOptions.getMinimumQuality(),
                                                 SWVOptions.getMinimumRatioSatisfied())) {
                return new ValidationResult(true, "Validation success.");
            }
            return new ValidationResult(false, "Barcode '" + barcodeSequence.getName() + "' failed validation.");
        } catch (DocumentOperationException e) {
            return new ValidationResult(false, e.getMessage());
        }
    }

    /**
     * @return Associated options.
     */
    @Override
    public ValidationOptions getOptions() {
        return new SlidingWindowValidationOptions(SlidingWindowBarcodeValidation.class);
    }
}