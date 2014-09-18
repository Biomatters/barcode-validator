package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

import java.util.*;

/**
 * Algorithm for mapping barcodes to traces using their file names.
 *
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

    /**
     * Maps barcodes to traces.
     *
     * @param barcodes Barcodes.
     * @param traces Traces.
     * @return Mapping of barcodes to traces.
     * @throws DocumentOperationException
     */
    @Override
    public Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>
    map(List<NucleotideSequenceDocument> barcodes, List<NucleotideSequenceDocument> traces)
            throws DocumentOperationException {

        /* Map traces to the part of their respective name that is used for grouping. */
        Map<NucleotideSequenceDocument, String> tracesToNameParts = mapDocumentToDocumentNamePart(traces,
                                                                                                  traceSeparator,
                                                                                                  traceNamePart);

        /* Map barcodes to the part of their respective name that is used for grouping. */
        Map<NucleotideSequenceDocument, String> barcodesToNameParts = mapDocumentToDocumentNamePart(barcodes,
                                                                                                    barcodeSeparator,
                                                                                                    barcodeNamePart);

        return groupTracesToBarcodes(tracesToNameParts, barcodesToNameParts);
    }

    /**
     * Groups traces to barcodes.
     *
     * @param tracesToNameParts Map of traces to the part of their respective name that is used for grouping.
     * @param barcodesToNameParts Map of barcodes to the part of their respective name that is used for grouping.
     * @return Map of barcodes to traces.
     * @throws DocumentOperationException
     */
    private Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>
    groupTracesToBarcodes(Map<NucleotideSequenceDocument, String> tracesToNameParts,
                          Map<NucleotideSequenceDocument, String> barcodesToNameParts) throws DocumentOperationException {
        Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>> result
                = new HashMap<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>();

        /* Populate the result with the barcodes. */
        for (NucleotideSequenceDocument barcode : barcodesToNameParts.keySet())
            result.put(barcode, new ArrayList<NucleotideSequenceDocument>());

        /* Associate the traces with the barcodes. */
        for (Map.Entry<NucleotideSequenceDocument, String> traceToNamePart : tracesToNameParts.entrySet()) {
            String namePart = traceToNamePart.getValue();

            NucleotideSequenceDocument barcode = null;

            for (Map.Entry<NucleotideSequenceDocument, String> barcodeToNamePart : barcodesToNameParts.entrySet())
                if (barcodeToNamePart.getValue().equals(namePart))
                    barcode = barcodeToNamePart.getKey();

            if (barcode == null)
                throw new DocumentOperationException("Could not match traces to barcodes: " +
                                                     "Trace '" + traceToNamePart.getKey().getName() + "' " +
                                                     "has no associated barcode.");

            result.get(barcode).add(traceToNamePart.getKey());
        }

        return result;
    }

    /**
     * Maps documents to the part of their respective name that is used for grouping.
     *
     * @param documents Documents.
     * @param separator Separator used to extract the document name parts.
     * @param i Index used to extract the document name parts;
     * @return Map of documents to the part of their respective name that is used for grouping.
     * @throws DocumentOperationException
     */
    private Map<NucleotideSequenceDocument, String>
    mapDocumentToDocumentNamePart(List<NucleotideSequenceDocument> documents, String separator, int i) {
        Map<NucleotideSequenceDocument, String> result = new HashMap<NucleotideSequenceDocument, String>();

        for (NucleotideSequenceDocument document : documents) {
            String documentName = document.getName();

            result.put(document, splitAndReturnIth(documentName, separator, i));
        }

        return result;
    }

    /**
     * Equivalent to s.split(sep)[i].
     *
     * @param s String.
     * @param sep Separator.
     * @param i Index.
     * @return s.split(sep)[i].
     */
    private String splitAndReturnIth(String s, String sep, int i) {
        return s.split(sep)[i];
    }

    /**
     * Returns the n-th ordinal.
     *
     * @param n N.
     * @return N-th ordinal.
     */
    private String getOrdinalString(int n) {
        String nString = String.valueOf(n);

        String nAbsString = String.valueOf(Math.abs(n));

        int nAbsStringLength = nAbsString.length();

        if (nAbsStringLength > 1 && Integer.valueOf(nAbsString.charAt(nAbsStringLength - 2)) == 1)
            return nString + "th";

        switch (Integer.valueOf(nAbsString.substring(nAbsStringLength - 1, nAbsStringLength))) {
            case 1:
                return nString + "st";
            case 2:
                return nString + "nd";
            case 3:
                return nString + "rd";
            default:
                return nString + "th";
        }
    }
}