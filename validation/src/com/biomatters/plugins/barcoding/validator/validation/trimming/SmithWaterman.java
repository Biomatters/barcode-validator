package com.biomatters.plugins.barcoding.validator.validation.trimming;

import jebl.evolution.align.SmithWatermanLinearSpaceAffine;
import jebl.util.ProgressListener;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;

/**
 * @author Amy Wilson
 * @version $Id$
 *          <p/>
 *          Created on 22/07/2009 10:24:58 AM
 */
public class SmithWaterman {
    private final SequenceAnnotationInterval[] intervals;
    private final String[] alignedSequences;

    public SmithWaterman(String[] sequences, ProgressListener progressListener, SmithWatermanLinearSpaceAffine algorithm) {
        algorithm.doAlignment(sequences[0], sequences[1], progressListener);
        alignedSequences = algorithm.getMatch();
        intervals = getIntervals(sequences, alignedSequences);
    }

    private static SequenceAnnotationInterval[] getIntervals(String[] sequences, String[] alignedStrings) {
        return new SequenceAnnotationInterval[] {
                getInterval(sequences[0], alignedStrings[0]), getInterval(sequences[1], alignedStrings[1])
        };
    }

    public String[] getAlignedSequences() {
        return alignedSequences;
    }

    public SequenceAnnotationInterval[] getIntervals() {
        return intervals;
    }

    private static SequenceAnnotationInterval getInterval(String sequence, String matchRegion) {
        matchRegion = matchRegion.replace("-", "");
        if (matchRegion.length() == 0) {
            return null;
        }
        int start = sequence.indexOf(matchRegion);
        return new SequenceAnnotationInterval(start + 1, start + matchRegion.length());
    }
}
