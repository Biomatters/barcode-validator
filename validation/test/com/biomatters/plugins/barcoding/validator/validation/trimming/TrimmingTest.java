package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.DefaultNucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.CharSequenceUtilities;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;


/**
 * @author Gen Li
 *         Created on 10/09/14 7:08 AM
 */
public class TrimmingTest extends Assert {
    @Test
    public void testRedundantTrimSequence() throws DocumentOperationException {
        SequenceCharSequence sequence = SequenceCharSequence.valueOf("TAGCTAGC");
        Trimmage trimmage = new Trimmage(0, 0);
        assertEquals(sequence, getTrimmedCharSequence(sequence, trimmage));
    }

    private SequenceCharSequence getTrimmedCharSequence(SequenceCharSequence sequence, Trimmage trimmage) {
        DefaultNucleotideGraphSequence toTrim = new DefaultNucleotideGraphSequence("toTrim", "", sequence, new Date(),
                DefaultNucleotideGraph.createNucleotideGraph(new int[sequence.length()], 0, 0));
        return SequenceTrimmer.trimNucleotideGraphSequenceDocument(toTrim, trimmage).getCharSequence();
    }

    @Test
    public void testTrimSequence() {
        SequenceCharSequence sequence = SequenceCharSequence.valueOf("TAGCTAGC");
        Trimmage trimmage = new Trimmage(1, 2);

        assertEquals(SequenceCharSequence.valueOf("AGCTA"), getTrimmedCharSequence(sequence, trimmage));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testOverTrimSequence() {
        SequenceCharSequence sequence = SequenceCharSequence.valueOf("TAGCTAGC");
        Trimmage trimmage = new Trimmage(4, 5);

        assertEquals(SequenceCharSequence.valueOf(""), getTrimmedCharSequence(sequence, trimmage));
    }

    private int[] getTrimmedQualityArray(int[] quality, Trimmage trimmage) {
        DefaultNucleotideGraphSequence toTrim = new DefaultNucleotideGraphSequence("toTrim", "",
                CharSequenceUtilities.repeatedCharSequence("A", quality.length), new Date(),
                DefaultNucleotideGraph.createNucleotideGraph(quality, 0, 0));
        NucleotideGraphSequenceDocument trimmed = SequenceTrimmer.trimNucleotideGraphSequenceDocument(toTrim, trimmage);
        return DefaultNucleotideGraph.getSequenceQualities(trimmed);
    }

    @Test
    public void testRedundantTrimQualities() {
        int[] qualities = { 0, 1, 2, 3, 4, 5, 6, 7};
        Trimmage trimmage = new Trimmage(0, 0);

        assertArrayEquals(qualities, getTrimmedQualityArray(qualities, trimmage));
    }

    @Test
    public void testTrimQualities() {
        int[] qualities = { 0, 1, 2, 3, 4, 5, 6, 7};
        Trimmage trimmage = new Trimmage(1, 2);

        assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, getTrimmedQualityArray(qualities, trimmage));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOverTrimQualities() {
        int[] qualities = { 0, 1, 2, 3, 4, 5, 6, 7};
        Trimmage trimmage = new Trimmage(4, 5);

        getTrimmedQualityArray(qualities, trimmage);
    }

    @Test
    public void testRedundentTrimChromatogramPositions() {
        int[] positions = { 0, 1, 2, 3, 4, 5, 6, 7};
        Trimmage trimmage = new Trimmage(0, 0);

        assertArrayEquals(positions, getTrimmedQualityArray(positions, trimmage));
    }

    @Test
    public void testTrimChromatogramPositions() {
        int[] positions = { 0, 1, 2, 3, 4, 5, 6, 7};
        Trimmage trimmage = new Trimmage(1, 2);

        assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, getTrimmedQualityArray(positions, trimmage));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOverTrimChromatogramPositions() {
        int[] positions = { 0, 1, 2, 3, 4, 5, 6, 7};
        Trimmage trimmage = new Trimmage(4, 5);

        getTrimmedQualityArray(positions, trimmage);
    }
}