package com.biomatters.plugins.barcoding.validator.validation.consensus;

import com.biomatters.geneious.publicapi.documents.sequence.DefaultNucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.DefaultAlignmentDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.CharSequenceUtilities;
import com.biomatters.plugins.barcoding.validator.validation.ValidationTestUtilities;
import jebl.evolution.sequences.Nucleotides;
import jebl.evolution.sequences.State;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * @author Matthew Cheung
 *         Created on 28/10/14 11:38 AM
 */
public class ConsensusUtilitiesTest extends Assert {

    @Test(expected = DocumentOperationException.class)
    public void doesNotWorkWhenQualityIsMissing() throws DocumentOperationException {
        DefaultAlignmentDocument alignment = new DefaultAlignmentDocument("test",
                new DefaultNucleotideSequence("1", "ACTG"),
                new DefaultNucleotideSequence("2", "ACTG"));
        ConsensusUtilities.getConsensus(alignment);
    }

    @Test
    public void canBuildSimpleConsensus() throws DocumentOperationException {
        testSimpleConsensus("AAAA");
        testSimpleConsensus("CCCC");
        testSimpleConsensus("ACTG");

        for(int i=1; i<500; i++) {
            testSimpleConsensus(ValidationTestUtilities.getRandomString(i));
        }
    }

    @Test
    public void usesHighestQualityValue() throws DocumentOperationException {
        DefaultNucleotideGraphSequence highQualitySeq = ValidationTestUtilities.getTestSequenceWithConsistentQuality(ValidationTestUtilities.getRandomString(50), 100);
        DefaultNucleotideGraphSequence lowQualitySeq = ValidationTestUtilities.getTestSequenceWithConsistentQuality(ValidationTestUtilities.getRandomString(50), 20);
        testConsensusFromSequences(highQualitySeq.getSequenceString(), null, highQualitySeq, lowQualitySeq);

        NucleotideGraphSequenceDocument highLowHigh = concatenate(highQualitySeq, lowQualitySeq, highQualitySeq);
        NucleotideGraphSequenceDocument lowHighHigh = concatenate(lowQualitySeq, highQualitySeq, highQualitySeq);
        testConsensusFromSequences(
                CharSequenceUtilities.repeatedCharSequence(highQualitySeq.getCharSequence(), 3).toString(),
                null, highLowHigh, lowHighHigh);
    }

    @Test
    public void ignoresGaps() throws DocumentOperationException {
        DefaultNucleotideGraphSequence seq1 = ValidationTestUtilities.getTestSequenceWithConsistentQuality("AC-AC", 100);
        DefaultNucleotideGraphSequence seq2 = ValidationTestUtilities.getTestSequenceWithConsistentQuality("TTTTT", 50);
        testConsensusFromSequences("ACTAC", new int[]{50, 50, 50, 50, 50}, seq1, seq2);
    }

    @Test
    public void handlesAmbiguities() throws DocumentOperationException {
        DefaultNucleotideGraphSequence seq1 = ValidationTestUtilities.getTestSequenceWithConsistentQuality("R", 100);
        DefaultNucleotideGraphSequence seq2 = ValidationTestUtilities.getTestSequenceWithConsistentQuality("A", 50);
        testConsensusFromSequences("A", new int[]{150}, seq1, seq2);

        seq1 = ValidationTestUtilities.getTestSequence("TACTRD", new int[]{11, 44, 44, 1, 41, 33});
        seq2 = ValidationTestUtilities.getTestSequence("GTGAMB", new int[]{55, 20, 50, 1, 41, 33});
        testConsensusFromSequences("GAGWAK", new int[]{44, 24, 6, 2, 82, 66}, seq1, seq2);
    }

    @Test
    public void generatesAmbiguitiesWhenQualityEqual() throws DocumentOperationException {
        int qualityValue = 100;
        DefaultNucleotideGraphSequence seq1 = ValidationTestUtilities.getTestSequenceWithConsistentQuality("G", qualityValue);
        DefaultNucleotideGraphSequence seq2 = ValidationTestUtilities.getTestSequenceWithConsistentQuality("A", qualityValue);
        testConsensusFromSequences("R", new int[]{qualityValue*2}, seq1, seq2);

        List<State> states = Nucleotides.getStates();
        // Inosine is special.  It is exactly the same as D.  So we'll skip it.
        // Both gap (-) and unknown (?) map to Unknown (N)
        Map<String, String> specialMappings = new HashMap<String, String>();
        specialMappings.put("I", "D");
        specialMappings.put("-", "N");
        specialMappings.put("?", "N");

        for (State state : states) {
            String expected = state.toString();
            if(specialMappings.containsKey(expected)) {
                expected = specialMappings.get(expected);
            }

            Set<State> possibles = state.getCanonicalStates();
            if(possibles.size() > 1) {
                List<NucleotideGraphSequenceDocument> seqs = new ArrayList<NucleotideGraphSequenceDocument>();
                for (State possible : possibles) {
                    seqs.add(ValidationTestUtilities.getTestSequenceWithConsistentQuality(possible.toString(), qualityValue));
                }
                testConsensusFromSequences(expected, new int[]{qualityValue*seqs.size()}, seqs.toArray(new NucleotideGraphSequenceDocument[seqs.size()]));
            }
        }
    }

    private static void testConsensusFromSequences(String expectedConsensus, int[] expectedQuality, NucleotideGraphSequenceDocument... sequences) throws DocumentOperationException {
        DefaultAlignmentDocument alignment = new DefaultAlignmentDocument("test", sequences);
        NucleotideGraphSequenceDocument consensus = ConsensusUtilities.getConsensus(alignment);
        assertEquals(expectedConsensus, consensus.getSequenceString());
        if(expectedQuality != null) {
            assertArrayEquals(expectedQuality, DefaultNucleotideGraph.getSequenceQualities(consensus));
        }
    }

    private static NucleotideGraphSequenceDocument concatenate(NucleotideGraphSequenceDocument... seqs) {
        StringBuilder charSeq = new StringBuilder();
        for (NucleotideGraphSequenceDocument seq : seqs) {
            charSeq.append(seq.getCharSequence());
        }
        int[] qualities = new int[charSeq.length()];
        int index = 0;
        for (NucleotideGraphSequenceDocument seq : seqs) {
            for (int i = 0; i < seq.getSequenceLength(); i++) {
                qualities[index++] = seq.getSequenceQuality(i);
            }
        }
        return new DefaultNucleotideGraphSequence("concatenated", null, charSeq, new Date(),
                new DefaultNucleotideGraph(null, null, qualities, charSeq.length(), 0));
    }

    private static void testSimpleConsensus(String testString) throws DocumentOperationException {
        int qualityValue = 50;
        DefaultNucleotideGraphSequence seq1 = ValidationTestUtilities.getTestSequenceWithConsistentQuality(testString, qualityValue);
        DefaultNucleotideGraphSequence seq2 = ValidationTestUtilities.getTestSequenceWithConsistentQuality(testString, qualityValue);

        int[] qualities = new int[testString.length()];
        Arrays.fill(qualities, qualityValue*2);

        testConsensusFromSequences(testString, qualities, seq1, seq2);
    }

}
