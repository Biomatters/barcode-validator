package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;

import java.util.Iterator;

/**
 * @author Amy Wilson
 *          <p/>
 *          Created on 4/12/2008 4:26:52 PM
 */
abstract class BaseIterator implements Iterator<Integer> {
    protected final SequenceCharSequence sequence;
    protected int index;
    protected int previousIndex;
    protected final Trimmage existingTrimmage;

    BaseIterator(Trimmage existingTrimmage, SequenceDocument sequenceDocument) {
        this.existingTrimmage = existingTrimmage;
        this.sequence = sequenceDocument.getCharSequence();
        previousIndex = getStartIndex();
        index = previousIndex;
    }
    public boolean hasNext() {
        return nextIndex() != getEndIndex();
    }

    public Integer next() {
        previousIndex = index;
        index = nextIndex();
        return index + 1;
    }

    public void remove() {
        throw new UnsupportedOperationException("Cannot remove");
    }

    abstract int finalIndex();

    abstract int getStartIndex();
    protected abstract int getEndIndex();
    protected abstract int nextIndex();

    static BaseIterator get(Trimmage existingTrimmage, SequenceDocument sequence, TrimmableEnds trimmableEnds) {
        if (trimmableEnds == TrimmableEnds.End) {
            return new IncrementingBaseIterator(existingTrimmage, sequence);
        } else {
            assert trimmableEnds == TrimmableEnds.Start;
            return new DecrementingBaseIterator(existingTrimmage, sequence);
        }
    }

    private static class IncrementingBaseIterator extends BaseIterator {

        IncrementingBaseIterator(Trimmage existingTrimmage, SequenceDocument sequence) {
            super(existingTrimmage, sequence);
        }

        protected int getStartIndex() {
            return BaseCriteriaTrimmer.getStartIndex(existingTrimmage, sequence) - 2; // double offset due to how we're handling indicers
        }

        protected int getEndIndex() {
            return BaseCriteriaTrimmer.getEndIndex(existingTrimmage, sequence);
        }

        protected int nextIndex() {
            return index + 1;
        }

        int finalIndex() {
            return previousIndex + 1;
        }
    }

    private static class DecrementingBaseIterator extends BaseIterator {

        DecrementingBaseIterator(Trimmage existingTrimmage, SequenceDocument sequence) {
            super(existingTrimmage, sequence);
        }

        protected int getStartIndex() {
            return BaseCriteriaTrimmer.getEndIndex(existingTrimmage, sequence);
        }

        protected int getEndIndex() {
            return BaseCriteriaTrimmer.getStartIndex(existingTrimmage, sequence) - 2; // double offset due to how we're handling indicers
        }

        protected int nextIndex() {
            return index - 1;
        }
        int finalIndex() {
            return previousIndex;
        }
    }

    static Trimmage getTrimmage(NucleotideSequenceDocument sequenceDocument, TrimmableEnds trimmableEnds, int currentIndex) {
        if (trimmableEnds == TrimmableEnds.Start) {
            return new Trimmage(currentIndex, 0);
        } else {
            assert trimmableEnds == TrimmableEnds.End;
            return new Trimmage(0, sequenceDocument.getSequenceLength() - currentIndex);
        }
    }

}