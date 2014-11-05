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
 * Static methods for trimming sequences. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 9/09/14 11:03 AM
 */
public class SequenceTrimmer {

    private SequenceTrimmer() {
    }

    /**
     * Trims the supplied sequence by removing the greatest number of bases that can be removed via either modified Mott 
     * algorithm or the Smith-Waterman algorithm.
     *
     * @param sequence Sequence to trim.
     * @param errorProbabilityLimit Error probability limit for the modified Mott algorithm.
     * @param primers Primer sequences for the Smith-Waterman algorithm.
     * @param gapOpenPenalty Gap open penalty for the Smith-Waterman algorithm.
     * @param gapExtensionPenalty Gap extension penalty for the Smith-Waterman algorithm.
     * @param scores Scores matrix for the Smith-Waterman algorithm.
     * @param maxMismatches Maximum number of mismatched bases that are allowed for Smith-Waterman alignment results.
     * @param minMatchLength Minimum number of matched bases that are allowed for Smith-Waterman alignment results.
     * @return Trimmed sequence.
     */
    public static NucleotideGraphSequenceDocument trimSequenceByQualityAndPrimers(NucleotideGraphSequenceDocument sequence,
                                                                                  double errorProbabilityLimit,
                                                                                  List<OligoSequenceDocument> primers,
                                                                                  float gapOpenPenalty,
                                                                                  float gapExtensionPenalty,
                                                                                  Scores scores,
                                                                                  int maxMismatches,
                                                                                  int minMatchLength) {
        List<Trimmage> trimmages = new ArrayList<Trimmage>();

        /* Add the Trimmage that derives from running the modified Mott algorithm on the supplied sequence. */
        trimmages.add(ErrorProbabilityTrimmer.getTrimmage(sequence, TrimmableEnds.Both, errorProbabilityLimit));

        /* Add the Trimmages that derive from aligning the supplied sequence with each of the supplied primers using the
         * Smith-Waterman algorithm.
         */
        for (OligoSequenceDocument primer : primers) {
            trimmages.add(getTrimmageForPrimerTrimming(sequence, primer, gapOpenPenalty, gapExtensionPenalty, scores, maxMismatches, minMatchLength));
        }

        /* Generate the Trimmage with the maximum trimAtStart value and the maximum trimAtEnd value that is found among
         * trimmages.
         */
        Trimmage maxTrimmage = max(trimmages);

        if (maxTrimmage.trimAtStart > sequence.getSequenceLength() - maxTrimmage.trimAtEnd + 1) {
            maxTrimmage = new Trimmage(sequence.getSequenceLength(), 0);
        }

        return trimSequenceUsingTrimmage(sequence, maxTrimmage);
    }

    /**
     * Trims the supplied sequence using the supplied Trimmage.
     *
     * @param sequence Sequence to trim.
     * @param trimmage Bases to trim.
     * @return Trimmed sequence.
     */
    static NucleotideGraphSequenceDocument trimSequenceUsingTrimmage(NucleotideGraphSequenceDocument sequence, Trimmage trimmage) {
        SequenceExtractionUtilities.ExtractionOptions options = new SequenceExtractionUtilities.ExtractionOptions(trimmage.getNonTrimmedInterval(sequence.getSequenceLength()));
        options.setOverrideName(sequence.getName() + " trimmed");

        return (NucleotideGraphSequenceDocument)SequenceExtractionUtilities.extract(sequence, options);
    }

    /**
     * Returns the maximization of trimmageOne and trimmageTwo.
     * 
     * Maximization.trimAtStart = max of trimmageOne.trimAtStart and trimmageTwo.trimAtStart, and
     * Maximization.trimAtEnd = max of trimmageOne.trimAtEnd and trimmageTwo.trimAtEnd.
     *
     * @param trimmageOne
     * @param trimmageTwo
     * @return Maximization of trimmageOne and trimmageTwo.
     */
    private static Trimmage max(Trimmage trimmageOne, Trimmage trimmageTwo) {
        return new Trimmage(Math.max(trimmageOne.trimAtStart, trimmageTwo.trimAtStart), Math.max(trimmageOne.trimAtEnd, trimmageTwo.trimAtEnd));
    }

    /**
     * Returns the maximization of the supplied Trimmages.
     * 
     * Maximization.trimAtStart = max of the trimAtStart values of the supplied Trimmages.
     * Maximization.trimAtEnd = max of the trimAtEnd valuess of the supplied Trimmages.
     *
     * @param trimmages Trimmages for which the maximization is calculated.
     * @return Maximization of the supplied Trimmages.
     */
    private static Trimmage max(Collection<Trimmage> trimmages) {
        Trimmage maxTrimmage = new Trimmage(0, 0);

        for (Trimmage trimmage : trimmages) {
            maxTrimmage = max(maxTrimmage, trimmage);
        }

        return maxTrimmage;
    }

