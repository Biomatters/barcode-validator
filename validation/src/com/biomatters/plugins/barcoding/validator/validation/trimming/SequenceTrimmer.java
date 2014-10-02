package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.DefaultNucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Functionality for trimming sequences. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 9/09/14 11:03 AM
 */
public class SequenceTrimmer {
    private SequenceTrimmer() {
    }

    /**
     * Trims NucleotideGraphSequenceDocuments by removing regions off sequence ends.
     *
     * @param sequences Sequences.
     * @param errorProbabilityLimit Error probability limit.
     * @return Trimmed sequences.
     * @throws DocumentOperationException
     */
    public static List<NucleotideGraphSequenceDocument> trimSequences(List<NucleotideGraphSequenceDocument> sequences, double errorProbabilityLimit)
            throws DocumentOperationException {
        List<NucleotideGraphSequenceDocument> trimmedSequences = new ArrayList<NucleotideGraphSequenceDocument>();

        try {
            for (NucleotideGraphSequenceDocument sequence : sequences) {
                trimmedSequences.add(
                        trimNucleotideGraphSequenceDocument(sequence, ErrorProbabilityTrimmer.getTrimmage(sequence, TrimmableEnds.Both, errorProbabilityLimit))
                );
            }
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not trim documents: " + e.getMessage(), e);
        }

        return trimmedSequences;
    }

    /**
     * Trims character sequence by removing regions off ends.
     *
     * @param sequence Character sequence.
     * @param trimmage Region lengths.
     * @return Trimmed character sequence.
     */
    static SequenceCharSequence trimCharacterSequence(SequenceCharSequence sequence, Trimmage trimmage) {
        return sequence.subSequence(trimmage.trimAtStart, sequence.length() - trimmage.trimAtEnd);
    }

    /**
     * Trims array of qualities by removing regions off ends.
     *
     * @param qualities Qualities array.
     * @param trimmage Region lengths.
     * @return Trimmed qualities array.
     */
    static int[] trimQualities(int[] qualities, Trimmage trimmage) {
        return Arrays.copyOfRange(qualities, trimmage.trimAtStart, qualities.length - trimmage.trimAtEnd);
    }

    /**
     * Trims NucleotideGraphSequenceDocument by removing regions off sequence ends.
     *
     * @param sequence Sequence.
     * @param trimmage Region lengths.
     * @return Trimmed sequence.
     * @throws DocumentOperationException
     */
    private static NucleotideGraphSequenceDocument trimNucleotideGraphSequenceDocument(NucleotideGraphSequenceDocument sequence, Trimmage trimmage)
            throws DocumentOperationException {
        int[] trimmedQualities = trimQualities(getQualities(sequence), trimmage);

        NucleotideGraph graph = new DefaultNucleotideGraph(null, null, trimmedQualities, trimmedQualities.length, 0);

        try {
            return new DefaultNucleotideGraphSequence(
                    sequence.getName(), sequence.getDescription(), trimCharacterSequence(sequence.getCharSequence(), trimmage), new Date(), graph
            );
        } catch (IndexOutOfBoundsException e) {
            throw new DocumentOperationException("Could not trim '" + sequence.getName() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Extracts quality values from sequence.
     *
     * @param sequence Sequence.
     * @return Qualities.
     */
    static int[] getQualities(NucleotideGraphSequenceDocument sequence) {
        int length = sequence.getSequenceLength();

        int[] qualities = new int[length];

        for (int i = 0; i < length; i++) {
            qualities[i] = sequence.getSequenceQuality(i);
        }

        return qualities;
    }
}