package com.biomatters.plugins.barcoding.validator.research.input.map;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

import java.util.*;

/**
 * @author Gen Li
 *         Created on 4/09/14 12:40 PM
 */
public class ByFileNameMapper extends TraceToBarcodeMapper {
    private String traceSeparator;
    private String barcodeSeparator;

    private int traceNamePartToMatch;
    private int barcodeNamePartToMatch;

    public ByFileNameMapper(String traceSeparator,
                            int traceNamePartToMatch,
                            String barcodeSeparator,
                            int barcodeNamePartToMatch) {
        this.traceSeparator = traceSeparator;
        this.traceNamePartToMatch = traceNamePartToMatch;
        this.barcodeSeparator = barcodeSeparator;
        this.barcodeNamePartToMatch = barcodeNamePartToMatch;
    }

    @Override
    public Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>
    map(List<NucleotideSequenceDocument> traces, List<NucleotideSequenceDocument> barcodes)
            throws DocumentOperationException {
        Map<String, NucleotideSequenceDocument> traceDocumentToNamePart =
                new HashMap<String, NucleotideSequenceDocument>();
        Map<String, NucleotideSequenceDocument> barcodeDocumentToNamePart =
                new HashMap<String, NucleotideSequenceDocument>();

        /* Map trace documents to part of name of trace documents to match. */
        traceDocumentToNamePart = mapDocumentToDocumentNamePartToMatch(traces,
                                                                       traceSeparator,
                                                                       traceNamePartToMatch);
        /* Map barcode sequence documents to part of name of barcode sequence document to match. */
        barcodeDocumentToNamePart = mapDocumentToDocumentNamePartToMatch(barcodes,
                                                                         barcodeSeparator,
                                                                         barcodeNamePartToMatch);

        return matchTraceToSequence(traceDocumentToNamePart, barcodeDocumentToNamePart);
    }

    private Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>
    matchTraceToSequence(Map<String, NucleotideSequenceDocument> traceNamePartToMatchToDocument,
                         Map<String, NucleotideSequenceDocument> barcodeNamePartToMatchToDocument)
            throws DocumentOperationException {
        Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>> barcodeToTraces =
                new HashMap<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>();

        for (NucleotideSequenceDocument barcode : barcodeNamePartToMatchToDocument.values()) {
            barcodeToTraces.put(barcode, new ArrayList<NucleotideSequenceDocument>());
        }

        for (Map.Entry<String, NucleotideSequenceDocument> traceEntry : traceNamePartToMatchToDocument.entrySet()) {
            NucleotideSequenceDocument barcode = barcodeNamePartToMatchToDocument.get(traceEntry.getKey());
            if (barcode == null) {
                throw new DocumentOperationException("Could not match traces to barcodes: " +
                                                     "Found trace with no associated barcode.");
            }
            barcodeToTraces.get(barcode).add(traceEntry.getValue());
        }

        return barcodeToTraces;
    }

    private Map<String, NucleotideSequenceDocument>
    mapDocumentToDocumentNamePartToMatch(List<NucleotideSequenceDocument> documents, String separator, int partNumber) {
        Map<String, NucleotideSequenceDocument> map = new HashMap<String, NucleotideSequenceDocument>();
        for (NucleotideSequenceDocument document : documents) {
            map.put(getNamePartToMatch(document.getName(), separator, partNumber), document);
        }
        return map;
    }

    private String getNamePartToMatch(String name, String separator, int partNumber) {
        return name.split(separator)[partNumber];
    }
}