    /**
     * Builds the Trimmage for the trimming of the supplied sequence using the Smith-Waterman algorithm.
     *
     * @param sequence Sequence to trim.
     * @param primer Primer sequence for the Smith-Waterman algorithm.
     * @param gapOpenPenalty Gap open penalty for the Smith-Waterman algorithm.
     * @param gapExtensionPenalty Gap extension penalty for the Smith-Waterman algorithm.
     * @return Constructed Trimmage.
     */
    private static Trimmage getTrimmageForPrimerTrimming(NucleotideGraphSequenceDocument sequence,
                                                         OligoSequenceDocument primer,
                                                         float gapOpenPenalty,
                                                         float gapExtensionPenalty,
                                                         Scores scores,
                                                         int maxMismatches,
                                                         int minMatchLength) {
        CharSequence traceSequence = sequence.getCharSequence();
        CharSequence primerSequence = primer.getBindingSequence();
        CharSequence primerSequenceReversed = SequenceUtilities.reverseComplement(primerSequence);

        /* Add any additional characters from the supplied sequence and the supplied primer to the supplied scores matrix. */
        Scores scoresWithAdditionalCharacters = getScoresWithAdditionalCharacters(scores, Arrays.asList(SequenceUtilities.removeGaps(traceSequence), SequenceUtilities.removeGaps(primerSequence)));

        /* Align the supplied sequence and the supplied primer via the Smith-Waterman algorithm. */
        SmithWaterman forwardAlignmentResult = new SmithWaterman(new String[] { traceSequence.toString(), primerSequence.toString() },
                                                                 ProgressListener.EMPTY,
                                                                 new SmithWatermanLinearSpaceAffine(scoresWithAdditionalCharacters, gapOpenPenalty, gapExtensionPenalty));
        /* Align the supplied sequence and the supplied primer reversed via the Smith-Waterman algorithm. */
        SmithWaterman reverseAlignmentResult = new SmithWaterman(new String[] { traceSequence.toString(), primerSequenceReversed.toString() },
                                                                 ProgressListener.EMPTY,
                                                                 new SmithWatermanLinearSpaceAffine(scoresWithAdditionalCharacters, gapOpenPenalty, gapExtensionPenalty));

        int amountToTrimFromLeftEnd = getAmountToTrimForPrimerAlignment(traceSequence, forwardAlignmentResult, maxMismatches, minMatchLength, false);
        int amountToTrimFromRightEnd = getAmountToTrimForPrimerAlignment(traceSequence, reverseAlignmentResult, maxMismatches, minMatchLength, true);

        return new Trimmage(amountToTrimFromLeftEnd, amountToTrimFromRightEnd);
    }

    /**
     * Returns the amount of bases to trim off from the supplied sequence using the Smith-Waterman algorithm.
     *
     * @param sequence Sequence to trim.
     * @param primerAlignmentResult Result of running the Smith-Waterman algorithm on the supplied sequence
     * @param maxMismatches Maximum number of mismatched bases that are allowed between the supplied portion of sequence
     *                      and the supplied portion of primer.
     * @param minMatchLength Minimum number of matched bases that are allowed in an alignment.
     * @param reversed True if the primer that is associated with the trimming is of a reverse direction.
     * @return Amount of bases to remove from (the left end, if reversed==true, or the right end, if reverse==false, of)
     * the supplied sequence.
     */
    private static int getAmountToTrimForPrimerAlignment(CharSequence sequence, SmithWaterman primerAlignmentResult, int maxMismatches, int minMatchLength, boolean reversed) {
        String matchingPortionOfSequence = primerAlignmentResult.getAlignedSequences()[0];
        String matchingPortionOfPrimer = primerAlignmentResult.getAlignedSequences()[1];

        if (matchingPortionOfSequence.length() < minMatchLength || hasMoreThanMaximumNumberOfMismatches(matchingPortionOfSequence, matchingPortionOfPrimer, maxMismatches)) {
            return 0;
        }

        SequenceAnnotationInterval sequenceAlignmentInterval = primerAlignmentResult.getIntervals()[0];

        return getAmountToTrimForPrimerAlignment(sequence, sequenceAlignmentInterval, reversed);
    }

    /**
     * Returns the amount of bases to remove from the supplied sequence using the Smith-Waterman algorithm.
     *
     * @param sequence Sequence to trim.
     * @param primerAlignmentInterval Interval associated with the
     * @param reversed True if the primer that is associated with the trimming is of a reverse direction.
     * @return Amount of bases to remove from (the left end, if reversed==true, or the right end, if reverse==false, of)
     * the supplied sequence.
     */
    private static int getAmountToTrimForPrimerAlignment(CharSequence sequence, SequenceAnnotationInterval primerAlignmentInterval, boolean reversed) {
        return reversed ? sequence.length() - primerAlignmentInterval.getFrom() + 1 : primerAlignmentInterval.getTo();
    }

    /**
     * Checks if the number of mismatched bases between sequenceOne and sequenceTwo is greater than the specified 
     * threshold.
     *
     * @param sequenceOne
     * @param sequenceTwo
     * @param maxMismatches Maximum number of mismatching bases that are allowed between sequenceOne and sequenceTwo.
     * @return True if the number of mismatched bases between sequenceOne and sequenceTwo > maxMismatches.
     */
    private static boolean hasMoreThanMaximumNumberOfMismatches(String sequenceOne, String sequenceTwo, int maxMismatches) {
        if (sequenceOne.length() != sequenceTwo.length()) {
            return false;
        }

        int numOfMismatches = 0;
        for (int i = 0; i < sequenceOne.length(); i++) {
            if (sequenceOne.charAt(i) != sequenceTwo.charAt(i)) {
                maxMismatches++;
            }
        }

        return numOfMismatches <= maxMismatches;
    }

    /**
     * Adds the unique characters from the supplied sequences to the supplied scores matrix.
     *
     * @param scores Scores matrix for adding the unique characters to.
     * @param sequences Character sequences.
     * @return The supplied scores matrix containing the unique characters from the supplied sequences.
     */
    private static Scores getScoresWithAdditionalCharacters(Scores scores, List<CharSequence> sequences) {
        return Scores.includeAdditionalCharacters(scores, toString(getCharacters(sequences)));
    }

    /**
     * Returns the unique characters from the supplied sequences.
     *
     * @param sequences Character sequences.
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
     * @param sequence Character sequence.
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
     * Builds the string that contains the characters in the supplied set in the order specified by the iteration of the
     * set.
     *
     * Set['A', 'B', 'C'] -> String("ABC").
     *
     * @param characters Characters from which the string is constructed.
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