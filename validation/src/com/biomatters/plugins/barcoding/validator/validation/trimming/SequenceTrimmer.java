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
     * Trims NucleotideGraphSequenceDocuments by removing regions off sequence ends.
     *
     * @param documents Supplied NucleotideGraphSequenceDocuments.
     * @param errorProbabilityLimit Error probability limit.
     * @return Trimmed NucleotideGraphSequenceDocuments.
     */
    public static List<NucleotideGraphSequenceDocument> trimSequences(List<NucleotideGraphSequenceDocument> documents, double errorProbabilityLimit) {
        List<NucleotideGraphSequenceDocument> trimmedSequences = new ArrayList<NucleotideGraphSequenceDocument>();
        for (NucleotideGraphSequenceDocument sequence : documents) {
            trimmedSequences.add(
                    trimNucleotideGraphSequenceDocument(sequence, ErrorProbabilityTrimmer.getTrimmage(sequence, TrimmableEnds.Both, errorProbabilityLimit))
            );
        }
        return trimmedSequences;
    }

    /**
     * Trims NucleotideGraphSequenceDocument by removing regions off sequence ends.
     *
     * @param document Supplied NucleotideGraphSequenceDocument.
     * @param trimmage Region lengths.
     * @return Trimmed sequence.
     */
    static NucleotideGraphSequenceDocument trimNucleotideGraphSequenceDocument(NucleotideGraphSequenceDocument document, Trimmage trimmage) {
        int from = Math.max(0, trimmage.trimAtStart - 1);
        int to = Math.max(0, document.getSequenceLength() - trimmage.trimAtEnd);

        SequenceExtractionUtilities.ExtractionOptions options = new SequenceExtractionUtilities.ExtractionOptions(from, to);

        return (NucleotideGraphSequenceDocument)SequenceExtractionUtilities.extract(document, options);
    }
}