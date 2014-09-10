package com.biomatters.plugins.barcoding.validator.research.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author Gen Li
 *         Created on 10/09/14 7:08 AM
 */
public class TrimmingTest extends Assert {
    @Test
    public void testDocumentsTrimmedCorrectly() {
        SequenceCharSequence sequence = SequenceCharSequence.valueOf("TAGCTAGC");
        assertEquals(SequenceCharSequence.valueOf("AGCTA"),
                     NucleotideSequenceDocumentTrimmer.trimSequence(sequence, 1, sequence.length() - 2));
    }
}