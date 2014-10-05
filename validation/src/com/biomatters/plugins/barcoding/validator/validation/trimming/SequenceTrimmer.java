package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.SequenceExtractionUtilities;

import java.util.ArrayList;
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
     * Trims NucleotideGraphSequenceDocuments by quality scores using the modified Mott algorithm.  This is the same
     * algorithm used in PHRED and is also one of the trimming algorithms available in Geneious.
     *
     * @param documents {@link NucleotideGraphSequenceDocument}s to trim.
     * @param errorProbabilityLimit Error probability limit for the modified mott algorithm.
     * @return Trimmed {@link com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument}s.
     */
    public static List<NucleotideGraphSequenceDocument> trimSequences(List<NucleotideGraphSequenceDocument> documents, double errorProbabilityLimit) {
        List<NucleotideGraphSequenceDocument> trimmedSequences = new ArrayList<NucleotideGraphSequenceDocument>();
        for (NucleotideGraphSequenceDocument sequence : documents) {
            trimmedSequences.add(trimSequence(sequence, ErrorProbabilityTrimmer.getTrimmage(sequence, TrimmableEnds.Both, errorProbabilityLimit)));
        }
        return trimmedSequences;
    }

    /**
     * @param document {@link NucleotideGraphSequenceDocument} to trim
     * @param trimmage Region lengths to trim
     * @return Trimmed {@link com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument}
     */
    static NucleotideGraphSequenceDocument trimSequence(NucleotideGraphSequenceDocument document, Trimmage trimmage) {
        SequenceExtractionUtilities.ExtractionOptions options = new SequenceExtractionUtilities.ExtractionOptions(
                trimmage.getNonTrimmedInterval(document.getSequenceLength()));
        options.setOverrideName(document.getName() + " trimmed");

        return (NucleotideGraphSequenceDocument)SequenceExtractionUtilities.extract(document, options);
    }
}