package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.DefaultNucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.SequenceUtilities;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

/**
 * @author Gen Li
 *         Created on 10/09/14 7:08 AM
 */
public class TrimmingTest extends Assert {
    private final static int CHROMATOGRAM_NUCLEOTIDE_STATE_RANGE_SIZE = 4;
    private CharSequence sequence = "ACGTACGTACGTACGTACGT";
    private int[][] chromatograms = {
            { 0, 70, 0, 0, 40, 40, 40, 70, 0, 40, 40, 40, 70, 40, 40, 40, 70, 40, 40, 40, 70, 40, 40, 0, 0, 40 },
            { 0, 50, 0, 0, 70, 50, 50, 50, 0, 70, 50, 50, 50, 70, 50, 50, 50, 70, 50, 50, 50, 70, 50, 0, 0, 50 },
            { 0, 60, 0, 0, 60, 70, 60, 60, 0, 60, 70, 60, 60, 60, 70, 60, 60, 60, 70, 60, 60, 60, 70, 0, 0, 60 },
            { 0, 40, 0, 0, 50, 60, 70, 40, 0, 50, 60, 70, 40, 50, 60, 70, 40, 50, 60, 70, 40, 50, 60, 0, 0, 70 }
    };
    private int[] chromatogramPositions = { 1, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 24, 25 };
    private int[] qualities = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
    private NucleotideGraph graph = DefaultNucleotideGraph.createNucleotideGraph(chromatograms, chromatogramPositions, qualities, sequence.length(), chromatograms[0].length);
    private NucleotideGraphSequenceDocument document = new DefaultNucleotideGraphSequence("document", "Test Document", sequence, new Date(), graph);

    @Test
    public void testTrim() throws DocumentOperationException {
        testTrim(document, 1, 2);
    }

    @Test
    public void testRedundantTrim() throws DocumentOperationException {
        testTrim(document, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOverTrim() throws DocumentOperationException {
        testTrim(document, document.getSequenceLength(), document.getSequenceLength());
    }

    private void testTrim(NucleotideGraphSequenceDocument document, int fromBeginning, int fromEnd) {
        Trimmage trimmage = new Trimmage(fromBeginning, fromEnd);
        NucleotideGraphSequenceDocument trimmedDocument = SequenceTrimmer.trimSequenceUsingTrimmage(document, trimmage);

        String originalSequence = document.getSequenceString();
        int[][] originalChromatograms = DefaultNucleotideGraph.getChromatogramValues(document);
        int[] originalChromatogramPositions = DefaultNucleotideGraph.getChromatogramPositionsForResidues(document);
        int[] originalQualities = DefaultNucleotideGraph.getSequenceQualities(document);

        String trimmedSequence = trimmedDocument.getSequenceString();
        int[][] trimmedChromatograms = DefaultNucleotideGraph.getChromatogramValues(trimmedDocument);
        int[] trimmedChromatogramPositions = DefaultNucleotideGraph.getChromatogramPositionsForResidues(trimmedDocument);
        int[] trimmedQualities = DefaultNucleotideGraph.getSequenceQualities(trimmedDocument);

        /* Check sequence. */
        assertEquals(originalSequence.substring(fromBeginning, originalSequence.length() - fromEnd), trimmedSequence);
        /* Check chromatograms and chromatogram positions. */
        compareChromatograms(originalChromatograms, trimmedChromatograms, originalChromatogramPositions, trimmedChromatogramPositions, trimmage);
        /* Check qualities. */
        assertArrayEquals(Arrays.copyOfRange(originalQualities, fromBeginning, originalQualities.length - fromEnd), trimmedQualities);
    }

    private void compareChromatograms(int[][] originalChromatograms,
                                      int[][] trimmedChromatograms,
                                      int[] originalChromatogramPositions,
                                      int[] trimmedChromatogramPositions,
                                      Trimmage trimmage) {
        assertChromatogramLengthsAreCorrect(originalChromatograms);
        assertChromatogramLengthsAreCorrect(trimmedChromatograms);

        for (int i = 0; i < trimmedChromatogramPositions.length; i++) {
            for (int j = 0; j < CHROMATOGRAM_NUCLEOTIDE_STATE_RANGE_SIZE; j++) {
                assertEquals(
                        trimmedChromatograms[j][trimmedChromatogramPositions[i]],
                        originalChromatograms[j][originalChromatogramPositions[i + trimmage.trimAtStart]]
                );
            }
        }
    }

    private void assertChromatogramLengthsAreCorrect(int[][] chromatograms) {
        assertEquals(CHROMATOGRAM_NUCLEOTIDE_STATE_RANGE_SIZE, chromatograms.length);

        for (int i = 0; i < CHROMATOGRAM_NUCLEOTIDE_STATE_RANGE_SIZE - 1; i++) {
            assertEquals(chromatograms[i].length, chromatograms[i + 1].length);
        }
    }
}