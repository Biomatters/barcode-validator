package com.biomatters.plugins.barcoding.validator.research.input.map;

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

        Set<String> barcodeNamePartsToMatch = new HashSet<String>();
        for (NucleotideSequenceDocument barcode : barcodes) {
            String namePartToMatch = getNamePartToMatch(barcode.getName(), barcodeSeparator, barcodeNamePartToMatch);
            if (barcodeNamePartsToMatch.contains(namePartToMatch)) {
                throw new DocumentOperationException("Could not match traces to barcodes: " +
                                                     "Two or more documents of which part of name to match is: " +
                                                     namePartToMatch);
            }
            barcodeNamePartsToMatch.add(namePartToMatch);
        }

        /* Map trace documents to part of name of trace documents to match. */
        Map<NucleotideSequenceDocument, String> traceDocumentToNamePart =
                mapDocumentToDocumentNamePartToMatch(traces, traceSeparator, traceNamePartToMatch);

        /* Map barcode sequence documents to part of name of barcode sequence document to match. */
        Map<NucleotideSequenceDocument, String> barcodeDocumentToNamePart =
                mapDocumentToDocumentNamePartToMatch(barcodes, barcodeSeparator, barcodeNamePartToMatch);

        return matchTraceToSequence(traceDocumentToNamePart, barcodeDocumentToNamePart);
    }

    private Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>
    matchTraceToSequence(Map<NucleotideSequenceDocument, String> traceNamePartToMatchToDocument,
                         Map<NucleotideSequenceDocument, String> barcodeNamePartToMatchToDocument)
            throws DocumentOperationException {
        Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>> barcodeToTraces =
                new HashMap<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>();

        for (NucleotideSequenceDocument barcode : barcodeNamePartToMatchToDocument.keySet()) {
            barcodeToTraces.put(barcode, new ArrayList<NucleotideSequenceDocument>());
        }

        for (Map.Entry<NucleotideSequenceDocument, String> traceEntry : traceNamePartToMatchToDocument.entrySet()) {
            String traceNamePartToMatch = traceEntry.getValue();
            NucleotideSequenceDocument barcode = null;
            for (Map.Entry<NucleotideSequenceDocument, String> barcodeEntry : barcodeNamePartToMatchToDocument.entrySet()) {
                if (barcodeEntry.getValue().equals(traceNamePartToMatch)) {
                    barcode = barcodeEntry.getKey();
                }
            }
            if (barcode == null) {
                throw new DocumentOperationException("Could not match traces to barcodes: " +
                                                     "Found trace with no associated barcode.");
            }
            barcodeToTraces.get(barcode).add(traceEntry.getKey());
        }

        return barcodeToTraces;
    }

    private Map<NucleotideSequenceDocument, String>
    mapDocumentToDocumentNamePartToMatch(List<NucleotideSequenceDocument> documents, String separator, int partNumber) {
        Map<NucleotideSequenceDocument, String> map = new HashMap<NucleotideSequenceDocument, String>();
        for (NucleotideSequenceDocument document : documents) {
            map.put(document, getNamePartToMatch(document.getName(), separator, partNumber));
        }
        return map;
    }

    private String getNamePartToMatch(String name, String separator, int partNumber) {
        return name.split(separator)[partNumber];
    }
}