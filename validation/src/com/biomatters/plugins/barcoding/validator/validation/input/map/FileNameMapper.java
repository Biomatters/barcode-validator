package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps barcodes to traces via file name matching.
 *
 * @author Gen Li
 *         Created on 4/09/14 12:40 PM
 */
public class FileNameMapper extends BarcodesToTracesMapper {
    private int traceNamePart;
    private int barcodeNamePart;

    private String traceSeparator;
    private String barcodeSeparator;

    public FileNameMapper(String traceSeparator, int traceNamePart, String barcodeSeparator, int barcodeNamePart) {
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
    public Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> map(List<NucleotideSequenceDocument> barcodes,
                                                                                      List<NucleotideGraphSequenceDocument> traces)
            throws DocumentOperationException {
        try {
            /* Map documents to part of their names used for mapping. */
            Map<NucleotideGraphSequenceDocument, String> tracesToNameParts = mapTracesToPartOfName(traces, traceSeparator, traceNamePart);
            Map<NucleotideSequenceDocument, String> barcodesToNameParts = mapBarcodesToPartOfName(barcodes, barcodeSeparator, barcodeNamePart);

            /* Map. */
            return map(tracesToNameParts, barcodesToNameParts);
        } catch (NoMatchException e) {
            throw new DocumentOperationException(
                    e.getMessage() + "\n\n" +
                    "No matches searching for <strong>" + e.getSearchString() + "</strong> in " +
                    NamePartOption.getLabelForPartNumber(barcodeNamePart) + " part of barcode names separated by " +
                    NameSeparatorOption.getLabelForPartNumber(barcodeSeparator) + ".",
                    e
            );
        }
    }

    /**
     * Maps barcodes to traces.
     *
     * @param tracesToNameParts Map of traces to part of their names used for mapping.
     * @param barcodesToNameParts Map of barcodes to part of their names used for mapping.
     * @return Map of barcodes to traces.
     * @throws DocumentOperationException
     */
    private static Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> map(Map<NucleotideGraphSequenceDocument, String> tracesToNameParts,
                                                                                              Map<NucleotideSequenceDocument, String> barcodesToNameParts)
            throws NoMatchException {
        Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> result =
                new HashMap<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>>();

        for (NucleotideSequenceDocument barcode : barcodesToNameParts.keySet()) {
            result.put(barcode, new ArrayList<NucleotideGraphSequenceDocument>());
        }

        /* Map barcodes to traces. */
        for (Map.Entry<NucleotideGraphSequenceDocument, String> traceToNamePart : tracesToNameParts.entrySet()) {
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
     * Maps documents to part of their names used for mapping. Given name n, separator s, and index i:
     * name part = n.split(s)[i].
     *
     * @param documents Documents.
     * @param separator Separator.
     * @param i Index.
     * @return Map of documents to part of their names used for mapping.
     * @throws DocumentOperationException
     */
    private static Map<NucleotideSequenceDocument, String> mapDocumentsToPartOfName(List<NucleotideSequenceDocument> documents, String separator, int i)
            throws DocumentOperationException {
        Map<NucleotideSequenceDocument, String> result = new HashMap<NucleotideSequenceDocument, String>();

        for (NucleotideSequenceDocument document : documents) {
            String documentName = document.getName();
            try {
                result.put(document, splitAndReturnNth(documentName, separator, i));
            } catch (IndexOutOfBoundsException e) {
                throw new DocumentOperationException(
                        "Could not get " + getOrdinalString(i + 1) + " substring of '" + documentName +
                        "' separated by '" + separator + "'.",
                        e
                );
            }
        }

        return result;
    }

    /**
     * Maps traces to part of their names used for mapping. Given name n, separator s, and index i:
     * name part = n.split(s)[i].
     *
     * @param traces Traces.
     * @param separator Separator.
     * @param i Index.
     * @return Map of traces to part of their names used for mapping.
     * @throws DocumentOperationException
     */
    private static Map<NucleotideGraphSequenceDocument, String> mapTracesToPartOfName(List<NucleotideGraphSequenceDocument> traces, String separator, int i)
            throws DocumentOperationException {
        Map<NucleotideSequenceDocument, String> resultNSDs = mapDocumentsToPartOfName(new ArrayList<NucleotideSequenceDocument>(traces), separator, i);
        Map<NucleotideGraphSequenceDocument, String> result = new HashMap<NucleotideGraphSequenceDocument, String>();

        for (Map.Entry<NucleotideSequenceDocument, String> traceNSDToNamePart : resultNSDs.entrySet()) {
            result.put((NucleotideGraphSequenceDocument)traceNSDToNamePart.getKey(), traceNSDToNamePart.getValue());
        }

        return result;
    }

    /**
     * Maps barcodes to part of their names used for mapping. Given name n, separator s, and index i:
     * name part = n.split(s)[i].
     *
     * @param barcodes Barcodes.
     * @param separator Separator.
     * @param i Index.
     * @return Map of barcodes to, the part of the name of each barcode used for the mapping.
     * @throws DocumentOperationException
     */
    private static Map<NucleotideSequenceDocument, String> mapBarcodesToPartOfName(List<NucleotideSequenceDocument> barcodes, String separator, int i)
            throws DocumentOperationException {
        return mapDocumentsToPartOfName(barcodes, separator, i);
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