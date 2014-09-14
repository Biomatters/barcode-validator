package com.biomatters.plugins.barcoding.validator.validation.trimming;

import java.util.List;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;

import java.util.ArrayList;

/**
 * Trims by checking whether bases meet particular criteria
 * @author Amy WIlson
 * @version $Id$
 *          <p/>
 *          Created on 16/06/2008 11:52:59
 */
class BaseCriteriaTrimmer {

    /**
     * Finds the longest region in the sequence which meets the given criteria
     *
     * @param sequenceDocument sequence to trim
     * @param criteria criteria to meet
     * @return array where array[0] = number of residues trimmed from beginning of sequence and array[1] =
     * number trimmed from end.
     */
    static Trimmage getTrimmage(Trimmage existingTrimmage, NucleotideSequenceDocument sequenceDocument, TrimmableEnds trimmableEnds, List<Criterion> criteria) {
        if (trimmableEnds == TrimmableEnds.Both) {
            return getTrimmageBothEnds(existingTrimmage, sequenceDocument, criteria);
        } else {
            return getTrimmageOneEnd(existingTrimmage, sequenceDocument, trimmableEnds, criteria);
        }
    }
    private static Trimmage getTrimmageOneEnd(Trimmage existingTrimmage, NucleotideSequenceDocument sequenceDocument, TrimmableEnds trimmableEnds, List<Criterion> criteria) {
        BaseIterator iterator = BaseIterator.get(existingTrimmage, sequenceDocument, trimmableEnds);
        while (iterator.hasNext()) {
            int index = iterator.next();
            for (Criterion unmetCriterionForBase: unmetCriteria(criteria, index)) {
                unmetCriterionForBase.incrementFailedBases();
                if (unmetCriterionForBase.exceededFailedBaseLimit()) {
                    return BaseIterator.getTrimmage(sequenceDocument, trimmableEnds, iterator.finalIndex());
                }
            }
        }
        return Trimmage.EMPTY;
    }

    private static Trimmage getTrimmageBothEnds(Trimmage existingTrimmage, NucleotideSequenceDocument sequenceDocument, List<Criterion> criteria) {
        SequenceCharSequence sequence = sequenceDocument.getCharSequence();

        int firstNonGap = getStartIndex(existingTrimmage, sequence);
        int lastNonGap = getEndIndex(existingTrimmage, sequence);
        SequenceAnnotationInterval currentInterval = new SequenceAnnotationInterval(firstNonGap, firstNonGap);
        SequenceAnnotationInterval bestInterval = new SequenceAnnotationInterval(firstNonGap, firstNonGap - 1);

        // We first extend currentInterval until the current interval only just meets the criteria
        // (or we reach the sequence end), and then we maintain the invariant
        // that the interval only just meets the criteria but keep moving the interval to the right
        while (currentInterval.getTo() <= lastNonGap) {
            for (Criterion unmetCriterionForBase: unmetCriteria(criteria, currentInterval.getTo())) {
                unmetCriterionForBase.incrementFailedBases();
            }
            currentInterval = incrementStartUntilMeetsCriteria(currentInterval, criteria);

            if (currentInterval.getLength() > bestInterval.getLength()) {
                bestInterval = currentInterval;
            }
            currentInterval = incrementEnd(currentInterval);
        }

        return new Trimmage(bestInterval.getFrom() - 1, sequence.length() - bestInterval.getTo());
    }

    // inclusive 1-offset index or exclusive 0-offset as you prefer
    static int getEndIndex(Trimmage existingTrimmage, SequenceCharSequence sequence) {
        return Math.min(sequence.length() - existingTrimmage.trimAtEnd, sequence.getTrailingGapsStartIndex());
    }

    // inclusive 1-offset
    static int getStartIndex(Trimmage existingTrimmage, SequenceCharSequence sequence) {
        return Math.max(existingTrimmage.trimAtStart, sequence.getLeadingGapsLength()) + 1;
    }

    private static SequenceAnnotationInterval incrementStartUntilMeetsCriteria(
            SequenceAnnotationInterval currentInterval, List<Criterion> criteria) {
        while (!exceededFailedBaseLimit(criteria).isEmpty()) {
            for (Criterion unmetCriterionInDroppedBase: unmetCriteria(criteria, currentInterval.getFrom())) {
                unmetCriterionInDroppedBase.decrementFailedBases();
            }
            currentInterval = incrementStart(currentInterval);
        }

        return currentInterval;
    }
    private static List<Criterion> exceededFailedBaseLimit(List<Criterion> criteria) {
        List<Criterion> reachedFailedBaseLimit = new ArrayList<Criterion>();
        for (Criterion criterion: criteria) {
            if (criterion.exceededFailedBaseLimit()) {
                reachedFailedBaseLimit.add(criterion);
            }
        }
        return reachedFailedBaseLimit;
    }

    private static List<Criterion> unmetCriteria(List<Criterion> criteriaToMeet, int index) {
        List<Criterion> unmetCriteria = new ArrayList<Criterion>();
        for (Criterion criterion: criteriaToMeet) {
            boolean meets = criterion.meets(index);
            if (!meets) {
                unmetCriteria.add(criterion);
            }
        }
        return unmetCriteria;
    }

    private static SequenceAnnotationInterval incrementStart(SequenceAnnotationInterval interval) {
        return new SequenceAnnotationInterval(interval.getFrom() + 1, interval.getTo());
    }

    private static SequenceAnnotationInterval incrementEnd(SequenceAnnotationInterval interval) {
        return new SequenceAnnotationInterval(interval.getFrom(), interval.getTo() + 1);
    }
}