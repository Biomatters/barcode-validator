package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

/**
 * @author Gen Li
 *         Created on 22/09/14 9:43 AM
 */
public class SlidingWindowValidator {
    public static boolean validate(NucleotideGraphSequenceDocument seq,
                                   int winSize,
                                   int stepSize,
                                   double minQuality,
                                   double minRatioSatisfied) throws DocumentOperationException {
        if (!seq.hasSequenceQualities())
            throw new DocumentOperationException("Sequence document '" + seq.getName() + "' has no sequence qualities.");
        

        return true;
    }

    private static int[] getQualityWindow(NucleotideGraphSequenceDocument sequence, int startPos, int winSize)
            throws IndexOutOfBoundsException {
        if (startPos < 0 || sequence.getSequenceLength() < startPos + winSize)
            throw new IndexOutOfBoundsException("Valid range: 0 - " + sequence.getSequenceLength() + ", " +
                                                "specified range: " + startPos + " - " + startPos + winSize + ".");

        int[] result = new int[winSize];

        for (int i = 0, j = startPos; j < startPos + winSize; i++, j++)
            result[i] = sequence.getSequenceQuality(j);

        return result;
    }

    private static boolean validateQualities(int[] qualities, double minQuality, double minRatioSatisfied) {
        /* Check minQuality and minRatioSatisifed are in range 0 - 1 inclusive. */
        if (minQuality < 0 || minQuality > 1)
            throw new IllegalArgumentException("Minimum quality value out of range, " +
                                               "valid range: 0 - 1, " +
                                               "actual value: " + minQuality);
        if (minRatioSatisfied < 0 || minRatioSatisfied > 1)
            throw new IllegalArgumentException("Minimum ratio satisfied value out of range, " +
                                               "Valid range: 0 - 1, " +
                                               "actual value: " + minRatioSatisfied);

        int satisfied = 0, qualitiesLength = qualities.length;

        /* Check quality values. */
        for (int i = 0; i < qualitiesLength; i++)
            if (qualities[i] >= minQuality)
                satisfied++;

        if (satisfied/qualitiesLength >= minRatioSatisfied)
            return true;

        return false;
    }
}