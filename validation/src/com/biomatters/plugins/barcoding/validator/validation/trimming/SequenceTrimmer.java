package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.implementations.SequenceExtractionUtilities;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.OligoSequenceDocument;
import com.biomatters.geneious.publicapi.utilities.SequenceUtilities;
import jebl.evolution.align.SmithWatermanLinearSpaceAffine;
import jebl.evolution.align.scores.Scores;
import jebl.util.ProgressListener;

import java.util.*;

/**
 * Static methods for trimming sequences. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 9/09/14 11:03 AM
 */
public class SequenceTrimmer {

    private SequenceTrimmer() {
    }

    /**
     * Trims the supplied sequence by removing the greatest number of bases that can be removed from each of its ends by
     * using either the Smith-Waterman algorithm with one of the supplied primers or by using the modified Mott
     * algorithm.
     *
     * @param sequence Sequence to trim.
     * @param errorProbabilityLimit Error probability limit for the modified Mott algorithm.
     * @param primers Primer sequences for the Smith-Waterman algorithm.
     * @param gapOpenPenalty Gap open penalty for the Smith-Waterman algorithm.
     * @param gapExtensionPenalty Gap extension penalty for the Smith-Waterman algorithm.
     * @param scores Scores matrix for the Smith-Waterman algorithm.
     * @return Trimmed sequence.
     */
    public static NucleotideGraphSequenceDocument trimSequenceByQualityAndPrimers(NucleotideGraphSequenceDocument sequence,
                                                                                  double errorProbabilityLimit,
                                                                                  List<OligoSequenceDocument> primers,
                                                                                  float gapOpenPenalty,
                                                                                  float gapExtensionPenalty,
                                                                                  Scores scores,
                                                                                  boolean addAnnotion) {
        List<Trimmage> trimmages = new ArrayList<Trimmage>();

        /* Add the trim regions that derive from running the modified Mott algorithm on the supplied sequence. */
        trimmages.add(ErrorProbabilityTrimmer.getTrimmage(sequence, TrimmableEnds.Both, errorProbabilityLimit));

        /* Add the trim regions that derive from running the Smith-Waterman algorithm with each of the supplied primers
         * on the supplied sequence.
         */
        for (OligoSequenceDocument primer : primers) {
            trimmages.add(getTrimmageForPrimerTrimming(sequence, primer, gapOpenPenalty, gapExtensionPenalty, scores));
        }

        /* Generate the trimmage that when used to trim the supplied sequence, removes the maximum number of bases that
         * can be removed from each of its ends.
         */
        Trimmage maxTrimmage = max(trimmages);

        if (maxTrimmage.trimAtStart > sequence.getSequenceLength() - maxTrimmage.trimAtEnd + 1) {
            maxTrimmage = new Trimmage(sequence.getSequenceLength(), 0);
        }

        if (addAnnotion && sequence instanceof DefaultSequenceDocument) {
            SequenceAnnotation annotation = SequenceAnnotation.createTrimAnnotation(1, maxTrimmage.trimAtStart);
            annotation.addInterval(sequence.getSequenceLength() - maxTrimmage.trimAtEnd + 1, sequence.getSequenceLength());
            ((DefaultSequenceDocument)sequence).addSequenceAnnotation(annotation);
            return sequence;
        } else {
            /* Trim the sequence using the generated trimmage and return the result. */
            return trimSequenceUsingTrimmage(sequence, maxTrimmage);
        }
    }

    /**
     * Trims the supplied sequence using the supplied trimmage.
     *
     * @param sequence Sequence to trim.
     * @param trimmage The trimmage containing the number of bases to remove from each of the ends of the sequence.
     * @return Trimmed sequence.
     */
    static NucleotideGraphSequenceDocument trimSequenceUsingTrimmage(NucleotideGraphSequenceDocument sequence, Trimmage trimmage) {
        SequenceExtractionUtilities.ExtractionOptions options = new SequenceExtractionUtilities.ExtractionOptions(trimmage.getNonTrimmedInterval(sequence.getSequenceLength()));
        options.setOverrideName(sequence.getName() + " trimmed");

        return (NucleotideGraphSequenceDocument)SequenceExtractionUtilities.extract(sequence, options);
    }

    /**
     * Constructs a trimmage where its trimAtStart field value and its trimAtEnd field value are equal to the maximum of
     * trimmageOne's trimAtStart field value and trimmageTwo's trimAtStart field value and the maximum of trimmageOne's
     * trimAtEnd field value and trimmageTwo's trimAtEnd field value respectively.
     *
     * @param trimmageOne
     * @param trimmageTwo
     * @return Constructed trimmage.
     */
    private static Trimmage max(Trimmage trimmageOne, Trimmage trimmageTwo) {
        return new Trimmage(Math.max(trimmageOne.trimAtStart, trimmageTwo.trimAtStart), Math.max(trimmageOne.trimAtEnd, trimmageTwo.trimAtEnd));
    }

    /**
     * Constructs a trimmage where its trimAtStart and trimAtEnd field values are equal to the maximum of the
     * trimAtStart field values of the supplied trimmages and the maximum of the trimAtEnd field values of the supplied
     * trimmages respectively.
     *
     *
     * @param trimmages
     * @return Constructed trimmage.
     */
    private static Trimmage max(Collection<Trimmage> trimmages) {
        Trimmage maxTrimmage = new Trimmage(0, 0);

        for (Trimmage trimmage : trimmages) {
            maxTrimmage = max(maxTrimmage, trimmage);
        }

        return maxTrimmage;
    }

    /**
     * Constructs the trimmage for the trimming of the supplied sequence using the Smith-Waterman algorithm.
     *
     * @param sequence Sequence to trim.
     * @param primer Primer sequence for the Smith-Waterman algorithm.
     * @param gapOpenPenalty Gap open penalty for the Smith-Waterman algorithm.
     * @param gapExtensionPenalty Gap extension penalty for the Smith-Waterman algorithm.
     * @return Constructed trimmage.
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

    /**
     * Includes the unique characters from the supplied sequences into the supplied scores matrix.
     *
     * @param scores
     * @param sequences
     * @return The supplied scores matrix with the unique characters from the supplied sequences included.
     */
    private static Scores getScoresWithAdditionalCharacters(Scores scores, List<CharSequence> sequences) {
        return Scores.includeAdditionalCharacters(scores, toString(getCharacters(sequences)));
    }

    /**
     * Returns the unique characters from the supplied sequences.
     *
     * @param sequences
     * @return Unique characters from the supplied sequences.
     */
    private static Set<Character> getCharacters(List<CharSequence> sequences) {
        Set<Character> characters = new HashSet<Character>();

        for (CharSequence sequence : sequences) {
            characters.addAll(getCharacters(sequence));
        }

        return characters;
    }

    /**
     * Returns the unique characters from the supplied sequence.
     *
     * @param sequence
     * @return Unique characters from the supplied sequence.
     */
    private static Set<Character> getCharacters(CharSequence sequence) {
        Set<Character> characters = new HashSet<Character>();

        for (int i = 0; i < sequence.length(); i++) {
            characters.add(sequence.charAt(i));
        }

        return characters;
    }

    /**
     * Constructs the string that contains each of the characters from the supplied set in the order specified by the
     * iteration of the set.
     *
     * When supplied Set['A', 'B', 'C'], String("ABC") is returned.
     *
     * @param characters
     * @return Constructed string.
     */
    private static String toString(Set<Character> characters) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Character c : characters) {
            stringBuilder.append(c);
        }

        return stringBuilder.toString();
    }
}