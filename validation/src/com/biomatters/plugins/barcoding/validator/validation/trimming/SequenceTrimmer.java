package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Functionality for trimming NucleotideSequenceDocuments. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 9/09/14 11:03 AM
 */
public class SequenceTrimmer {
    private SequenceTrimmer() {
    }

    /**
     * Trims DefaultNucleotideGraphSequences: Removes regions from their ends.
     *
     * @param sequences Sequences.
     * @param errorProbabilityLimit Error probability limit.
     * @return Trimmed sequences.
     * @throws DocumentOperationException
     */
    public static List<DefaultNucleotideGraphSequence> trimSequences(List<DefaultNucleotideGraphSequence> sequences,
                                                                    double errorProbabilityLimit)
            throws DocumentOperationException {
        List<DefaultNucleotideGraphSequence> trimmedSequences = new ArrayList<DefaultNucleotideGraphSequence>();

        try {
            for (DefaultNucleotideGraphSequence sequence : sequences) {
                Trimmage trimmage = ErrorProbabilityTrimmer.getTrimmage(sequence,
                                                                        TrimmableEnds.Both,
                                                                        errorProbabilityLimit);

                trimmedSequences.add(trimNucleotideSequenceDocument(sequence, trimmage));
            }
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not trim documents: " + e.getMessage(), e);
        }

        return trimmedSequences;
    }

    /**
     * Trims a DefaultNucleotideGraphSequence: Removes regions from its ends.
     *
     * @param sequence Sequence.
     * @param trimmage Region lengths.
     * @return Trimmed sequence.
     * @throws DocumentOperationException
     */
    private static DefaultNucleotideGraphSequence
    trimNucleotideSequenceDocument(DefaultNucleotideGraphSequence sequence, Trimmage trimmage)
            throws DocumentOperationException {
        try {
            return null;
        } catch (IndexOutOfBoundsException e) {
            throw new DocumentOperationException("Could not trim '" + sequence.getName() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Removes regions from a character sequence's ends.
     *
     * @param sequence Character sequence.
     * @param trimmage Regions.
     * @return Trimmed character sequence.
     */
    public static SequenceCharSequence trimCharacterSequence(SequenceCharSequence sequence, Trimmage trimmage) {
        return sequence.subSequence(trimmage.trimAtStart, sequence.length() - trimmage.trimAtEnd);
    }
}