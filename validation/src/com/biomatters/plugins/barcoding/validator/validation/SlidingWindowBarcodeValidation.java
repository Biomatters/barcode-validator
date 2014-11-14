package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;

/**
 * @author Gen Li
 *         Created on 29/09/14 3:03 PM
 */
public class SlidingWindowBarcodeValidation  {  //remove "extends" to prevent BarcodeValidation load it, since it is not available now
    /**
     * @param barcodeSequence The user supplied barcode.
     * @param options options Options obtained from calling {@link #getOptions()}
     * @return a {@link com.biomatters.plugins.barcoding.validator.validation.ValidationResult}.
     */
    public ValidationResult validate(NucleotideGraphSequenceDocument barcodeSequence, ValidationOptions options) {
//        if (!(options instanceof SlidingWindowValidationOptions)) {
//            throw new IllegalArgumentException(
//                    "Wrong options supplied: " +
//                    "Expected: SlidingWindowValidationOptions, " +
//                    "actual: " + options.getClass().getSimpleName() + "."
//            );
//        }
//
//        SlidingWindowValidationOptions SWVOptions = (SlidingWindowValidationOptions)options;
//        try {
//            if (SlidingWindowValidator.validate(barcodeSequence,
//                                                 SWVOptions.getWindowSize(),
//                                                 SWVOptions.getStepSize(),
//                                                 SWVOptions.getMinimumQuality(),
//                                                 SWVOptions.getMinimumRatioSatisfied())) {
//                return new ValidationResult(true, null);
//            }
//            return new ValidationResult(false, "Quality too low.");
//        } catch (DocumentOperationException e) {
//            return new ValidationResult(false, "Error: " + e.getMessage());
//        }
        return null;
    }

    /**
     * @return Associated options.
     */
    public ValidationOptions getOptions() {
        return new SlidingWindowValidationOptions(SlidingWindowBarcodeValidation.class);
    }
}