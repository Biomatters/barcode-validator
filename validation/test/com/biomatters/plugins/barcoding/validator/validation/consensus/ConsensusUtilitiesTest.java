package com.biomatters.plugins.barcoding.validator.validation.consensus;

import com.biomatters.geneious.publicapi.documents.sequence.DefaultNucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.DefaultAlignmentDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.CharSequenceUtilities;
import jebl.evolution.sequences.Nucleotides;
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
            testSimpleConsensus(getRandomString(i));
        }
    }

    @Test
    public void usesHighestQualityValue() throws DocumentOperationException {
        DefaultNucleotideGraphSequence highQualitySeq = getTestSequenceWithConsistentQuality(getRandomString(50), 100);
        DefaultNucleotideGraphSequence lowQualitySeq = getTestSequenceWithConsistentQuality(getRandomString(50), 20);
        testConsensusFromSequences(highQualitySeq.getSequenceString(), highQualitySeq, lowQualitySeq);

        NucleotideGraphSequenceDocument highLowHigh = concatenate(highQualitySeq, lowQualitySeq, highQualitySeq);
        NucleotideGraphSequenceDocument lowHighHigh = concatenate(lowQualitySeq, highQualitySeq, highQualitySeq);
        testConsensusFromSequences(
                CharSequenceUtilities.repeatedCharSequence(highQualitySeq.getCharSequence(), 3).toString(),
                highLowHigh, lowHighHigh);
    }

    private static String getRandomString(int length) {
        StringBuilder seqBuilder = new StringBuilder(length);
        for (int j = 0; j < length; j++) {
            seqBuilder.append(Nucleotides.getCanonicalStates().get((int)(Math.random()*4)));
        }
        return seqBuilder.toString();
    }

    private static void testConsensusFromSequences(String expectedConsensus, NucleotideGraphSequenceDocument highQualitySeq, NucleotideGraphSequenceDocument lowQualitySeq) throws DocumentOperationException {
        DefaultAlignmentDocument alignment = new DefaultAlignmentDocument("test", highQualitySeq, lowQualitySeq);
        NucleotideGraphSequenceDocument consensus = ConsensusUtilities.getConsensus(alignment);
        assertEquals(expectedConsensus, consensus.getSequenceString());
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
        DefaultNucleotideGraphSequence seq1 = getTestSequenceWithConsistentQuality(testString, 50);
        DefaultNucleotideGraphSequence seq2 = getTestSequenceWithConsistentQuality(testString, 50);

        testConsensusFromSequences(testString, seq1, seq2);
    }

    private static DefaultNucleotideGraphSequence getTestSequenceWithConsistentQuality(String charSequence, int qualityValue) {
        int[] qualities = new int[charSequence.length()];
        Arrays.fill(qualities, qualityValue);
        return getTestSequence(charSequence, qualities);
    }

    private static DefaultNucleotideGraphSequence getTestSequence(String charSequence, int[] qualities) {
        return new DefaultNucleotideGraphSequence(UUID.randomUUID().toString(), null, charSequence, new Date(),
                new DefaultNucleotideGraph(null, null, qualities, charSequence.length(), 0));
    }
}
