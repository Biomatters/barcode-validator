package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocumentWithEditableAnnotations;
import com.biomatters.plugins.barcoding.validator.validation.results.ResultFact;
import com.biomatters.plugins.barcoding.validator.validation.results.SlidingWindowQualityValidationResultEntry;
import com.biomatters.plugins.barcoding.validator.validation.results.ValidationResultEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Validates the quality of sequences using a sliding window approach.
 *
 * @author Gen Li
 *         Created on 25/09/14 1:32 PM
 */
public class SlidingWindowQualityValidation extends SingleSequenceValidation {
    /**
     * Validates the quality of the supplied sequence using a sliding window approach.
     *
     * @param sequence Sequence to validate.
     * @param options Options obtained from calling {@link #getOptions()}.
     * @return Validation result.
     */
    @Override
    public ResultFact validate(NucleotideGraphSequenceDocument sequence, ValidationOptions options) {
        if (!(options instanceof SlidingWindowQualityValidationOptions)) {
            throw new IllegalArgumentException(
                    "Wrong options supplied: " +
                    "Expected: SlidingWindowValidationOptions, " +
                    "actual: " + options.getClass().getSimpleName() + "."
            );
        }

        SlidingWindowQualityValidationOptions SWVOptions = (SlidingWindowQualityValidationOptions)options;

        return validate(sequence, SWVOptions.getWindowSize(), SWVOptions.getStepSize(), SWVOptions.getMinimumQuality(), SWVOptions.getMinimumSatisfactionRatio());
    }

    /**
     * @return Associated options.
     */
    @Override
    public ValidationOptions getOptions() {
        return new SlidingWindowQualityValidationOptions(SlidingWindowQualityValidation.class);
    }

    @Override
    public ValidationResultEntry getValidationResultEntry() {
        return new SlidingWindowQualityValidationResultEntry();
    }

    ResultFact validate(NucleotideGraphSequenceDocument sequence, int winSize, int stepSize, int minimumQuality, double minimumSatisfactionRatio) {
        if (stepSize < 1) {
            throw new IllegalArgumentException("Could not validate sequence: Negative step size.");
        }

        if (stepSize > winSize) {
            throw new IllegalArgumentException(
                    "Could not validate sequence: " +
                    "Step size is greater than window size, " +
                    "step size: " + stepSize + ", window size: " + winSize + "."
            );
        }

        String sequenceName = sequence.getName();
        int sequenceLength = sequence.getSequenceLength();
        SequenceAnnotation validationFailureAnnotation = new SequenceAnnotation("Validation Failure", "Insufficient Quality");
        int numberOfFailedWindows = 0;

        SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact result = new SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact(
                sequenceName, sequenceName, Collections.singletonList(sequence.getURN()), false, sequenceLength, 0, ""
        );

        /* Validate sequence. */
        for (int i = 0; i <= sequenceLength - winSize; i += stepSize) {
            if (!validateQualities(getQualityWindow(sequence, i, winSize), minimumQuality, minimumSatisfactionRatio)) {
                validationFailureAnnotation.addInterval(i + 1, i + winSize);

                numberOfFailedWindows++;
            }
        }

        result.setNumberOfFailedWindows(numberOfFailedWindows);

        if (numberOfFailedWindows == 0) {
            if (sequence instanceof SequenceDocumentWithEditableAnnotations) {
                validationFailureAnnotation.setQualifier(
                        "Validation Settings",
                        " Window Size=" + winSize +
                        ", Step Size=" + stepSize +
                        ", Min Quality=" + minimumQuality +
                        ", Min Ratio=" + minimumSatisfactionRatio + "%"
                );
            }

            validationFailureAnnotation.setIntervals(SequenceAnnotationInterval.merge(validationFailureAnnotation.getIntervals(), false));
            List<SequenceAnnotation> sequenceAnnotations = new ArrayList<SequenceAnnotation>();
            sequenceAnnotations.addAll(sequence.getSequenceAnnotations());
            sequenceAnnotations.add(validationFailureAnnotation);
            ((SequenceDocumentWithEditableAnnotations)sequence).setAnnotations(sequenceAnnotations);

            result.setPass(true);
        }

        return result;
    }

    private static int[] getQualityWindow(NucleotideGraphSequenceDocument sequence, int startPos, int winSize) {
        if (!sequence.hasSequenceQualities()) {
            throw new IllegalArgumentException("Sequence document '" + sequence.getName() + "' has no sequence qualities.");
        }

        if (winSize < 1) {
            throw new IllegalArgumentException("Negative window size: " + winSize + ".");
        }

        int[] result = new int[winSize];

        for (int i = 0, j = startPos; j < startPos + winSize; i++, j++) {
            result[i] = sequence.getSequenceQuality(j);
        }

        return result;
    }

    /**
     * Validates the supplied "quality window" against the supplied quality constraints.
     *
     * @param qualityWindow Quality window to validate.
     * @param minQuality Minimum base quality.
     * @param minRatioSatisfied Minimum ratio of bases in the window that must satisfy the supplied minimum base
     *                          quality.
     * @return True if the supplied window passes the validation.
     */
    private static boolean validateQualities(int[] qualityWindow, int minQuality, double minRatioSatisfied) {
        if (minRatioSatisfied < 0 || minRatioSatisfied > 100) {
            throw new IllegalArgumentException(
                    "Minimum ratio satisfied value out of range: " +
                    "valid range: 0 - 100 inclusive, " +
                    "value: " + minRatioSatisfied + "."
            );
        }

        int numSatisfied = 0, qualitiesLength = qualityWindow.length;

        /* Check quality values. */
        for (int quality : qualityWindow) {
            if (quality >= minQuality) {
                numSatisfied++;
            }
        }

        if ((double)numSatisfied/qualitiesLength >= minRatioSatisfied/100) {
            return true;
        }

        return false;
    }
}