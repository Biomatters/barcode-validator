package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
import com.biomatters.geneious.publicapi.utilities.xml.FastSaxBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Gen Li
 *         Created on 5/09/14 9:57 AM
 */
public class GenbankXmlMapper extends BarcodeToTraceMapper {
    private static final String TRACE_VOLUME_ELEMENT_TAG_NAME       = "trace_volume";
    private static final String TRACE_ELEMENT_TAG_NAME              = "trace";
    private static final String TRACE_NAME_ELEMENT_TAG_NAME         = "trace_name";
    private static final String NCBI_TRACE_ARCHIVE_ELEMENT_TAG_NAME = "ncbi_trace_archive";
    private static final String ACCESSION_ELEMENT_TAG_NAME          = "accession";

    private static final String GENBANK_BARCODE_DESCRIPTION_SEPARATOR = "\\|";

    private static final int INDEX_OF_ACCESSION_IN_GENBANK_BARCODE_DESCRIPTION = 3;

    private String genbankTraceInfoFilePath;

    public GenbankXmlMapper(String genbankTraceInfoFilePath) {
        setGenbankTraceInfoFilePath(genbankTraceInfoFilePath);
    }

    @Override
    public Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> map(Collection<AnnotatedPluginDocument> barcodes, Collection<AnnotatedPluginDocument> traces)
            throws DocumentOperationException {
        if (barcodes == null) {
            throw new IllegalArgumentException("barcodes cannot be null.");
        }

        if (traces == null) {
            throw new IllegalArgumentException("traces cannot be null");
        }

        return map(getBarcodesToAccessions(barcodes), getAccessionsToTraces(traces), traces);
    }

    public void setGenbankTraceInfoFilePath(String genbankTraceInfoFilePath) {
        this.genbankTraceInfoFilePath = genbankTraceInfoFilePath;
    }

    public String getGenbankTraceInfoFilePath() {
        return genbankTraceInfoFilePath;
    }

    private static Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> map(Map<AnnotatedPluginDocument, String> barcodesToAccessionsMap,
                                                                                  Multimap<String, AnnotatedPluginDocument> accessionsToTracesMap,
                                                                                  Collection<AnnotatedPluginDocument> allTraces) throws DocumentOperationException {
        Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> barcodesToTraces = ArrayListMultimap.create();

        for (Map.Entry<AnnotatedPluginDocument, String> barcodeToAccession : barcodesToAccessionsMap.entrySet()) {
            barcodesToTraces.putAll(barcodeToAccession.getKey(), accessionsToTracesMap.get(barcodeToAccession.getValue()));
        }

        Collection<AnnotatedPluginDocument> tracesWithoutAnAssociatedBarcode = getTracesWithoutAnAssociatedBarcode(allTraces, barcodesToTraces.values());

        if (!tracesWithoutAnAssociatedBarcode.isEmpty()) {
            throw new DocumentOperationException("Unmapped traces: " + StringUtilities.join(", ", tracesWithoutAnAssociatedBarcode));
        }

        return barcodesToTraces;
    }

    private static Map<AnnotatedPluginDocument, String> getBarcodesToAccessions(Collection<AnnotatedPluginDocument> barcodes) throws DocumentOperationException {
        Map<AnnotatedPluginDocument, String> barcodesToAccessions = new HashMap<AnnotatedPluginDocument, String>();

        for (AnnotatedPluginDocument barcode : barcodes) {
            if (barcode == null) {
                throw new DocumentOperationException("Barcode documents cannot be null.");
            }

            barcodesToAccessions.put(barcode, barcode.getName().split(GENBANK_BARCODE_DESCRIPTION_SEPARATOR)[INDEX_OF_ACCESSION_IN_GENBANK_BARCODE_DESCRIPTION]);
        }

        return barcodesToAccessions;
    }

    private Multimap<String, AnnotatedPluginDocument> getAccessionsToTraces(Collection<AnnotatedPluginDocument> traces) throws DocumentOperationException {
        Multimap<String, AnnotatedPluginDocument> accessionsToTraces = ArrayListMultimap.create();
        Element traceVolume;

        try {
            traceVolume = new FastSaxBuilder().build(new File(genbankTraceInfoFilePath)).getRootElement();
        } catch (JDOMException e) {
            throw new DocumentOperationException("Could not read from " + genbankTraceInfoFilePath + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DocumentOperationException("Could not read from " + genbankTraceInfoFilePath + ": " + e.getMessage(), e);
        }

        if (!traceVolume.getName().equals(TRACE_VOLUME_ELEMENT_TAG_NAME)) {
            throw new DocumentOperationException(
                    "Invalid TRACEINFO.xml file: " +
                    "expected name of the root element: " + TRACE_VOLUME_ELEMENT_TAG_NAME +
                    ", actual name of the root element: " + traceVolume.getName() + "."
            );
        }

        Multimap<String, AnnotatedPluginDocument> traceNamesToTraces = getTraceNamesToTraces(traces);

        for (Element trace : traceVolume.getChildren(TRACE_ELEMENT_TAG_NAME)) {
            Element ncbiTraceArchive = trace.getChild(NCBI_TRACE_ARCHIVE_ELEMENT_TAG_NAME);

            if (ncbiTraceArchive == null) {
                throw new DocumentOperationException(
                        "Invalid TRACEINFO.xml file: " +
                        "a " + TRACE_ELEMENT_TAG_NAME + " element did not contain an " + NCBI_TRACE_ARCHIVE_ELEMENT_TAG_NAME + " element."
                );
            }

            Element accession = ncbiTraceArchive.getChild(ACCESSION_ELEMENT_TAG_NAME);

            if (accession == null) {
                throw new DocumentOperationException(
                        "Invalid TRACEINFO.xml file: " +
                        "an " + NCBI_TRACE_ARCHIVE_ELEMENT_TAG_NAME + " element did not contain an " + ACCESSION_ELEMENT_TAG_NAME + " element."
                );
            }

            Element name = trace.getChild(TRACE_NAME_ELEMENT_TAG_NAME);

            if (name == null) {
                throw new DocumentOperationException(
                        "Invalid TRACEINFO.xml file: " +
                        "a " + TRACE_ELEMENT_TAG_NAME + " element did not contain a " + TRACE_NAME_ELEMENT_TAG_NAME + " element."
                );
            }

            accessionsToTraces.putAll(accession.getTextTrim(), traceNamesToTraces.get(name.getTextTrim()));
        }

        return accessionsToTraces;
    }

    private Multimap<String, AnnotatedPluginDocument> getTraceNamesToTraces(Collection<AnnotatedPluginDocument> traces) throws DocumentOperationException {
        Multimap<String, AnnotatedPluginDocument> tracesToTraceNames = ArrayListMultimap.create();

        for (AnnotatedPluginDocument trace : traces) {
            if (trace == null) {
                throw new DocumentOperationException("Trace documents cannot be null.");
            }

            tracesToTraceNames.put(trace.getName(), trace);
        }

        return tracesToTraceNames;
    }
}