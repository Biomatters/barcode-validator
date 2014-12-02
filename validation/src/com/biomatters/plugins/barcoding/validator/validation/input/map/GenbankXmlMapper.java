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
import java.util.Collection;

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

    private String genbankTraceInfoFilePath;

    public GenbankXmlMapper(String genbankTraceInfoFilePath) {
        setGenbankTraceInfoFilePath(genbankTraceInfoFilePath);
    }

    @Override
    public Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> map(Collection<AnnotatedPluginDocument> traces, Collection<AnnotatedPluginDocument> barcodes)
            throws DocumentOperationException {
        return null;
    }

    public void setGenbankTraceInfoFilePath(String genbankTraceInfoFilePath) {
        this.genbankTraceInfoFilePath = genbankTraceInfoFilePath;
    }

    private Multimap<String, String> getAccessionToTraceName() throws DocumentOperationException {
        Multimap<String, String> accessionToTraceName = ArrayListMultimap.create();
        Element traceVolume;

        try {
            traceVolume = new FastSaxBuilder().build(new File(genbankTraceInfoFilePath)).getRootElement();
        } catch (JDOMException e) {
            throw new DocumentOperationException("Could not read ");
        } catch (IOException e) {
            throw new DocumentOperationException("");
        }

        if (!traceVolume.getName().equals(TRACE_VOLUME_ELEMENT_TAG_NAME)) {
            throw new DocumentOperationException(
                    "Invalid TRACEINFO.xml file: " +
                    "expected name of the root element: " + TRACE_VOLUME_ELEMENT_TAG_NAME +
                    ", actual name of the root element: " + traceVolume.getName() + "."
            );
        }

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

            accessionToTraceName.put(accession.getTextTrim(), name.getTextTrim());
        }

        return accessionToTraceName;
    }
}