package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.documents.sequence.DefaultNucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

/**
 * @author Gen Li
 *         Created on 20/08/14 4:55 PM
 */
public class BarcodeValidatorTest extends Assert {
    @Test
    public void testNameIsCorrect() {
        assertEquals("Barcode Validator", new BarcodeValidatorPlugin().getName());
    }

    @Test
    public void testGetSetNameUsesBarcodeName() {
        DefaultNucleotideSequence barcode = new DefaultNucleotideSequence("myBarcode", "ACGT");
        NucleotideGraphSequenceDocument trace1 = createTestTraceDoc("trace1", "ACTG");
        NucleotideGraphSequenceDocument trace2 = createTestTraceDoc("trace2", "ACTG");
        assertEquals(barcode.getName(), BarcodeValidatorOperation.getNameForInputSet(barcode, Arrays.asList(trace1, trace2)));

        barcode.setName("abc");
        assertEquals(barcode.getName(), BarcodeValidatorOperation.getNameForInputSet(barcode, Arrays.asList(trace1, trace2)));
    }

    @Test
    public void testGetSetNameUsesTracesIfNoBarcode() {
        String prefix = "trace";
        NucleotideGraphSequenceDocument trace1 = createTestTraceDoc(prefix + "1", "ACTG");
        NucleotideGraphSequenceDocument trace2 = createTestTraceDoc(prefix + "2", "ACTG");
        assertEquals(prefix, BarcodeValidatorOperation.getNameForInputSet(null, Arrays.asList(trace1, trace2)));

        NucleotideGraphSequenceDocument differentName = createTestTraceDoc("a", "ACTG");
        assertEquals("Set 1", BarcodeValidatorOperation.getNameForInputSet(null, Arrays.asList(trace1, trace2, differentName)));
    }

    public NucleotideGraphSequenceDocument createTestTraceDoc(String name, String sequence) {
        return new DefaultNucleotideGraphSequence(name, "", sequence, new Date(), new DefaultNucleotideGraph(
                new int[][]{new int[sequence.length()], new int[sequence.length()], new int[sequence.length()], new int[sequence.length()]},
                new int[sequence.length()], new int[sequence.length()], sequence.length(), sequence.length()
        ));
    }
}
