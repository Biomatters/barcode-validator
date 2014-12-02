package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

import com.google.common.collect.Multimap;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Matthew Cheung
 *         Created on 25/09/14 4:27 PM
 */
public class FileNameMapperTest extends Assert {
    @Test
    public void test() throws DocumentOperationException {
        String traceSeparator = " ";
        String barcodeSeparator = "_";
        int partOfTraceName = 1;
        int partOfBarcodeName = 0;

        AnnotatedPluginDocument trace1 = DocumentUtilities.createAnnotatedPluginDocument(new DefaultNucleotideSequence("trace" + traceSeparator + "groupOne", "", "", null));
        AnnotatedPluginDocument trace2 = DocumentUtilities.createAnnotatedPluginDocument(new DefaultNucleotideSequence("trace2" + traceSeparator + "groupOne", "", "", null));
        AnnotatedPluginDocument trace3 = DocumentUtilities.createAnnotatedPluginDocument(new DefaultNucleotideSequence("trace3" + traceSeparator + "groupTwo", "", "", null));

        AnnotatedPluginDocument barcode1 = DocumentUtilities.createAnnotatedPluginDocument(new DefaultNucleotideSequence("groupOne" + barcodeSeparator + "barcode1", "", "", null));
        AnnotatedPluginDocument barcode2 = DocumentUtilities.createAnnotatedPluginDocument(new DefaultNucleotideSequence("groupTwo" + barcodeSeparator + "barcode2", "", "", null));

        Collection<AnnotatedPluginDocument> traces = Arrays.asList(trace1, trace2, trace3);
        Collection<AnnotatedPluginDocument> barcodes = Arrays.asList(barcode1, barcode2);

        Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> barcodesToTraces = new FileNameMapper(traceSeparator, partOfTraceName, barcodeSeparator, partOfBarcodeName).map(
                barcodes,
                traces
        );

        assertEquals(Arrays.asList(trace1, trace2), barcodesToTraces.get(barcode1));
        assertEquals(Arrays.asList(trace3), barcodesToTraces.get(barcode2));
    }
}
