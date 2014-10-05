package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.DefaultNucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

/**
 * @author Gen Li
 *         Created on 10/09/14 7:08 AM
 */
public class TrimmingTest extends Assert {
    private CharSequence sequence = "ACGTACGTACGTACGTACGT";
    private int[][] chromatograms = {
            { 70, 40, 40, 40, 70, 40, 40, 40, 70, 40, 40, 40, 70, 40, 40, 40, 70, 40, 40, 40 },
            { 50, 70, 50, 50, 50, 70, 50, 50, 50, 70, 50, 50, 50, 70, 50, 50, 50, 70, 50, 50 },
            { 60, 60, 70, 60, 60, 60, 70, 60, 60, 60, 70, 60, 60, 60, 70, 60, 60, 60, 70, 60 },
            { 40, 50, 60, 70, 40, 50, 60, 70, 40, 50, 60, 70, 40, 50, 60, 70, 40, 50, 60, 70 }
    };
    private int[] chromatogramPositions = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
    private int[] qualities = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };

    private NucleotideGraph graph = DefaultNucleotideGraph.createNucleotideGraph(chromatograms, chromatogramPositions, qualities, sequence.length(), chromatograms[0].length);

    private NucleotideGraphSequenceDocument document = new DefaultNucleotideGraphSequence("document", "Test Document", sequence, new Date(), graph);

    @Test
    public void testTrim() throws DocumentOperationException {
        testTrim(document, 5, 5);
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
        NucleotideGraphSequenceDocument trimmedDocument = SequenceTrimmer.trimSequence(document, trimmage);

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
        /* Check chromatograms. */
        assertArrayEquals(
                new int[][] {
                        Arrays.copyOfRange(originalChromatograms[0], fromBeginning, originalChromatograms[0].length - fromEnd),
                        Arrays.copyOfRange(originalChromatograms[1], fromBeginning, originalChromatograms[1].length - fromEnd),
                        Arrays.copyOfRange(originalChromatograms[2], fromBeginning, originalChromatograms[2].length - fromEnd),
                        Arrays.copyOfRange(originalChromatograms[3], fromBeginning, originalChromatograms[3].length - fromEnd)

                },
                trimmedChromatograms
        );
        /* Check chromatogram positions. */
        assertArrayEquals(Arrays.copyOfRange(originalChromatogramPositions, 0, originalChromatogramPositions.length - fromBeginning - fromEnd), trimmedChromatogramPositions);
        /* Check qualities. */
        assertArrayEquals(Arrays.copyOfRange(originalQualities, fromBeginning, originalQualities.length - fromEnd), trimmedQualities);
    }
}