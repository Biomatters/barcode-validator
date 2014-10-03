package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author Gen Li
 *         Created on 10/09/14 7:08 AM
 */
public class TrimmingTest extends Assert {
    @Test
    public void testRedundantTrimSequence() {
        SequenceCharSequence sequence = SequenceCharSequence.valueOf("TAGCTAGC");
        Trimmage trimmage = new Trimmage(0, 0);

        assertEquals(sequence, SequenceTrimmer.trimCharacterSequence(sequence, trimmage));
    }

    @Test
    public void testTrimSequence() {
        SequenceCharSequence sequence = SequenceCharSequence.valueOf("TAGCTAGC");
        Trimmage trimmage = new Trimmage(1, 2);

        assertEquals(SequenceCharSequence.valueOf("AGCTA"), SequenceTrimmer.trimCharacterSequence(sequence, trimmage));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testOverTrimSequence() {
        SequenceCharSequence sequence = SequenceCharSequence.valueOf("TAGCTAGC");
        Trimmage trimmage = new Trimmage(4, 5);

        assertEquals(SequenceCharSequence.valueOf(""), SequenceTrimmer.trimCharacterSequence(sequence, trimmage));
    }

    @Test
    public void testRedundantTrimQualities() {
        int[] qualities = { 0, 1, 2, 3, 4, 5, 6, 7};
        Trimmage trimmage = new Trimmage(0, 0);

        assertArrayEquals(qualities, SequenceTrimmer.trimQualities(qualities, trimmage));
    }

    @Test
    public void testTrimQualities() {
        int[] qualities = { 0, 1, 2, 3, 4, 5, 6, 7};
        Trimmage trimmage = new Trimmage(1, 2);

        assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, SequenceTrimmer.trimQualities(qualities, trimmage));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOverTrimQualities() {
        int[] qualities = { 0, 1, 2, 3, 4, 5, 6, 7};
        Trimmage trimmage = new Trimmage(4, 5);

        SequenceTrimmer.trimQualities(qualities, trimmage);
    }

    @Test
    public void testRedundentTrimChromatogramPositions() {
        int[] positions = { 0, 1, 2, 3, 4, 5, 6, 7};
        Trimmage trimmage = new Trimmage(0, 0);

        assertArrayEquals(positions, SequenceTrimmer.trimChromatogramPositionsForResidues(positions, trimmage));
    }

    @Test
    public void testTrimChromatogramPositions() {
        int[] positions = { 0, 1, 2, 3, 4, 5, 6, 7};
        Trimmage trimmage = new Trimmage(1, 2);

        assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, SequenceTrimmer.trimChromatogramPositionsForResidues(positions, trimmage));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOverTrimChromatogramPositions() {
        int[] positions = { 0, 1, 2, 3, 4, 5, 6, 7};
        Trimmage trimmage = new Trimmage(4, 5);

        SequenceTrimmer.trimChromatogramPositionsForResidues(positions, trimmage);
    }
}