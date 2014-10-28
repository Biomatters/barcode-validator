package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;

/**
 * @author Amy Wilson
 * @version $Id$
 *          <p/>
 *          Created on 8/12/2008 12:18:31 PM
 */
class ErrorProbabilityTrimmer {

    // calculating the confidence is a bottleneck so cache it
    private static final double[] confCache;
    static {
        confCache = new double[101];
        for (int i = 0; i <= 100; ++i) {
            confCache[i] = _confidenceToErrorProbability(i);
        }
    }

    /**
     * Confidence values are
     * on a log scale (conf = -10 * log10(errorProb), there for to calculate error probability
     * the following formula is used:
     * <p/>
     * errorProb = 10^(confidence / -10)
     *
     * @param confidence
     * @return
     */
    static double confidenceToErrorProbability(int confidence) {
        if (confidence >= 0 && confidence <= 100) {
            return confCache[confidence];
        }
        return _confidenceToErrorProbability(confidence);
    }

    private static double _confidenceToErrorProbability(int confidence) {
        return Math.pow(10.0, (confidence / -10.0));
    }

    /**
     * Trims the ends of the sequence using the modified Mott algorithm. If no region of acceptable quality is found
     * then [sequence.length, 0] will be returned. ie. the entire sequence should be discarded.
     *
     * @param _sequence to trim
     * @param cutOff bases with error probability less than this will be considered "good". See {@link #confidenceToErrorProbability(int)}
     * @return array where array[0] = number of residues trimmed from beginning of sequence and array[1] =
     * number trimmed from end.
     */
    static Trimmage getTrimmage(NucleotideSequenceDocument _sequence, TrimmableEnds trimmableEnds, double cutOff) {
        if (!(_sequence instanceof NucleotideGraphSequenceDocument)) {
            return Trimmage.EMPTY;
        }
        NucleotideGraphSequenceDocument sequence = (NucleotideGraphSequenceDocument) _sequence;
        if (!sequence.hasSequenceQualities()) {
            return Trimmage.EMPTY;
        }
        if (trimmableEnds == TrimmableEnds.Both) {
            return getTrimmageBothEnds(sequence, cutOff);
        } else {
            return getTrimmageOneEnd(sequence, trimmableEnds, cutOff);
        }
    }

    private static Trimmage getTrimmageOneEnd(NucleotideGraphSequenceDocument sequence, TrimmableEnds trimmableEnds, double cutOff) {
        BaseIterator iterator = BaseIterator.get(Trimmage.EMPTY, sequence, trimmableEnds);
        double segmentErrorProbability = 0;
        double bestSegmentErrorProbability = Double.MAX_VALUE;
        int bestSegment = 0;
        while (iterator.hasNext()) {
            int index = iterator.next();

            segmentErrorProbability += confidenceToErrorProbability(sequence.getSequenceQuality(index -1)) - cutOff;
            if (segmentErrorProbability > 0) {
                continue;
            }
            //less than or equal because longer is better if the score is the same.
            if (segmentErrorProbability <= bestSegmentErrorProbability) {
                bestSegmentErrorProbability = segmentErrorProbability;
                bestSegment = iterator.finalIndex();
            }
        }
        return BaseIterator.getTrimmage(sequence, trimmableEnds, bestSegment);
    }

    private static Trimmage getTrimmageBothEnds(NucleotideGraphSequenceDocument sequence, double cutOff) {
        int length = sequence.getSequenceLength();

        double[] confidences = new double[length];
        for (int i = 0; i < length; ++i) {
            confidences[i] = confidenceToErrorProbability(sequence.getSequenceQuality(i)) - cutOff;
        }

        int[] bestSegment = new int[] {length, length - 1};
        double bestSegmentErrorProbability = Double.MAX_VALUE;
        for (int i = 0; i <= length; i++) {
            double segmentErrorProbability = 0;
            for (int j = i; j < length; j++) {
                segmentErrorProbability += confidences[j];
                if (segmentErrorProbability > 0) {
                    continue;
                }
                //less than or equal because longer is better if the score is the same.
                if (segmentErrorProbability <= bestSegmentErrorProbability) {
                    bestSegmentErrorProbability = segmentErrorProbability;
                    bestSegment[0] = i;
                    bestSegment[1] = j;
                }
            }
        }
        return new Trimmage(bestSegment[0], length - 1 - bestSegment[1]);
    }
}