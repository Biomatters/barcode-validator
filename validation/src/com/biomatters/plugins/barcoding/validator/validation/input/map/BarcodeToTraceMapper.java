package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Gen Li
 *         Created on 3/09/14 5:26 PM
 */
public abstract class BarcodeToTraceMapper {
    public abstract Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> map(Collection<AnnotatedPluginDocument> barcodes, Collection<AnnotatedPluginDocument> traces) throws DocumentOperationException;

    protected static void throwExceptionIfThereAreTracesWithoutAnAssociatedBarcode(Collection<AnnotatedPluginDocument> allTraces, Collection<AnnotatedPluginDocument> mappedTraces)
            throws DocumentOperationException {
        Collection<AnnotatedPluginDocument> tracesWithoutAnAssociatedBarcode =  getTracesWithoutAnAssociatedBarcode(allTraces, mappedTraces);
        if (!tracesWithoutAnAssociatedBarcode.isEmpty()) {
            throw new DocumentOperationException(buildTracesWithoutAnAssociatedBarcodeFoundMessage(tracesWithoutAnAssociatedBarcode));
        }
    }

    private static Collection<AnnotatedPluginDocument> getTracesWithoutAnAssociatedBarcode(Collection<AnnotatedPluginDocument> allTraces, Collection<AnnotatedPluginDocument> mappedTraces) {
        Collection<AnnotatedPluginDocument> tracesWithoutAnAssociatedBarcode = new HashSet<AnnotatedPluginDocument>();

        for (AnnotatedPluginDocument trace : allTraces) {
            if (!mappedTraces.contains(trace)) {
                tracesWithoutAnAssociatedBarcode.add(trace);
            }
        }

        return tracesWithoutAnAssociatedBarcode;
    }

    private static String buildTracesWithoutAnAssociatedBarcodeFoundMessage(Collection<AnnotatedPluginDocument> tracesWithoutAnAssociatedBarcode) {
        Collection<String> namesOfTracesWithoutAnAssociatedBarcode = new ArrayList<String>();

        for (AnnotatedPluginDocument traceWithoutAnAssociatedBarcode : tracesWithoutAnAssociatedBarcode) {
            namesOfTracesWithoutAnAssociatedBarcode.add(traceWithoutAnAssociatedBarcode.getName());
        }

        String commaSeparatedListOfTracesWithoutAnAssociatedBarcode = StringUtilities.join(", ", namesOfTracesWithoutAnAssociatedBarcode);

        return "Unmapped traces: " + (commaSeparatedListOfTracesWithoutAnAssociatedBarcode.isEmpty() ? "None" : commaSeparatedListOfTracesWithoutAnAssociatedBarcode) + " .";
    }
}