package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.EditableSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.implementations.SequenceExtractionUtilities;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.OligoSequenceDocument;
import com.biomatters.geneious.publicapi.utilities.SequenceUtilities;
import jebl.evolution.align.SmithWatermanLinearSpaceAffine;
import jebl.evolution.align.scores.Scores;
import jebl.evolution.sequences.NucleotideState;
import jebl.evolution.sequences.Nucleotides;
import jebl.util.ProgressListener;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Static methods for trimming sequences. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 9/09/14 11:03 AM
 */
public class SequenceTrimmer {
    public static final String ANNOTATION_SUFFIX = "trims annotated";
    public static final String TRIMMED_SUFFIX    = "trimmed";
    
    private static final int SMITH_WATERMAN_SEQUENCE_INDEX = 0;
    private static final int SMITH_WATERMAN_PRIMER_INDEX   = 1;

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
     * @param trimByAddingAnnotations If true, trim regions are annotated. If false, trim regions are removed.
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
                                                                                  boolean trimByAddingAnnotations,
                                                                                  AtomicBoolean hasPrimerTrimmed) {
        List<Trimmage> trimmages = new ArrayList<Trimmage>();

        /* Get the Trimmage that derives from running the modified Mott algorithm on the supplied sequence. */
        trimmages.add(ErrorProbabilityTrimmer.getTrimmage(sequence, TrimmableEnds.Both, errorProbabilityLimit));

        /* Get the Trimmages that derive from aligning the supplied sequence with each of the supplied primers using the
         * Smith-Waterman algorithm.
         */
        for (OligoSequenceDocument primer : primers) {
            Trimmage trimmageForPrimerTrimming = getTrimmageForPrimerTrimming(sequence, primer, gapOpenPenalty, gapExtensionPenalty, scores, maxMismatches, minMatchLength);
            if (trimmageForPrimerTrimming.trimAtStart + trimmageForPrimerTrimming.trimAtEnd > 0) {
                hasPrimerTrimmed.set(true);
            }
            trimmages.add(trimmageForPrimerTrimming);
        }

        /* Calculate the maximization of the Trimmages. */
        Trimmage maxTrimmage = max(trimmages);

        if (maxTrimmage.trimAtStart >= sequence.getSequenceLength() - maxTrimmage.trimAtEnd + 1) {
            maxTrimmage = new Trimmage(sequence.getSequenceLength(), 0);
        }

        if (trimByAddingAnnotations && sequence instanceof DefaultSequenceDocument) {
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
     * Trims the supplied sequence by removing regions that are annotated by Trimmed annotations.
     *
     * @param sequence Sequence to trim.
     * @return Trimmed sequence.
     */
    public static NucleotideGraphSequenceDocument trimSequenceUsingAnnotations(NucleotideGraphSequenceDocument sequence) {
        List<SequenceAnnotationInterval> nonTrimmedIntervals = getNonTrimmedIntervals(sequence);

        if (nonTrimmedIntervals.isEmpty()) {
            return (NucleotideGraphSequenceDocument)SequenceExtractionUtilities.extract(sequence, new SequenceExtractionUtilities.ExtractionOptions(1, sequence.getSequenceLength()));
        } else {
            return (NucleotideGraphSequenceDocument)SequenceExtractionUtilities.extract(sequence, new SequenceExtractionUtilities.ExtractionOptions(nonTrimmedIntervals));
        }
    }

    /**
     * Trims the supplied sequence by using the supplied Trimmage.
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

        List<SequenceAnnotation> primerAnnotations = new ArrayList<SequenceAnnotation>();

        int amountToTrimFromLeftEndOfSequence = getAmountToTrimUsingPrimer(sequenceCharSequence, primerSequence, primerAlignmentResult, primerAlignmentFullMatchIntervals, maxMismatches, minMatchLength, false);
        if (amountToTrimFromLeftEndOfSequence > 0) {
            SequenceAnnotationInterval intervalInSeqAlignedToPrimer = getIntervalOfPrimerInSequence(
                    primer.getSequenceLength(),
                    primerAlignmentFullMatchIntervals[SMITH_WATERMAN_SEQUENCE_INDEX],
                    primerAlignmentFullMatchIntervals[SMITH_WATERMAN_PRIMER_INDEX]
            );

            primerAnnotations.add(new SequenceAnnotation(primer.getName(), SequenceAnnotation.TYPE_PRIMER_BIND, intervalInSeqAlignedToPrimer));
        }

        int amountToTrimFromRightEndOfSequence = getAmountToTrimUsingPrimer(sequenceCharSequence, primerSequenceReversed, reversePrimerAlignmentResult, reversePrimerAlignmentFullMatchIntervals, maxMismatches, minMatchLength, true);
        if (amountToTrimFromRightEndOfSequence > 0) {
            SequenceAnnotationInterval intervalInSeqAlignedToPrimer = getIntervalOfPrimerInSequence(
                    primer.getSequenceLength(),
                    reversePrimerAlignmentFullMatchIntervals[SMITH_WATERMAN_SEQUENCE_INDEX].reverse(),
                    reversePrimerAlignmentFullMatchIntervals[SMITH_WATERMAN_PRIMER_INDEX]
            );

            primerAnnotations.add(new SequenceAnnotation(primer.getName(), SequenceAnnotation.TYPE_PRIMER_BIND_REVERSE, intervalInSeqAlignedToPrimer));
        }

        if (!primerAnnotations.isEmpty() && sequence instanceof EditableSequenceDocument) {
            primerAnnotations.addAll(sequence.getSequenceAnnotations());

            ((EditableSequenceDocument)sequence).setAnnotations(primerAnnotations);
        }

        return new Trimmage(amountToTrimFromLeftEndOfSequence, amountToTrimFromRightEndOfSequence);
    }

    private static SequenceAnnotationInterval getIntervalOfPrimerInSequence(int primerLength, SequenceAnnotationInterval sequenceOverlapInterval, SequenceAnnotationInterval primerOverlapInterval) {
        int startOfPrimerInSequence = sequenceOverlapInterval.getMinimumIndex() - (primerOverlapInterval.getMinimumIndex() - 1);
        int endOfPrimerInSequence = sequenceOverlapInterval.getMaximumIndex() + (primerLength - primerOverlapInterval.getMaximumIndex());

        return new SequenceAnnotationInterval(startOfPrimerInSequence, endOfPrimerInSequence, sequenceOverlapInterval.getDirection());
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
    private static int getAmountToTrimUsingPrimer(CharSequence sequence,
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
            if (isMismatch(portionOfSequenceAlignedToPrimerWithDeletions.charAt(i), portionOfPrimerAlignedToSequenceWithDeletions.charAt(i))) {
                numOfMismatches++;
            }
        }

        return numOfMismatches > maxMismatches;
    }

    private static boolean isMismatch(char sequenceChar, char primerChar) {
        NucleotideState sequenceState = Nucleotides.getState(sequenceChar);
        NucleotideState primerState = Nucleotides.getState(primerChar);

        if (sequenceState == null || primerState == null || sequenceState == Nucleotides.GAP_STATE || primerState == Nucleotides.GAP_STATE) {
            return true;
        }

        return !sequenceState.possiblyEqual(primerState);
    }

    /**
     * Returns the "full" match intervals of a Smith-Waterman alignment.  The intervals will always be in the
     * {@link com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval.Direction#leftToRight}
     * direction.
     * <p>
     * Given sequence AAAAAAAA and primer GGAG, the expected Smith-Waterman alignment is:
     * </p>
     * <pre>
     * --AAAAAAA
     * GGAG-----
     * </pre>
     * The aligned sequences are therefore A from the sequence and A from the primer,
     * <pre>
     * --{A}AAAAA
     * GG{A}G----
     * </pre>
     * <p>
     * Which corresponds to the match intervals 1 -> 1 for the sequence and 3 -> 3 for the primer.
     * </p>
     * <p>
     * The maximum number of mismatches for the primer trimming functionality needs to take into account all pairs of
     * matching bases between the sequence and the primer, which are AA from the sequence and AG from the primer:
     * </p>
     * <pre>
     * --{AA}AAAA
     * GG{AG}----
     * </pre>
     * Of which The corresponding intervals, 1 -> 2 for the sequence and 3 -> 4 for the primer, are the full match
     * intervals.
     *
     * @param smithWatermanAlignmentIntervals Alignment intervals from the Smith-Waterman alignment.
     * @param sequenceLength Length of the sequence of the Smith-Waterman alignment.
     * @param primerLength Length of the primer of the the Smith-Waterman alignment.
     * @return Full match intervals of the Smith-Waterman alignment.
     */
    private static SequenceAnnotationInterval[] getFullMatchIntervals(SequenceAnnotationInterval[] smithWatermanAlignmentIntervals, int sequenceLength, int primerLength) {
        if (smithWatermanAlignmentIntervals[0] == null) {
            return smithWatermanAlignmentIntervals;
        }

        SequenceAnnotationInterval primerIntervalInSequence = getIntervalOfPrimerInSequence(
                primerLength,
                smithWatermanAlignmentIntervals[SMITH_WATERMAN_SEQUENCE_INDEX],
                smithWatermanAlignmentIntervals[SMITH_WATERMAN_PRIMER_INDEX]
        );

        // Need to truncate primer interval if it extends past end of sequence
        int sequenceFullIntervalFrom = Math.max(1, primerIntervalInSequence.getMinimumIndex());
        int sequenceFullIntervalTo = Math.min(sequenceLength, primerIntervalInSequence.getMaximumIndex());

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

    /**
     * Returns annotations that denote regions from the supplied sequence that are not annotated by Trimmed annotations.
     *
     * @param sequence Sequence to create annotations for.
     * @return "Non-trimmed" annotations of the supplied sequence.
     */
    private static List<SequenceAnnotationInterval> getNonTrimmedIntervals(NucleotideGraphSequenceDocument sequence) {
        return getNonTrimmedIntervals(getTrimmedIntervals(sequence), sequence.getSequenceLength());
    }

    /**
     * Returns annotations that denote regions from a sequence that are not annotated by Trimmed annotations.
     *
     * @param trimmedIntervals Trimmed intervals of the sequence.
     * @param sequenceLength Length of the sequence.
     * @return "Non-trimmed" annotations of the sequence.
     */
    private static List<SequenceAnnotationInterval> getNonTrimmedIntervals(List<SequenceAnnotationInterval> trimmedIntervals, int sequenceLength) {
        List<SequenceAnnotationInterval> nonTrimmedIntervals = new ArrayList<SequenceAnnotationInterval>();
        boolean buildingOfNonTrimmedIntervalInProgress = false;
        int startIndexOfNonTrimmedInterval = -1;

        /* Scan for and accumulate non trimmed intervals. */
        for (int i = 1; i <= sequenceLength; i++) {
            if (buildingOfNonTrimmedIntervalInProgress) {
                if (isInIntervals(i, trimmedIntervals)) {
                    nonTrimmedIntervals.add(new SequenceAnnotationInterval(startIndexOfNonTrimmedInterval, i - 1));
                    buildingOfNonTrimmedIntervalInProgress = false;
                }
            } else {
                if (!isInIntervals(i, trimmedIntervals)) {
                    buildingOfNonTrimmedIntervalInProgress = true;
                    startIndexOfNonTrimmedInterval = i;
                }
            }
        }

        /* If exists, add the non trimmed interval at the right end of the sequence. */
        if (buildingOfNonTrimmedIntervalInProgress) {
            nonTrimmedIntervals.add(new SequenceAnnotationInterval(startIndexOfNonTrimmedInterval, sequenceLength));
        }

        return nonTrimmedIntervals;
    }

    /**
     * Retrieves the Trimmed annotation intervals from the supplied sequence.
     *
     * @param sequence Sequence.
     * @return Trimmed annotation intervals.
     */
    private static List<SequenceAnnotationInterval> getTrimmedIntervals(NucleotideGraphSequenceDocument sequence) {
        List<SequenceAnnotationInterval> trimmedIntervals = new ArrayList<SequenceAnnotationInterval>();

        for (SequenceAnnotation annotation : sequence.getSequenceAnnotations()) {
            if (annotation.getType().equals(SequenceAnnotation.TYPE_TRIMMED)) {
                trimmedIntervals.addAll(annotation.getIntervals());
            }
        }

        return trimmedIntervals;
    }

    /**
     * Checks if the supplied index is within the range of any of the supplied intervals.
     *
     * @param index One-based index.
     * @param intervals Intervals.
     * @return True if the supplied index is within the range of any of the supplied intervals.
     */
    private static boolean isInIntervals(int index, Collection<SequenceAnnotationInterval> intervals) {
        for (SequenceAnnotationInterval interval : intervals) {
            if (interval.contains(index)) {
                return true;
            }
        }

        return false;
    }
}