package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.*;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
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

        NucleotideGraph g1 = new DefaultNucleotideGraph(null, null, q1, q1.length, 0);
        NucleotideGraph g2 = new DefaultNucleotideGraph(null, null, q2, q2.length, 0);
        NucleotideGraph g3 = new DefaultNucleotideGraph(null, null, q3, q3.length, 0);
        NucleotideGraph g4 = new DefaultNucleotideGraph(null, null, q4, q4.length, 0);

        NucleotideGraphSequenceDocument d1 = new DefaultNucleotideGraphSequence("", null, "XXXXX", null, g1);
        NucleotideGraphSequenceDocument d2 = new DefaultNucleotideGraphSequence("", null, "XXXXX", null, g2);
        NucleotideGraphSequenceDocument d3 = new DefaultNucleotideGraphSequence("", null, "XXXXX", null, g3);
        NucleotideGraphSequenceDocument d4 = new DefaultNucleotideGraphSequence("", null, "XXXXX", null, g4);

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
}