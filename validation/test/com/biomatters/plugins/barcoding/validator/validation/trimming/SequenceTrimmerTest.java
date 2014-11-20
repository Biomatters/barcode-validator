package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.DefaultNucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.implementations.sequence.OligoSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.validation.ValidationTestUtilities;
import jebl.evolution.sequences.Nucleotides;
import jebl.evolution.sequences.State;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Gen Li
 *         Created on 10/09/14 7:08 AM
 */
public class SequenceTrimmerTest extends Assert {
    private final static int CHROMATOGRAM_NUCLEOTIDE_STATE_RANGE_SIZE = 4;
    private CharSequence charSequenceTrimmage = "ACGTACGTACGTACGTACGT";
    private int[][] chromatogramsTrimmage = {
            { 0, 70, 0, 0, 40, 40, 40, 70, 0, 40, 40, 40, 70, 40, 40, 40, 70, 40, 40, 40, 70, 40, 40, 0, 0, 40 },
            { 0, 50, 0, 0, 70, 50, 50, 50, 0, 70, 50, 50, 50, 70, 50, 50, 50, 70, 50, 50, 50, 70, 50, 0, 0, 50 },
            { 0, 60, 0, 0, 60, 70, 60, 60, 0, 60, 70, 60, 60, 60, 70, 60, 60, 60, 70, 60, 60, 60, 70, 0, 0, 60 },
            { 0, 40, 0, 0, 50, 60, 70, 40, 0, 50, 60, 70, 40, 50, 60, 70, 40, 50, 60, 70, 40, 50, 60, 0, 0, 70 }
    };
    private int[] chromatogramPositionsTrimmage = { 1, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 24, 25 };
    private int[] qualitiesTrimmage = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
    private NucleotideGraph graphTrimmage = DefaultNucleotideGraph.createNucleotideGraph(chromatogramsTrimmage, chromatogramPositionsTrimmage, qualitiesTrimmage, charSequenceTrimmage.length(), chromatogramsTrimmage[0].length);
    private NucleotideGraphSequenceDocument sequenceTrimmage = new DefaultNucleotideGraphSequence("Sequence (Trimmage)", "Sequence for Trimmage tests", charSequenceTrimmage, new Date(), graphTrimmage);

    @Test
    public void testTrimmage() throws DocumentOperationException {
        testTrimmage(sequenceTrimmage, 1, 2);
    }

