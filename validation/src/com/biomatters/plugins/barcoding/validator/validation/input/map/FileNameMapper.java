package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Algorithm for mapping barcodes to traces via file names.
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
     * @return Map of barcodes to traces.
     * @throws DocumentOperationException
     */
    @Override
    public Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>
    map(List<NucleotideSequenceDocument> barcodes, List<NucleotideSequenceDocument> traces)
            throws DocumentOperationException {
        try {
            /* Map documents to, the part of the name of each document used for the mapping. */
            Map<NucleotideSequenceDocument, String> tracesToNameParts =
                    mapDocumentToPartOfName(traces, traceSeparator, traceNamePart);
            Map<NucleotideSequenceDocument, String> barcodesToNameParts =
                    mapDocumentToPartOfName(barcodes, barcodeSeparator, barcodeNamePart);

            /* Map. */
            return map(tracesToNameParts, barcodesToNameParts);
        } catch (NoMatchException e) {
            throw new DocumentOperationException(e.getMessage() + "\n\n" +
                                                 "No matches searching for " +
                                                 "<strong>" + e.getSearchString() + "</strong> in " +
                                                 NamePartOption.getLabelForPartNumber(barcodeNamePart) +
                                                 " part of barcode names separated by " +
                                                 NameSeparatorOption.getLabelForPartNumber(barcodeSeparator) + ".",
                                                 e);
        }
    }

    /**
     * Maps barcodes to traces.
     *
     * @param tracesToNameParts Map of traces to, the part of the name of each trace used for the mapping.
     * @param barcodesToNameParts Map of barcodes to, the part of the name of each barcode used for the mapping.
     * @return Map of barcodes to traces.
     * @throws DocumentOperationException
     */
    private static Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>
    map(Map<NucleotideSequenceDocument, String> tracesToNameParts,
        Map<NucleotideSequenceDocument, String> barcodesToNameParts)
            throws NoMatchException {
        Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>> result
                = new HashMap<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>();

        for (NucleotideSequenceDocument barcode : barcodesToNameParts.keySet()) {
            result.put(barcode, new ArrayList<NucleotideSequenceDocument>());
        }

        /* Match traces to barcodes. */
        for (Map.Entry<NucleotideSequenceDocument, String> traceToNamePart : tracesToNameParts.entrySet()) {
            String namePart = traceToNamePart.getValue();

            NucleotideSequenceDocument barcode = null;

            for (Map.Entry<NucleotideSequenceDocument, String> barcodeToNamePart : barcodesToNameParts.entrySet()) {
                if (barcodeToNamePart.getValue().equals(namePart)) {
                    barcode = barcodeToNamePart.getKey();
                }
            }

            if (barcode == null) {
                throw new NoMatchException(traceToNamePart.getKey().getName(), traceToNamePart.getValue());
            }

            result.get(barcode).add(traceToNamePart.getKey());
        }

        return result;
    }

    private static class NoMatchException extends Exception {
        private String searchString;

        private NoMatchException(String fullName, String searchString) {
            super("Trace <strong>" + fullName + "</strong> has no associated barcode.");
            this.searchString = searchString;
        }

        public String getSearchString() {
            return searchString;
        }
    }

    /**
     * Maps documents to, the part of the name of each document used for the mapping. Given name n, separator s, and
     * index i, name part = n.split(s)[i].
     *
     * @param documents Documents.
     * @param separator Separator.
     * @param i Index.
     * @return Map of documents to, the part of the name of each document used for the mapping.
     * @throws DocumentOperationException
     */
    private static Map<NucleotideSequenceDocument, String>
    mapDocumentToPartOfName(List<NucleotideSequenceDocument> documents, String separator, int i)
            throws DocumentOperationException {
        Map<NucleotideSequenceDocument, String> result = new HashMap<NucleotideSequenceDocument, String>();

        for (NucleotideSequenceDocument document : documents) {
            String documentName = document.getName();

            try {
                result.put(document, splitAndReturnNth(documentName, separator, i));
            } catch (IndexOutOfBoundsException e) {
                throw new DocumentOperationException("Could not get " + getOrdinalString(i + 1) + " substring of '" +
                                                     documentName + "' separated by '" + separator + "'.",
                                                     e);
            }
        }

        return result;
    }

    /**
     * Equivalent to s.split(sep)[n].
     *
     * @param s
     * @param sep
     * @param n
     * @return s.split(sep)[i].
     */
    private static String splitAndReturnNth(String s, String sep, int n) throws IndexOutOfBoundsException {
        return s.split(sep)[n];
    }

    /**
     * Returns the nth ordinal.
     *
     * @param n
     * @return Nth ordinal.
     */
    static String getOrdinalString(int n) {
        String nString = Integer.toString(n);

        String nAbsString = String.valueOf(Math.abs(n));

        int nAbsStringLength = nAbsString.length();

        if (nAbsStringLength > 1 && Character.digit(nAbsString.charAt(nAbsStringLength - 2), 10) == 1) {
            return nString + "th";
        }

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