package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.implementations.SequenceExtractionUtilities;
import com.biomatters.geneious.publicapi.implementations.sequence.OligoSequenceDocument;
import com.biomatters.geneious.publicapi.utilities.SequenceUtilities;
import jebl.evolution.align.SmithWatermanLinearSpaceAffine;
import jebl.evolution.align.scores.Scores;
import jebl.util.ProgressListener;

import java.util.*;

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
     * Trims sequence by quality scores using the modified Mott algorithm.  This is the same
     * algorithm used in PHRED and is also one of the trimming algorithms available in Geneious.
     *
     * @param sequence Sequence to trim.
     * @param errorProbabilityLimit Error probability limit for the modified mott algorithm.
     * @return Trimmed sequence.
     */
    public static NucleotideGraphSequenceDocument trimSequenceByQuality(NucleotideGraphSequenceDocument sequence, double errorProbabilityLimit) {
        return trimSequenceUsingTrimmage(sequence, ErrorProbabilityTrimmer.getTrimmage(sequence, TrimmableEnds.Both, errorProbabilityLimit));
    }

    public static NucleotideGraphSequenceDocument trimSequenceByPrimer(NucleotideGraphSequenceDocument sequence,
                                                                       OligoSequenceDocument primer,
                                                                       float gapOpenPenalty,
                                                                       float gapExtensionPenalty,
                                                                       Scores scores,
                                                                       ProgressListener progressListener) {
        return trimSequenceUsingTrimmage(sequence, getTrimmageForPrimerTrimming(sequence, primer, gapOpenPenalty, gapExtensionPenalty, scores, progressListener));
    }

    public static NucleotideGraphSequenceDocument trimSequenceByQualityAndPrimer(NucleotideGraphSequenceDocument sequence,
                                                                                 double errorProbabilityLimit,
                                                                                 OligoSequenceDocument primer,
                                                                                 float gapOpenPenalty,
                                                                                 float gapExtensionPenalty,
                                                                                 Scores scores,
                                                                                 ProgressListener progressListener) {
        return trimSequenceUsingTrimmage(sequence,
                                         max(ErrorProbabilityTrimmer.getTrimmage(sequence, TrimmableEnds.Both, errorProbabilityLimit),
                                         getTrimmageForPrimerTrimming(sequence, primer, gapOpenPenalty, gapExtensionPenalty, scores, progressListener)));
    }

    /**
     * @param sequence {@link NucleotideGraphSequenceDocument} to trim
     * @param trimmage Region lengths to trim
     * @return Trimmed {@link com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument}
     */
    static NucleotideGraphSequenceDocument trimSequenceUsingTrimmage(NucleotideGraphSequenceDocument sequence, Trimmage trimmage) {
        SequenceExtractionUtilities.ExtractionOptions options = new SequenceExtractionUtilities.ExtractionOptions(
                trimmage.getNonTrimmedInterval(sequence.getSequenceLength()));
        options.setOverrideName(sequence.getName() + " trimmed");

        return (NucleotideGraphSequenceDocument)SequenceExtractionUtilities.extract(sequence, options);
    }

    private static Trimmage max(Trimmage trimmageOne, Trimmage trimmageTwo) {
        return new Trimmage(Math.max(trimmageOne.trimAtStart, trimmageTwo.trimAtStart), Math.max(trimmageTwo.trimAtEnd, trimmageTwo.trimAtEnd));
    }

    /**
     * Returns trimmage used for primer trimming.
     *
     * @param sequence Sequence.
     * @param primer Primer.
     * @return Trimmage.
     */
    private static Trimmage getTrimmageForPrimerTrimming(NucleotideGraphSequenceDocument sequence,
                                                         OligoSequenceDocument primer,
                                                         float gapOpenPenalty,
                                                         float gapExtensionPenalty,
                                                         Scores scores,
                                                         ProgressListener progressListener) {
        CharSequence traceSequence = sequence.getCharSequence();
        CharSequence primerSequence = primer.getBindingSequence();
        CharSequence primerSequenceReversed = SequenceUtilities.reverseComplement(primerSequence);

        Scores scoresWithAdditionalCharacters = getScoresWithAdditionalCharacters(scores, Arrays.asList(SequenceUtilities.removeGaps(traceSequence), SequenceUtilities.removeGaps(primerSequence)));

        SequenceAnnotationInterval leftTrimInterval = new SmithWaterman(new String[] { traceSequence.toString(), primerSequence.toString() },
                                                                        progressListener,
                                                                        new SmithWatermanLinearSpaceAffine(scoresWithAdditionalCharacters, gapOpenPenalty, gapExtensionPenalty)).getIntervals()[0];
        SequenceAnnotationInterval rightTrimInterval = new SmithWaterman(new String[] { traceSequence.toString(), primerSequenceReversed.toString() },
                                                                         progressListener,
                                                                         new SmithWatermanLinearSpaceAffine(scoresWithAdditionalCharacters, gapOpenPenalty, gapExtensionPenalty)).getIntervals()[0];

        return new Trimmage(leftTrimInterval.getTo(), sequence.getSequenceLength() - rightTrimInterval.getFrom() + 1);
    }

    private static Scores getScoresWithAdditionalCharacters(Scores scores, List<CharSequence> sequences) {
        return Scores.includeAdditionalCharacters(scores, toString(getCharacters(sequences)));
    }

    private static Set<Character> getCharacters(List<CharSequence> sequences) {
        Set<Character> characters = new HashSet<Character>();

        for (CharSequence sequence : sequences) {
            characters.addAll(getCharacters(sequence));
        }

        return characters;
    }

    private static Set<Character> getCharacters(CharSequence sequence) {
        Set<Character> characters = new HashSet<Character>();

        for (int i = 0; i < sequence.length(); i++) {
            characters.add(sequence.charAt(i));
        }

        return characters;
    }

    private static String toString(Set<Character> characters) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Character c : characters) {
            stringBuilder.append(c);
        }

        return stringBuilder.toString();
    }
}