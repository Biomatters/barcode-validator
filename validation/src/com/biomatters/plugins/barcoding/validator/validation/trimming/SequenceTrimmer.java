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
     * Trims array of chromatogram positions by removing regions off ends.
     *
     * @param positions Supplied chromatogram positions.
     * @param trimmage Region lengths.
     * @return Trimmed chromatogram positions.
     */
    static int[] trimChromatogramPositionsForResidues(int[] positions, Trimmage trimmage) {
        return trimIntArray(positions, trimmage);
    }

    /**
     * Trims array of qualities by removing regions off ends.
     *
     * @param qualities Supplied qualities array.
     * @param trimmage Region lengths.
     * @return Trimmed qualities array.
     */
    static int[] trimQualities(int[] qualities, Trimmage trimmage) {
        return trimIntArray(qualities, trimmage);
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
        return new DefaultNucleotideGraphSequence(document.getName(),
                                                  document.getDescription(),
                                                  trimCharacterSequence(document.getCharSequence(), trimmage),
                                                  new Date(),
                                                  getTrimmedNucleotideGraph(document, trimmage));
    }

    /**
     * Creates NucleotideGraph corresponding to NucleotideGraphSequenceDocument trimmed.
     * NucleotideGraphSequenceDocuments are trimmed via removal of regions off sequence ends.
     *
     * @param document Supplied NucleotideGraphSequenceDocument.
     * @param trimmage Region lengths.
     * @return Trimmed NucleotideGraph.
     * @throws DocumentOperationException
     */
    private static NucleotideGraph getTrimmedNucleotideGraph(NucleotideGraphSequenceDocument document, Trimmage trimmage) throws DocumentOperationException {
        int[][] chromatograms = getChromatograms(document);
        int[] trimmedChromatogramPositions = trimChromatogramPositionsForResidues(getChromatogramPositionsForResidues(document), trimmage);
        int[] trimmedQualities = trimQualities(getQualities(document), trimmage);

        return DefaultNucleotideGraph.createNucleotideGraph(chromatograms,
                trimmedChromatogramPositions,
                trimmedQualities,
                trimmedChromatogramPositions.length,
                chromatograms[0].length);
    }

    /**
     * Gets chromatograms from NucleotideGraphSequenceDocument.
     *
     * @param document Supplied NucleotideGraphSequenceDocument
     * @return Chromatograms.
     */
    private static int[][] getChromatograms(NucleotideGraphSequenceDocument document) throws DocumentOperationException {
        int length = document.getChromatogramLength();

        int[][] chromatograms = new int[CHROMATOGRAM_NUCLEOTIDE_STATE_NUMBER_RANGE_SIZE][length];

        for (int i = 0; i < length ; i++) {
            for (int j = 0; j < CHROMATOGRAM_NUCLEOTIDE_STATE_NUMBER_RANGE_SIZE; j++) {
                try {
                    chromatograms[j][i] = document.getChromatogramValue(j, i);
                } catch (UnsupportedOperationException e) {
                    throw new DocumentOperationException("Could not get chromatograms for document '" + document.getName() + "'.\n\n" + e.getMessage(), e);
                }
            }
        }

        return chromatograms;
    }

    /**
     * Gets chromatogram positions from NucleotideGraphSequenceDocument.
     *
     * @param document Supplied NucleotideGraphSequenceDocument.
     * @return Chromatogram positions.
     */
    private static int[] getChromatogramPositionsForResidues(NucleotideGraphSequenceDocument document) throws DocumentOperationException {
        int length = document.getSequenceLength();

        int[] positions = new int[length];

        for (int i = 0; i < length; i++) {
            try {
                positions[i] = document.getChromatogramPositionForResidue(i);
            } catch (UnsupportedOperationException e) {
                throw new DocumentOperationException("Could not get chromatogram positions for document '" + document.getName() + "'.\n\n" + e.getMessage(), e);
            }
        }

        return positions;
    }

    /**
     * Gets qualities from NucleotideGraphSequenceDocument.
     *
     * @param document Supplied NucleotideGraphSequenceDocument.
     * @return Qualities.
     */
    private static int[] getQualities(NucleotideGraphSequenceDocument document) throws DocumentOperationException {
        int length = document.getSequenceLength();

        int[] qualities = new int[length];

        for (int i = 0; i < length; i++) {
            try {
                qualities[i] = document.getSequenceQuality(i);
            } catch (UnsupportedOperationException e) {
                throw new DocumentOperationException("Could not get qualities for document '" + document.getName() + ".\n\n" + e.getMessage(), e);
            }
        }

        return qualities;
    }

    /**
     * Trims integer array by removing regions off ends.
     *
     * @param ia Integer array.
     * @param trimmage Region lengths.
     * @return Trimmed integer array.
     */
    private static int[] trimIntArray(int[] ia, Trimmage trimmage) {
        return Arrays.copyOfRange(ia, trimmage.trimAtStart, ia.length - trimmage.trimAtEnd);
    }
}