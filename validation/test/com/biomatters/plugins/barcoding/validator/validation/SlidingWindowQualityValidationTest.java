package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.DefaultNucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.CharSequenceUtilities;
import com.biomatters.plugins.barcoding.validator.validation.results.SlidingWindowQualityValidationResultEntry;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gen Li
 *         Created on 29/09/14 8:44 AM
 */
public class SlidingWindowQualityValidationTest extends Assert {
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

        assertTrue(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact)(new SlidingWindowQualityValidation().validate(d1, 5, 1, 1, 19.9))).getPass());
        assertTrue(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact)(new SlidingWindowQualityValidation().validate(d1, 5, 1, 1, 20.0))).getPass());
        assertFalse(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact) (new SlidingWindowQualityValidation().validate(d1, 5, 1, 1, 20.1))).getPass());

        assertTrue(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact)(new SlidingWindowQualityValidation().validate(d2, 5, 1, 1, 39.9))).getPass());
        assertTrue(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact)(new SlidingWindowQualityValidation().validate(d2, 5, 1, 1, 40.0))).getPass());
        assertFalse(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact) (new SlidingWindowQualityValidation().validate(d2, 5, 1, 1, 40.1))).getPass());

        assertTrue(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact)(new SlidingWindowQualityValidation().validate(d3, 5, 1, 1, 59.9))).getPass());
        assertTrue(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact)(new SlidingWindowQualityValidation().validate(d3, 5, 1, 1, 60.0))).getPass());
        assertFalse(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact) (new SlidingWindowQualityValidation().validate(d3, 5, 1, 1, 60.1))).getPass());

        assertTrue(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact)(new SlidingWindowQualityValidation().validate(d4, 5, 1, 1, 79.9))).getPass());
        assertTrue(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact)(new SlidingWindowQualityValidation().validate(d4, 5, 1, 1, 80.0))).getPass());
        assertFalse(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact) (new SlidingWindowQualityValidation().validate(d4, 5, 1, 1, 80.1))).getPass());
    }

    @Test
    public void testWorksWithStepSizeGreaterThanOne() throws DocumentOperationException {
        NucleotideGraphSequenceDocument testDoc = createTestDocument(
                0, 1, 0, 1,
                0, 1, 0, 1,
                0, 1, 0, 1
        );

        assertTrue(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact)(new SlidingWindowQualityValidation().validate(testDoc, 4, 2, 1, 50.0))).getPass());
        assertTrue(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact)(new SlidingWindowQualityValidation().validate(testDoc, 6, 2, 1, 50.0))).getPass());
        assertTrue(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact)(new SlidingWindowQualityValidation().validate(testDoc, 8, 4, 1, 50.0))).getPass());
    }

    @Test
    public void testIgnoresIncompleteWindow() throws DocumentOperationException {
        NucleotideGraphSequenceDocument testDoc = createTestDocument(0, 1, 1, 0, 0);

        assertTrue(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact)(new SlidingWindowQualityValidation().validate(testDoc, 3, 2, 1, 30.0))).getPass());
        assertTrue(((SlidingWindowQualityValidationResultEntry.SlidingWindowQualityValidationResultFact)(new SlidingWindowQualityValidation().validate(testDoc, 4, 2, 1, 30.0))).getPass());
    }

    private static NucleotideGraphSequenceDocument createTestDocument(int... qualityArray) {
        NucleotideGraph g1 = new DefaultNucleotideGraph(null, null, qualityArray, qualityArray.length, 0);

        return new DefaultNucleotideGraphSequence("", null, CharSequenceUtilities.repeatedCharSequence("X", qualityArray.length), null, g1);
    }
}