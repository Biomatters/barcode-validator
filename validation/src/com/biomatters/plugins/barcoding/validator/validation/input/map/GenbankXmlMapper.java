package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
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
    private static final String TRACE_FILE_ELEMENT_TAG_NAME         = "trace_file";
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

        throwExceptionIfThereAreTracesWithoutAnAssociatedBarcode(allTraces, barcodesToTraces.values());

        return barcodesToTraces;
    }

    private static Map<AnnotatedPluginDocument, String> getBarcodesToAccessions(Collection<AnnotatedPluginDocument> barcodes) throws DocumentOperationException {
        Map<AnnotatedPluginDocument, String> barcodesToAccessions = new HashMap<AnnotatedPluginDocument, String>();

        for (AnnotatedPluginDocument barcode : barcodes) {
            if (barcode == null) {
                throw new DocumentOperationException("Barcode documents cannot be null.");
            }

            barcodesToAccessions.put(barcode, getAccessionFromBarcode(barcode));
        }

        return barcodesToAccessions;
    }

    private static String getAccessionFromBarcode(AnnotatedPluginDocument barcode) {
        String rawAccession = barcode.getName().split(GENBANK_BARCODE_DESCRIPTION_SEPARATOR)[INDEX_OF_ACCESSION_IN_GENBANK_BARCODE_DESCRIPTION];

        int indexOfPeriod = rawAccession.indexOf(".");

        return indexOfPeriod == -1 ? rawAccession : rawAccession.substring(0, indexOfPeriod);
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

        Multimap<String, AnnotatedPluginDocument> traceFileNamesToTraces = getTraceFileNamesToTraces(traces);

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

            Element traceFile = trace.getChild(TRACE_FILE_ELEMENT_TAG_NAME);

            if (traceFile == null) {
                throw new DocumentOperationException(
                        "Invalid TRACEINFO.xml file: " +
                        "a " + TRACE_ELEMENT_TAG_NAME + " element did not contain a " + TRACE_FILE_ELEMENT_TAG_NAME + " element."
                );
            }

            accessionsToTraces.putAll(accession.getTextTrim(), traceFileNamesToTraces.get(parseTraceFileName(traceFile.getTextTrim())));
        }

        return accessionsToTraces;
    }

    private Multimap<String, AnnotatedPluginDocument> getTraceFileNamesToTraces(Collection<AnnotatedPluginDocument> traces) throws DocumentOperationException {
        Multimap<String, AnnotatedPluginDocument> tracesToTraceNames = ArrayListMultimap.create();

        for (AnnotatedPluginDocument trace : traces) {
            if (trace == null) {
                throw new DocumentOperationException("Trace documents cannot be null.");
            }

            tracesToTraceNames.put(trace.getName(), trace);
        }

        return tracesToTraceNames;
    }

    private static String parseTraceFileName(String traceFileName) throws DocumentOperationException {
        if (traceFileName == null || traceFileName.isEmpty()) {
            throw new DocumentOperationException("trace file names cannot be null or empty.");
        }

        int lastIndexOfSlashInTraceFileName = traceFileName.lastIndexOf("/");

        return lastIndexOfSlashInTraceFileName == -1 ? traceFileName : traceFileName.substring(lastIndexOfSlashInTraceFileName + 1);
    }
}