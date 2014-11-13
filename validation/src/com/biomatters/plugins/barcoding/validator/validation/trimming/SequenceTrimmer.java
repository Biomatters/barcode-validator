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
    private static final int SMITH_WATERMAN_SEQUENCE_INDEX = 0;
    private static final int SMITH_WATERMAN_PRIMER_INDEX   = 1;

    public static final String ANNOTATION_SUFFIX = "trims annotated";
    public static final String TRIMMED_SUFFIX = "trimmed";

    private SequenceTrimmer() {
    }

    /**
     * Trims the supplied sequence by removing or annotating the greatest number of bases that can be removed via the
     * modified Mott algorithm and the Smith-Waterman algorithm.
     *
     * @param sequence Sequence to trim.
     * @param errorProbabilityLimit Error probability limit for the modified Mott algorithm.
     * @param primers Primer sequences for the Smith-Waterman algorithm.
     * @param gapOpenPenalty Gap open penalty for the Smith-Waterman algorithm.
     * @param gapExtensionPenalty Gap extension penalty for the Smith-Waterman algorithm.
     * @param scores Scores matrix for the Smith-Waterman algorithm.
     * @param addAnnotations If true, trim regions are annotated. If false, trim regions are removed.
     * @param maxMismatches Maximum number of mismatched bases that are allowed for the Smith-Waterman alignment
     *                      results.
     * @param minMatchLength Minimum number of matched bases that are allowed for the Smith-Waterman alignment results.
     * @return Trimmed sequence.
     */
    public static NucleotideGraphSequenceDocument trimSequenceByQualityAndPrimers(NucleotideGraphSequenceDocument sequence,
                                                                                  double errorProbabilityLimit,
                                                                                  List<OligoSequenceDocument> primers,
                                                                                  float gapOpenPenalty,
                                                                                  float gapExtensionPenalty,
                                                                                  Scores scores,
                                                                                  int maxMismatches,
                                                                                  int minMatchLength,
                                                                                  boolean addAnnotations) {
        List<Trimmage> trimmages = new ArrayList<Trimmage>();

        /* Get the Trimmage that derives from running the modified Mott algorithm on the supplied sequence. */
        trimmages.add(ErrorProbabilityTrimmer.getTrimmage(sequence, TrimmableEnds.Both, errorProbabilityLimit));

        /* Get the Trimmages that derive from aligning the supplied sequence with each of the supplied primers using the
         * Smith-Waterman algorithm.
         */
        for (OligoSequenceDocument primer : primers) {
            trimmages.add(getTrimmageForPrimerTrimming(sequence, primer, gapOpenPenalty, gapExtensionPenalty, scores, maxMismatches, minMatchLength));
        }

        /* Calculate the maximization of the Trimmages. */
        Trimmage maxTrimmage = max(trimmages);

        if (maxTrimmage.trimAtStart >= sequence.getSequenceLength() - maxTrimmage.trimAtEnd + 1) {
            maxTrimmage = new Trimmage(sequence.getSequenceLength(), 0);
        }

        if (addAnnotations && sequence instanceof DefaultSequenceDocument) {
            SequenceAnnotation annotation;
            if (maxTrimmage.trimAtStart > 0) {
                annotation = SequenceAnnotation.createTrimAnnotation(1, maxTrimmage.trimAtStart);
                ((DefaultSequenceDocument)sequence).addSequenceAnnotation(annotation);
            }

            if (maxTrimmage.trimAtEnd > 0) {
                annotation = SequenceAnnotation.createTrimAnnotation(sequence.getSequenceLength() - maxTrimmage.trimAtEnd + 1, sequence.getSequenceLength());
                ((DefaultSequenceDocument)sequence).addSequenceAnnotation(annotation);
            }
            return sequence;
        } else {
            /* Trim the sequence using the generated trimmage and return the result. */
            return trimSequenceUsingTrimmage(sequence, maxTrimmage);
        }
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
        options.setOverrideName(sequence.getName() + " " + TRIMMED_SUFFIX);

        return (NucleotideGraphSequenceDocument)SequenceExtractionUtilities.extract(sequence, options);
    }

    /**
     * Trims the supplied sequence using Trimmed annotations that are found on the supplied sequence.
     *
     * @param sequence Sequence to trim.
     * @return Trimmed sequence.
     */
    public static NucleotideGraphSequenceDocument trimSequenceUsingAnnotations(NucleotideGraphSequenceDocument sequence) {
        int start = 1;
        int end = sequence.getCharSequence().length();

        for (SequenceAnnotation annotation : sequence.getSequenceAnnotations()) {
            if (SequenceAnnotation.TYPE_TRIMMED.equals(annotation.getType())) {
                for (SequenceAnnotationInterval interval : annotation.getIntervals()) {
                    if (interval.getFrom() == 1) {
                        start = start < interval.getTo() ? interval.getTo() : start;
                    } else if (interval.getTo() == sequence.getCharSequence().length()) {
                        end = end > interval.getFrom() ? interval.getFrom() : end;
                    }
                }
            }
        }

        SequenceExtractionUtilities.ExtractionOptions options = new SequenceExtractionUtilities.ExtractionOptions(start + 1, end - 1);

        options.setOverrideName(sequence.getName());



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
     * Maximization.trimAtEnd = max of the trimAtEnd values of the supplied Trimmages.
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
     * @param maxMismatches Maximum number of mismatched bases that are allowed for the Smith-Waterman alignments.
     * @param minMatchLength Minimum number of matched bases that are allowed for the Smith-Waterman alignments.
     * @return Constructed Trimmage.
     */
    private static Trimmage getTrimmageForPrimerTrimming(NucleotideGraphSequenceDocument sequence,
                                                         OligoSequenceDocument primer,
                                                         float gapOpenPenalty,
                                                         float gapExtensionPenalty,
                                                         Scores scores,
                                                         int maxMismatches,
                                                         int minMatchLength) {
        CharSequence sequenceCharSequence = sequence.getCharSequence();
        CharSequence primerSequence = primer.getBindingSequence();
        CharSequence primerSequenceReversed = SequenceUtilities.reverseComplement(primerSequence);

        /* Add any additional characters from the supplied sequence and the supplied primer to the supplied scores matrix. */
        Scores scoresWithAdditionalCharacters = getScoresWithAdditionalCharacters(scores, Arrays.asList(SequenceUtilities.removeGaps(sequenceCharSequence), SequenceUtilities.removeGaps(primerSequence)));

        /* Align the supplied sequence and the supplied primer via the Smith-Waterman algorithm. */
        SmithWaterman primerAlignmentResult = new SmithWaterman(
                new String[] { sequenceCharSequence.toString(), primerSequence.toString() },
                ProgressListener.EMPTY,
                new SmithWatermanLinearSpaceAffine(scoresWithAdditionalCharacters, gapOpenPenalty, gapExtensionPenalty)
        );
        /* Align the supplied sequence and the supplied primer reversed via the Smith-Waterman algorithm. */
        SmithWaterman reversePrimerAlignmentResult = new SmithWaterman(
                new String[] { sequenceCharSequence.toString(), primerSequenceReversed.toString() },
                ProgressListener.EMPTY,
                new SmithWatermanLinearSpaceAffine(scoresWithAdditionalCharacters, gapOpenPenalty, gapExtensionPenalty)
        );

        SequenceAnnotationInterval[] primerAlignmentFullMatchIntervals = getFullMatchIntervals(primerAlignmentResult.getIntervals(), sequenceCharSequence.length(), primerSequence.length());
        SequenceAnnotationInterval[] reversePrimerAlignmentFullMatchIntervals = getFullMatchIntervals(reversePrimerAlignmentResult.getIntervals(), sequenceCharSequence.length(), primerSequenceReversed.length());

        int amountToTrimFromLeftEndOfSequence = getAmountToTrimByPrimer(sequenceCharSequence, primerSequence, primerAlignmentResult, primerAlignmentFullMatchIntervals, maxMismatches, minMatchLength, false);
        int amountToTrimFromRightEndOfSequence = getAmountToTrimByPrimer(sequenceCharSequence, primerSequenceReversed, reversePrimerAlignmentResult, reversePrimerAlignmentFullMatchIntervals, maxMismatches, minMatchLength, true);

        return new Trimmage(amountToTrimFromLeftEndOfSequence, amountToTrimFromRightEndOfSequence);
    }

    /**
     * Returns the amount of bases to trim from a sequence using the Smith-Waterman algorithm.
     *
     * @param sequence Sequence to trim.
     * @param primer Primer sequence associated with the Smith-Waterman alignment.
     * @param alignmentResult Results of the Smith-Waterman alignment.
     * @param alignmentFullMatchIntervals Full match intervals of the Smith-Waterman alignment.
     * @param reversed True if the primer that is associated with the trimming is of a reverse direction.
     * @return Amount of bases to remove from (the left end, if reversed==true, or the right end, if reverse==false, of)
     * the supplied sequence.
     */
    private static int getAmountToTrimByPrimer(CharSequence sequence,
                                               CharSequence primer,
                                               SmithWaterman alignmentResult,
                                               SequenceAnnotationInterval[] alignmentFullMatchIntervals,
                                               int maxMismatches,
                                               int minMatchLength,
                                               boolean reversed) {
        if (alignmentFullMatchIntervals[0] == null
                || alignmentFullMatchIntervals[SMITH_WATERMAN_SEQUENCE_INDEX].getLength() < minMatchLength
                || hasMoreThanMaximumNumberOfMismatches(sequence, primer, alignmentResult, alignmentFullMatchIntervals, maxMismatches)) {
            return 0;
        }

        return reversed ? sequence.length() - alignmentFullMatchIntervals[SMITH_WATERMAN_SEQUENCE_INDEX].getFrom() + 1 : alignmentFullMatchIntervals[SMITH_WATERMAN_SEQUENCE_INDEX].getTo();
    }

    /**
     * Checks if the number of mismatched bases in a Smith-Waterman alignment is above a specified threshold.
     *
     * @param sequence The sequence of the Smith-Waterman alignment.
     * @param primer The primer sequence of the Smith-Waterman alignment.
     * @param alignmentResult Result of the Smith-Waterman alignment.
     * @param maxMismatches Maximum number of mismatched bases that are allowed in the Smith-Waterman alignment.
     * @return True if the number of mismatched bases in the Smith-Waterman alignment is > maxMismatches.
     */
    private static boolean hasMoreThanMaximumNumberOfMismatches(CharSequence sequence,
                                                                CharSequence primer,
                                                                SmithWaterman alignmentResult,
                                                                SequenceAnnotationInterval[] alignmentFullMatchIntervals,
                                                                int maxMismatches) {

        CharSequence portionOfSequenceAlignedToPrimer = sequence.subSequence(alignmentFullMatchIntervals[SMITH_WATERMAN_SEQUENCE_INDEX].getFrom() - 1,
                                                                             alignmentFullMatchIntervals[SMITH_WATERMAN_SEQUENCE_INDEX].getTo());
        CharSequence portionOfPrimerAlignedToSequence = primer.subSequence(alignmentFullMatchIntervals[SMITH_WATERMAN_PRIMER_INDEX].getFrom() - 1,
                                                                           alignmentFullMatchIntervals[SMITH_WATERMAN_PRIMER_INDEX].getTo());

        int differenceOfFromBetweenOriginalAndFullIntervalsSequence = alignmentResult.getIntervals()[SMITH_WATERMAN_SEQUENCE_INDEX].getFrom() - alignmentFullMatchIntervals[SMITH_WATERMAN_SEQUENCE_INDEX].getFrom();
        int differenceOfToBetweenOriginalAndFullIntervalsSequence = alignmentFullMatchIntervals[SMITH_WATERMAN_SEQUENCE_INDEX].getTo() - alignmentResult.getIntervals()[SMITH_WATERMAN_SEQUENCE_INDEX].getTo();
        int differenceOfFromBetweenOriginalAndFullIntervalsPrimer = alignmentResult.getIntervals()[SMITH_WATERMAN_PRIMER_INDEX].getFrom() - alignmentFullMatchIntervals[SMITH_WATERMAN_PRIMER_INDEX].getFrom();
        int differenceOfToBetweenOriginalAndFullIntervalsPrimer = alignmentFullMatchIntervals[SMITH_WATERMAN_PRIMER_INDEX].getTo() - alignmentResult.getIntervals()[SMITH_WATERMAN_PRIMER_INDEX].getTo();

        CharSequence portionOfSequenceAlignedToPrimerWithDeletions = portionOfSequenceAlignedToPrimer.subSequence(0, differenceOfFromBetweenOriginalAndFullIntervalsSequence) +
                                                                     alignmentResult.getAlignedSequences()[SMITH_WATERMAN_SEQUENCE_INDEX] +
                                                                     portionOfSequenceAlignedToPrimer.subSequence(portionOfSequenceAlignedToPrimer.length() - differenceOfToBetweenOriginalAndFullIntervalsSequence, portionOfSequenceAlignedToPrimer.length());

        CharSequence portionOfPrimerAlignedToSequenceWithDeletions = portionOfPrimerAlignedToSequence.subSequence(0, differenceOfFromBetweenOriginalAndFullIntervalsPrimer) +
                                                                     alignmentResult.getAlignedSequences()[SMITH_WATERMAN_PRIMER_INDEX] +
                                                                     portionOfPrimerAlignedToSequence.subSequence(portionOfPrimerAlignedToSequence.length() - differenceOfToBetweenOriginalAndFullIntervalsPrimer, portionOfPrimerAlignedToSequence.length());

        int numOfMismatches = 0;

        numOfMismatches += alignmentFullMatchIntervals[SMITH_WATERMAN_PRIMER_INDEX].getFrom() - 1;
        numOfMismatches += primer.length() - alignmentFullMatchIntervals[SMITH_WATERMAN_PRIMER_INDEX].getTo();

        for (int i = 0; i < portionOfSequenceAlignedToPrimerWithDeletions.length(); i++) {
            if (portionOfSequenceAlignedToPrimerWithDeletions.charAt(i) != portionOfPrimerAlignedToSequenceWithDeletions.charAt(i)) {
                numOfMismatches++;
            }
        }

        return numOfMismatches > maxMismatches;
    }

    /**
     * Returns the "full" match intervals of a Smith-Waterman alignment.
     *
     * Given sequence AAAAAAAA and primer GGAG, the expected Smith-Waterman alignment is:
     *
     * --AAAAAAA
     * GGAG-----
     *
     * The aligned sequences are therefore A from the sequence and A from the primer,
     *
     * --{A}AAAAA
     * GG{A}G----
     *
     * Which corresponds to the match intervals 1 -> 1 for the sequence and 3 -> 3 for the primer.
     *
     * The maximum number of mismatches for the primer trimming functionality needs to take into account all pairs of
     * matching bases between the sequence and the primer, which are AA from the sequence and AG from the primer:
     *
     * --{AA}AAAA
     * GG{AG}----
     *
     * Of which The corresponding intervals, 1 -> 2 for the sequence and 3 -> 4 for the primer, are the full match
     * intervals.
     *
     * @param smithWatermanAlignmentIntervals Alignment intervals from the Smith-Waterman alignment.
     * @param sequenceLength Length of the sequence of the Smith-Waterman alignment.
     * @param primerLength Length of the primer of the the Smith-Waterman alignment.
     * @return Full match intervals of the Smith-Waterman alignment.
     */
    private static SequenceAnnotationInterval[] getFullMatchIntervals(SequenceAnnotationInterval[] smithWatermanAlignmentIntervals,
                                                                      int sequenceLength,
                                                                      int primerLength) {
        if (smithWatermanAlignmentIntervals[0] == null) {
            return smithWatermanAlignmentIntervals;
        }

        int sequenceFullIntervalFrom = Math.max(1, smithWatermanAlignmentIntervals[SMITH_WATERMAN_SEQUENCE_INDEX].getFrom() - smithWatermanAlignmentIntervals[SMITH_WATERMAN_PRIMER_INDEX].getFrom() + 1);
        int sequenceFullIntervalTo = Math.min(sequenceLength, smithWatermanAlignmentIntervals[SMITH_WATERMAN_SEQUENCE_INDEX].getTo() + primerLength - smithWatermanAlignmentIntervals[SMITH_WATERMAN_PRIMER_INDEX].getTo());
        int primerFullIntervalFrom = smithWatermanAlignmentIntervals[SMITH_WATERMAN_PRIMER_INDEX].getFrom() - smithWatermanAlignmentIntervals[SMITH_WATERMAN_SEQUENCE_INDEX].getFrom() + sequenceFullIntervalFrom;
        int primerFullIntervalTo = smithWatermanAlignmentIntervals[SMITH_WATERMAN_PRIMER_INDEX].getTo() + sequenceFullIntervalTo - smithWatermanAlignmentIntervals[SMITH_WATERMAN_SEQUENCE_INDEX].getTo();

        return new SequenceAnnotationInterval[] {
                new SequenceAnnotationInterval(sequenceFullIntervalFrom, sequenceFullIntervalTo),
                new SequenceAnnotationInterval(primerFullIntervalFrom, primerFullIntervalTo)
        };
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