    @Test
    public void testRedundantTrimmage() throws DocumentOperationException {
        testTrimmage(sequenceTrimmage, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOverflowTrimmage() throws DocumentOperationException {
        testTrimmage(sequenceTrimmage, sequenceTrimmage.getSequenceLength(), sequenceTrimmage.getSequenceLength());
    }

    private void testTrimmage(NucleotideGraphSequenceDocument document, int fromBeginning, int fromEnd) {
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

    private CharSequence charSequencePrimer = "AAAAAAAA";
    private int[][] chromatogramsPrimer = {
            { 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1 }
    };
    private int[] chromatogramPositionsPrimer = { 0, 1, 2, 3, 4, 5, 6, 7 };
    private int[] qualitiesPrimer = { 0, 1, 2, 3, 4, 5, 6, 7 };
    private NucleotideGraph graphPrimer = new DefaultNucleotideGraph(chromatogramsPrimer, chromatogramPositionsPrimer, qualitiesPrimer, charSequencePrimer.length(), chromatogramsPrimer[0].length);
    private NucleotideGraphSequenceDocument sequencePrimer = new DefaultNucleotideGraphSequence("Sequence (Primer)", "Sequence for primer trimming tests", charSequencePrimer, new Date(), graphPrimer);
    private OligoSequenceDocument primer = new OligoSequenceDocument("Primer", "Test Primer", "GGAG", new Date());

    private CharSequence expectedTrimmedSequence = "AAAAAA";

    /**
     * The expected Smith-Waterman alignment for the above sequence and primer is:
     *
     *      {AA}AAAAA
     *       ||
     *    GG{AG}.
     *
     * Trimmed Sequence:     AAAA
     * Number of mismatches: 3
     * Match length:         2
     */

    @Test
    public void testUnderMaxMismatches() {
        testMaxMismatches(4, expectedTrimmedSequence.toString());
    }

    @Test
    public void testEqualsMaxMismatches() {
        testMaxMismatches(3, expectedTrimmedSequence.toString());
    }

    @Test
    public void testOverMaxMismatches() {
        testMaxMismatches(2, charSequencePrimer.toString());
    }

    @Test
    public void testOverMinimumMatchLength() {
        testMinMatchLength(1, expectedTrimmedSequence.toString());
    }

    @Test
    public void testEqualsMinimumMatchLength() {
        testMinMatchLength(2, expectedTrimmedSequence.toString());
    }

    @Test
    public void testUnderMinimumMatchLength() {
        testMinMatchLength(3, charSequencePrimer.toString());
    }

    @Test
    public void worksWithAmbiguities() {
        String primer = "TTATTC";
        String sequence = "TTATTCGGGGGGGGGGGGGGGG";
        String trimmed = "GGGGGGGGGGGGGGGG";
        testTrimmingWhenMustMatchExactly(sequence, primer, trimmed);
        for (State state : Nucleotides.getStates()) {
            if(state == Nucleotides.GAP_STATE) {
                continue;  // Skip the gap since only aligned sequence will have gaps and we don't trim those.
            }
            String newSequence = sequence.replace(Nucleotides.A_STATE.getCode(), state.getCode());
            if(state.getCanonicalStates().contains(Nucleotides.A_STATE)) {
                testTrimmingWhenMustMatchExactly(newSequence, primer, trimmed);
            } else {
                testTrimmingWhenMustMatchExactly(newSequence, primer, newSequence);
            }
        }
    }

    @Test
    public void trimmingOnEnds() {
        String primer = "ACTG";
        String reversePrimer = "TATAG";
        testTrimmingWhenMustMatchExactly("ACTGCTATA", primer, "CTATA");
        testTrimmingWhenMustMatchExactly("ACTGCTATA", reversePrimer, "ACTG");
    }

    @Test
    public void trimsUpToAndIncludingPrimer() {
        String primer = "ACTG";
        String reversePrimer = "TATAG";
        testTrimmingWhenMustMatchExactly("ACACACTGCTATATTTA", primer, "CTATATTTA");
        testTrimmingWhenMustMatchExactly("ACACACTGCTATATTTA", reversePrimer, "ACACACTG");
    }

    @Test
    public void testPartialTrimsAtEnd() {
        String primer = "ACTG";
        String reversePrimer = "TATAG";
        testTrimming("CTGCTAT", primer, "CTAT", 1, 3, (float)PrimerTrimmingOptions.DEFAULT_GAP_OPEN);
        testTrimming("CTGCTAT", reversePrimer, "CTG", 1, 3, (float)PrimerTrimmingOptions.DEFAULT_GAP_OPEN);
    }

    @Test
    public void testTrimsOverMismatch() {
        String basicSequence = "GCGGGCTTTAAACAACTTGAAG";
        String primer = "GCGGGC";

        doIterativeMismatchTest(basicSequence, primer, "TTTAAACAACTTGAAG", true);
    }

    @Test
    public void testTrimsOverMismatchOnReverse() {
        String basicSequence = "CCCCCCCCCCCCCCCCCCCCCTTTGGG";
        String primer = "CCCAAA";

        doIterativeMismatchTest(basicSequence, primer, "CCCCCCCCCCCCCCCCCCCCC", false);
    }

    @Test
    public void testMismatchCheckWithGaps() {
        String untrimmed = "AAACCTTAAGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG";
        String primerWithoutMiddlePiece = "AAATTAA";
        String trimmed = "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG";

        // We set the gap open penalty to 3.0 so that the aligner will more readily open a gap
        //
        // The expected alignment will include 2 gaps.
        // AAACCTTAA
        // AAA--TTAA
        testTrimming(untrimmed, primerWithoutMiddlePiece, trimmed, 2, 7, 3.0f);
        testTrimming(untrimmed, primerWithoutMiddlePiece, untrimmed, 1, 7, 3.0f);
    }

    private void doIterativeMismatchTest(String basicSequence, String primer, String expected, boolean replaceFromFront) {
        for(int i=0; i<primer.length(); i++) {
            char[] chars = basicSequence.toCharArray();
            int index = replaceFromFront ? i : chars.length-i-1;
            chars[index] = 'A';
            String toTest = new String(chars);
            testTrimming(toTest, primer, expected, 1, primer.length()-1, (float)PrimerTrimmingOptions.DEFAULT_GAP_OPEN);
        }
    }

    private void testTrimmingWhenMustMatchExactly(CharSequence sequence, CharSequence primer, CharSequence expected) {
        testTrimming(sequence, primer, expected, 0, 0, (float)PrimerTrimmingOptions.DEFAULT_GAP_OPEN);
    }

    private void testTrimming(CharSequence sequence, CharSequence primer, CharSequence expected, int maxMismatches, int minMatchLength, float gapOpen) {
        DefaultNucleotideGraphSequence seqDoc = ValidationTestUtilities.getTestSequenceWithConsistentQuality(sequence, 40);
        OligoSequenceDocument primerDoc = new OligoSequenceDocument("primer", null, primer, new Date());
        testMatchConstraints(maxMismatches, minMatchLength, gapOpen, seqDoc, primerDoc, expected);
    }

    private void testMaxMismatches(int maxMismatches, String expectedTrimmedSequence) {
        testMatchConstraints(maxMismatches, 0, (float)PrimerTrimmingOptions.DEFAULT_GAP_OPEN, sequencePrimer, primer, expectedTrimmedSequence);
    }

    private void testMinMatchLength(int minMatchLength, String expectedTrimmedSequence) {
        testMatchConstraints(Integer.MAX_VALUE, minMatchLength, (float)PrimerTrimmingOptions.DEFAULT_GAP_OPEN, sequencePrimer, primer, expectedTrimmedSequence);
    }

    private void testMatchConstraints(int maxMismatches, int minMatchLength, float gapOpen, NucleotideGraphSequenceDocument inputSequence, OligoSequenceDocument primer, CharSequence expectedTrimmedSequence) {

        NucleotideGraphSequenceDocument trimmedSequence = SequenceTrimmer.trimSequenceByQualityAndPrimers(
                inputSequence,
                Integer.MAX_VALUE,
                Collections.singletonList(primer),
                gapOpen,
                (float)PrimerTrimmingOptions.DEFAULT_GAP_EXTEND,
                new CostMatrixOption("Scores", "Scores", true).getDefaultValue().getScores(),
                maxMismatches,
                minMatchLength,
                false,
                new AtomicBoolean(false)
        );

        assertEquals(expectedTrimmedSequence, trimmedSequence.getSequenceString());
    }
}