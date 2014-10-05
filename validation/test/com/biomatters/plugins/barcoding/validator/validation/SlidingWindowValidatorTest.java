package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.*;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.CharSequenceUtilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gen Li
 *         Created on 29/09/14 8:44 AM
 */
public class SlidingWindowValidatorTest extends Assert {
    @Test
    public void testMinimumSatisfiedRatio() throws DocumentOperationException {
        int[] q1 = { 0, 0, 0, 0, 1 };
        int[] q2 = { 0, 0, 0, 1, 1 };
        int[] q3 = { 0, 0, 1, 1, 1 };
        int[] q4 = { 0, 1, 1, 1, 1 };

        NucleotideGraphSequenceDocument d1 = createTestDocument(q1);
        NucleotideGraphSequenceDocument d2 = createTestDocument(q2);
        NucleotideGraphSequenceDocument d3 = createTestDocument(q3);
        NucleotideGraphSequenceDocument d4 = createTestDocument(q4);

        assertTrue(SlidingWindowValidator.validate(d1, 5, 1, 1, 19.9));
        assertTrue(SlidingWindowValidator.validate(d1, 5, 1, 1, 20.0));
        assertFalse(SlidingWindowValidator.validate(d1, 5, 1, 1, 20.1));

        assertTrue(SlidingWindowValidator.validate(d2, 5, 1, 1, 39.9));
        assertTrue(SlidingWindowValidator.validate(d2, 5, 1, 1, 40.0));
        assertFalse(SlidingWindowValidator.validate(d2, 5, 1, 1, 40.1));

        assertTrue(SlidingWindowValidator.validate(d3, 5, 1, 1, 59.9));
        assertTrue(SlidingWindowValidator.validate(d3, 5, 1, 1, 60.0));
        assertFalse(SlidingWindowValidator.validate(d3, 5, 1, 1, 60.1));

        assertTrue(SlidingWindowValidator.validate(d4, 5, 1, 1, 79.9));
        assertTrue(SlidingWindowValidator.validate(d4, 5, 1, 1, 80.0));
        assertFalse(SlidingWindowValidator.validate(d4, 5, 1, 1, 80.1));
    }

    @Test
    public void testWorksWithStepSizeGreaterThanOne() throws DocumentOperationException {
        NucleotideGraphSequenceDocument testDoc = createTestDocument(
                0, 1, 0, 1,
                0, 1, 0, 1,
                0, 1, 0, 1);

        assertTrue(SlidingWindowValidator.validate(testDoc, 4, 2, 1, 50.0));
        assertTrue(SlidingWindowValidator.validate(testDoc, 6, 2, 1, 50.0));
        assertTrue(SlidingWindowValidator.validate(testDoc, 8, 4, 1, 50.0));
    }

    @Test
    public void testIgnoresIncompleteWindow() throws DocumentOperationException {
        NucleotideGraphSequenceDocument testDoc = createTestDocument(0, 1, 1, 0, 0);
        assertTrue(SlidingWindowValidator.validate(testDoc, 3, 2, 1, 30.0));
        assertTrue(SlidingWindowValidator.validate(testDoc, 4, 2, 1, 30.0));
    }

    private static NucleotideGraphSequenceDocument createTestDocument(int... qualityArray) {
        NucleotideGraph g1 = new DefaultNucleotideGraph(null, null, qualityArray, qualityArray.length, 0);
        return new DefaultNucleotideGraphSequence("", null,
                CharSequenceUtilities.repeatedCharSequence("X", qualityArray.length), null, g1);
    }
}