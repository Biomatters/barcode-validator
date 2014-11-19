package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocumentWithEditableAnnotations;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.validation.results.QualityValidationResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * "Sliding window" based algorithm that validates the quality of sequences. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 22/09/14 9:43 AM
 */
public class SlidingWindowValidationAlgorithm {
    public static final String FAILED_REGION_NAME = "Insufficient Quality";
    public static final String FAILED_REGION_TYPE = "Validation Failure";

    private SlidingWindowValidationAlgorithm() {
    }

    /**
     * Validates the supplied sequence against the supplied quality-based constraints.
     *
     * @param sequence Sequence to validate.
     * @param winSize Window size.
     * @param stepSize Step size.
     * @param minQuality Minimum base quality.
     * @param minRatioSatisfied The minimum ratio of bases that must satisfy the supplied minimum base quality.
     * @return Result of the validation.
     * @throws DocumentOperationException
     */
    public static QualityValidationResult.StatsFact run(NucleotideGraphSequenceDocument sequence, int winSize, int stepSize, int minQuality, double minRatioSatisfied)
            throws DocumentOperationException {
        if (stepSize < 1) {
            throw new DocumentOperationException("Could not validate sequence: Negative step size.");
        }

        if (stepSize > winSize) {
            throw new DocumentOperationException(
                    "Could not validate sequence: " +
                    "Step size is greater than window size, " +
                    "step size: " + stepSize + ", window size: " + winSize + "."
            );
        }

        SequenceAnnotation validationFailureAnnotation = new SequenceAnnotation(FAILED_REGION_NAME, FAILED_REGION_TYPE);

        /* Validate sequence. */
        boolean passed = false;
        int totalNumberOfWindows = 0, numberOfFailedWindows = 0;

        try {
            for (int i = 0; i <= sequence.getSequenceLength() - winSize; i += stepSize) {
                if (!validateQualities(getQualityWindow(sequence, i, winSize), minQuality, minRatioSatisfied)) {
                    validationFailureAnnotation.addInterval(i + 1, i + winSize);

                    numberOfFailedWindows++;
                }

                totalNumberOfWindows++;
            }
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not validate sequence: " + e.getMessage(), e);
        } finally {
            if (numberOfFailedWindows == 0) {
                passed = true;
            }
        }

        if (!passed && sequence instanceof SequenceDocumentWithEditableAnnotations) {
            validationFailureAnnotation.setQualifier(
                    "Validation Settings",
                    " Window Size=" + winSize +
                    ", Step Size=" + stepSize +
                    ", Min Quality=" + minQuality +
                    ", Min Ratio=" + minRatioSatisfied + "%"
            );

            validationFailureAnnotation.setIntervals(SequenceAnnotationInterval.merge(validationFailureAnnotation.getIntervals(), false));
            List<SequenceAnnotation> sequenceAnnotations = new ArrayList<SequenceAnnotation>();
            sequenceAnnotations.addAll(sequence.getSequenceAnnotations());
            sequenceAnnotations.add(validationFailureAnnotation);
            ((SequenceDocumentWithEditableAnnotations)sequence).setAnnotations(sequenceAnnotations);
        }

        return new QualityValidationResult.StatsFact(sequence.getName(),
                passed,
                numberOfFailedWindows,
                (totalNumberOfWindows - numberOfFailedWindows)/totalNumberOfWindows,
                Collections.singletonList(sequence.getURN())
        );
    }

    private static int[] getQualityWindow(NucleotideGraphSequenceDocument sequence, int startPos, int winSize) throws DocumentOperationException {
        if (!sequence.hasSequenceQualities()) {
            throw new DocumentOperationException("Sequence document '" + sequence.getName() + "' has no sequence qualities.");
        }

        if (winSize < 1) {
            throw new DocumentOperationException("Negative window size: " + winSize + ".");
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