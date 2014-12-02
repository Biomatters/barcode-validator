package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Gen Li
 *         Created on 3/09/14 5:26 PM
 */
public abstract class BarcodeToTraceMapper {
    public abstract Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> map(Collection<AnnotatedPluginDocument> barcodes, Collection<AnnotatedPluginDocument> traces) throws DocumentOperationException;

    protected static Collection<AnnotatedPluginDocument> getTracesWithoutAnAssociatedBarcode(Collection<AnnotatedPluginDocument> allTraces, Collection<AnnotatedPluginDocument> mappedTraces) {
        Collection<AnnotatedPluginDocument> tracesWithoutAnAssociatedBarcode = new HashSet<AnnotatedPluginDocument>();

        for (AnnotatedPluginDocument trace : allTraces) {
            if (!mappedTraces.contains(trace)) {
                tracesWithoutAnAssociatedBarcode.add(trace);
            }
        }

        return tracesWithoutAnAssociatedBarcode;
    }
}