package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.SequenceExtractionUtilities;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.SequenceUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Functionality for trimming sequences. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 9/09/14 11:03 AM
 */
public class SequenceTrimmer {
    private static final int CHROMATOGRAM_NUCLEOTIDE_STATE_NUMBER_RANGE_SIZE = 4;

    private SequenceTrimmer() {
    }

    /**
     * Trims NucleotideGraphSequenceDocuments by removing regions off sequence ends.
     *
     * @param documents Supplied NucleotideGraphSequenceDocuments.
     * @param errorProbabilityLimit Error probability limit.
     * @return Trimmed NucleotideGraphSequenceDocuments.
     * @throws DocumentOperationException
     */
    public static List<NucleotideGraphSequenceDocument> trimSequences(List<NucleotideGraphSequenceDocument> documents, double errorProbabilityLimit)
            throws DocumentOperationException {
        List<NucleotideGraphSequenceDocument> trimmedSequences = new ArrayList<NucleotideGraphSequenceDocument>();

        try {
            for (NucleotideGraphSequenceDocument sequence : documents) {
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
     * Trims NucleotideGraphSequenceDocument by removing regions off sequence ends.
     *
     * @param document Supplied NucleotideGraphSequenceDocument.
     * @param trimmage Region lengths.
     * @return Trimmed sequence.
     * @throws DocumentOperationException
     */
    private static NucleotideGraphSequenceDocument trimNucleotideGraphSequenceDocument(NucleotideGraphSequenceDocument document, Trimmage trimmage)
            throws DocumentOperationException {
        int from = Math.max(0, trimmage.trimAtStart - 1);
        int to = Math.max(0, document.getSequenceLength() - trimmage.trimAtEnd);

        SequenceExtractionUtilities.ExtractionOptions options = new SequenceExtractionUtilities.ExtractionOptions(from, to);

        return (NucleotideGraphSequenceDocument)SequenceExtractionUtilities.extract(document, options);
    }
}