package com.biomatters.plugins.barcoding.validator.research.input.map;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

import java.util.*;

/**
 * @author Gen Li
 *         Created on 4/09/14 12:40 PM
 */
public class FileNameMapper extends BarcodesToTracesMapper {
    private String traceSeparator;
    private String barcodeSeparator;

    private int traceNamePart;
    private int barcodeNamePart;

    public FileNameMapper(String traceSeparator,
                          int traceNamePart,
                          String barcodeSeparator,
                          int barcodeNamePart) {
        this.traceSeparator = traceSeparator;
        this.traceNamePart = traceNamePart;
        this.barcodeSeparator = barcodeSeparator;
        this.barcodeNamePart = barcodeNamePart;
    }

    @Override
    public Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>
    map(List<NucleotideSequenceDocument> barcodes, List<NucleotideSequenceDocument> traces)
            throws DocumentOperationException {

        /* Map trace documents to part of name of trace documents to match. */
        Map<NucleotideSequenceDocument, String> tracesToNameParts = mapDocumentToDocumentNamePart(traces,
                                                                                                  traceSeparator,
                                                                                                  traceNamePart);

        /* Map barcode sequence documents to part of name of barcode sequence document to match. */
        Map<NucleotideSequenceDocument, String> barcodesToNameParts = mapDocumentToDocumentNamePart(barcodes,
                                                                                                    barcodeSeparator,
                                                                                                    barcodeNamePart);

        return mapBarcodesToTraces(tracesToNameParts, barcodesToNameParts);
    }

    private Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>
    mapBarcodesToTraces(Map<NucleotideSequenceDocument, String> tracesToNameParts,
                        Map<NucleotideSequenceDocument, String> barcodesToNameParts)
            throws DocumentOperationException {
        Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>> result
                = new HashMap<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>();

        for (NucleotideSequenceDocument barcode : barcodesToNameParts.keySet())
            result.put(barcode, new ArrayList<NucleotideSequenceDocument>());

        for (Map.Entry<NucleotideSequenceDocument, String> traceToNamePart : tracesToNameParts.entrySet()) {
            String namePart = traceToNamePart.getValue();
            NucleotideSequenceDocument barcode = null;

            for (Map.Entry<NucleotideSequenceDocument, String> barcodeToNamePart : barcodesToNameParts.entrySet())
                if (barcodeToNamePart.getValue().equals(namePart))
                    barcode = barcodeToNamePart.getKey();

            if (barcode == null)
                throw new DocumentOperationException("Could not match traces to barcodes: " +
                                                     "Found trace with no associated barcode.");

            result.get(barcode).add(traceToNamePart.getKey());
        }

        return result;
    }

    private Map<NucleotideSequenceDocument, String>
    mapDocumentToDocumentNamePart(List<NucleotideSequenceDocument> documents, String separator, int partNumber)
            throws DocumentOperationException {
        Map<NucleotideSequenceDocument, String> result = new HashMap<NucleotideSequenceDocument, String>();

        for (NucleotideSequenceDocument document : documents) {
            String documentName = document.getName();
            if (!document.getName().contains(separator))
                throw new DocumentOperationException("Could not match traces to barcodes: " +
                                                     "Document name + '" + documentName + "' " +
                                                     "does not contain separator '" + separator + "'.");

            result.put(document, getNamePartToMatch(documentName, separator, partNumber));
        }

        return result;
    }

    private String getNamePartToMatch(String name, String separator, int partNumber) {
        return name.split(separator)[partNumber];
    }
}