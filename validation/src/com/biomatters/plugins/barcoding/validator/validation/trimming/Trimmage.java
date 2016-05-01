package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;

/**
 * @author Amy Wilson
 *          <p/>
 *          Created on 3/12/2008 2:26:21 PM
 */
public class Trimmage {
    public final int trimAtStart;
    public final int trimAtEnd;

    public Trimmage(int trimAtStart, int trimAtEnd) {
        this.trimAtStart = trimAtStart;
        this.trimAtEnd = trimAtEnd;
    }

    Trimmage addTo(Trimmage trimmage) {
        return new Trimmage(this.trimAtStart + trimmage.trimAtStart, this.trimAtEnd + trimmage.trimAtEnd);
    }

    SequenceAnnotationInterval getNonTrimmedInterval(int sequenceLength) {
        return new SequenceAnnotationInterval(trimAtStart + 1, sequenceLength - trimAtEnd, SequenceAnnotationInterval.Direction.leftToRight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trimmage trimmage = (Trimmage) o;

        return trimAtEnd == trimmage.trimAtEnd && trimAtStart == trimmage.trimAtStart;

    }

    @Override
    public int hashCode() {
        int result = trimAtStart;
        result = 31 * result + trimAtEnd;
        return result;
    }@Override

     public String toString() {
        return "" + trimAtStart + ":" + trimAtEnd;
    }

    Trimmage max(Trimmage that) {
        int start = Math.max(this.trimAtStart, that.trimAtStart);
        int end = Math.max(this.trimAtEnd, that.trimAtEnd);
        return new Trimmage(start, end);
    }


    static final Trimmage EMPTY = new Trimmage(0, 0);

    boolean isEmpty() {
        return equals(EMPTY);
    }
}