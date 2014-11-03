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
                                                                       Scores scores) {
        return trimSequenceUsingTrimmage(sequence, getTrimmageForPrimerTrimming(sequence, primer, gapOpenPenalty, gapExtensionPenalty, scores));
    }

    public static NucleotideGraphSequenceDocument trimSequenceByQualityAndPrimers(NucleotideGraphSequenceDocument sequence,
                                                                                  double errorProbabilityLimit,
                                                                                  List<OligoSequenceDocument> primers,
                                                                                  float gapOpenPenalty,
                                                                                  float gapExtensionPenalty,
                                                                                  Scores scores) {
        List<Trimmage> trimmages = new ArrayList<Trimmage>();

        trimmages.add(ErrorProbabilityTrimmer.getTrimmage(sequence, TrimmableEnds.Both, errorProbabilityLimit));

        for (OligoSequenceDocument primer : primers) {
            trimmages.add(getTrimmageForPrimerTrimming(sequence, primer, gapOpenPenalty, gapExtensionPenalty, scores));
        }

        return trimSequenceUsingTrimmage(sequence, max(trimmages));
    }

    /**
     * @param sequence {@link NucleotideGraphSequenceDocument} to trim
     * @param trimmage Region lengths to trim
     * @return Trimmed {@link com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument}
     */
    static NucleotideGraphSequenceDocument trimSequenceUsingTrimmage(NucleotideGraphSequenceDocument sequence, Trimmage trimmage) {
        SequenceExtractionUtilities.ExtractionOptions options = new SequenceExtractionUtilities.ExtractionOptions(trimmage.getNonTrimmedInterval(sequence.getSequenceLength()));
        options.setOverrideName(sequence.getName() + " trimmed");

        return (NucleotideGraphSequenceDocument)SequenceExtractionUtilities.extract(sequence, options);
    }

    private static Trimmage max(Trimmage trimmageOne, Trimmage trimmageTwo) {
        return new Trimmage(Math.max(trimmageOne.trimAtStart, trimmageTwo.trimAtStart), Math.max(trimmageTwo.trimAtEnd, trimmageTwo.trimAtEnd));
    }

    private static Trimmage max(Collection<Trimmage> trimmages) {
        Trimmage maxTrimmage = new Trimmage(0, 0);

        for (Trimmage trimmage : trimmages) {
            maxTrimmage = max(maxTrimmage, trimmage);
        }

        return maxTrimmage;
    }

    /**
     * Creates the trimmage that would be used for the trimming of the supplied sequence with the supplied primer and
     * alignment settings.
     *
     * @param sequence
     * @param primer
     * @return Created Trimmage.
     */
    private static Trimmage getTrimmageForPrimerTrimming(NucleotideGraphSequenceDocument sequence,
                                                         OligoSequenceDocument primer,
                                                         float gapOpenPenalty,
                                                         float gapExtensionPenalty,
                                                         Scores scores) {
        CharSequence traceSequence = sequence.getCharSequence();
        CharSequence primerSequence = primer.getBindingSequence();
        CharSequence primerSequenceReversed = SequenceUtilities.reverseComplement(primerSequence);

        Scores scoresWithAdditionalCharacters = getScoresWithAdditionalCharacters(scores, Arrays.asList(SequenceUtilities.removeGaps(traceSequence), SequenceUtilities.removeGaps(primerSequence)));

        SequenceAnnotationInterval leftTrimInterval = new SmithWaterman(new String[] { traceSequence.toString(), primerSequence.toString() },
                ProgressListener.EMPTY,
                new SmithWatermanLinearSpaceAffine(scoresWithAdditionalCharacters, gapOpenPenalty, gapExtensionPenalty)).getIntervals()[0];
        SequenceAnnotationInterval rightTrimInterval = new SmithWaterman(new String[] { traceSequence.toString(), primerSequenceReversed.toString() },
                ProgressListener.EMPTY,
                new SmithWatermanLinearSpaceAffine(scoresWithAdditionalCharacters, gapOpenPenalty, gapExtensionPenalty)).getIntervals()[0];

        return new Trimmage(leftTrimInterval.getTo(), sequence.getSequenceLength() - rightTrimInterval.getFrom() + 1);
    }

    private static Scores getScoresWithAdditionalCharacters(Scores scores, List<CharSequence> sequences) {
        return Scores.includeAdditionalCharacters(scores, toString(getCharacters(sequences)));
    }

    /**
     * Extracts the unique characters from the supplied sequences.
     *
     * @param sequences
     * @return Generated set.
     */
    private static Set<Character> getCharacters(List<CharSequence> sequences) {
        Set<Character> characters = new HashSet<Character>();

        for (CharSequence sequence : sequences) {
            characters.addAll(getCharacters(sequence));
        }

        return characters;
    }

    /**
     * Extracts the unique characters from the supplied sequence.
     *
     * @param sequence
     * @return Unique characters.
     */
    private static Set<Character> getCharacters(CharSequence sequence) {
        Set<Character> characters = new HashSet<Character>();

        for (int i = 0; i < sequence.length(); i++) {
            characters.add(sequence.charAt(i));
        }

        return characters;
    }

    /**
     * Builds a string from the supplied set of characters.
     *
     * Set['A', 'B', 'C'] -> String("ABC").
     *
     * @param characters
     * @return Built string.
     */
    private static String toString(Set<Character> characters) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Character c : characters) {
            stringBuilder.append(c);
        }

        return stringBuilder.toString();
    }
}