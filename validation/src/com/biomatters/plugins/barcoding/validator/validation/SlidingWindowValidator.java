package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

/**
 * @author Gen Li
 *         Created on 22/09/14 9:43 AM
 */
public class SlidingWindowValidator {
    public static boolean validate(NucleotideGraphSequenceDocument sequence,
                                   int winSize,
                                   int stepSize,
                                   int minQuality,
                                   double minRatioSatisfied) throws DocumentOperationException {
        try {
            for (int i = 0; i <= sequence.getSequenceLength() - winSize; i += stepSize) {
                if (!validateQualities(getQualityWindow(sequence, i, winSize), minQuality, minRatioSatisfied)) {
                    return false;
                }
            }
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not validate sequence: " + e.getMessage(), e);
        }

        return true;
    }

    private static int[] getQualityWindow(NucleotideGraphSequenceDocument sequence, int startPos, int winSize)
            throws DocumentOperationException {
        if (!sequence.hasSequenceQualities()) {
            throw new DocumentOperationException("Sequence document '" + sequence.getName() + "' " +
                                                 "has no sequence qualities.");
        }

        if (winSize < 1) {
            throw new DocumentOperationException("Negative window size: " + winSize + ".");
        }

        if (startPos < 0 || sequence.getSequenceLength() < startPos + winSize) {
            throw new DocumentOperationException("Window coverage out of bounds, " +
                                                 "valid range: " +
                                                 "0 - " + (sequence.getSequenceLength() - 1) + ", " +
                                                 "window coverage: " +
                                                 startPos + " - " + (startPos + winSize - 1) + ".");
        }

        int[] result = new int[winSize];

        for (int i = 0, j = startPos; j < startPos + winSize; i++, j++) {
            result[i] = sequence.getSequenceQuality(j);
        }

        return result;
    }

    private static boolean validateQualities(int[] qualities, int minQuality, double minRatioSatisfied) {
        if (minRatioSatisfied < 0 || minRatioSatisfied > 1) {
            throw new IllegalArgumentException("Minimum ratio satisfied value out of range, " +
                                               "valid range: 0 - 1 inclusive, " +
                                               "actual value: " + minRatioSatisfied + ".");
        }

        int satisfied = 0, qualitiesLength = qualities.length;

        /* Check quality values. */
        for (int quality : qualities) {
            if (quality >= minQuality) {
                satisfied++;
            }
        }

        if (satisfied/qualitiesLength >= minRatioSatisfied) {
            return true;
        }

        return false;
    }
}