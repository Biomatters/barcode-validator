package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps barcodes to traces via the matching of file names.
 *
 * @author Gen Li
 *         Created on 4/09/14 12:40 PM
 */
public class FileNameMapper extends BarcodeToTraceMapper {
    private String traceSeparator;
    private String barcodeSeparator;

    private int partOfTraceName;
    private int partOfBarcodeName;

    public FileNameMapper(String traceSeparator, int partOfTraceName, String barcodeSeparator, int partOfBarcodeName) {
        setTraceSeparator(traceSeparator);
        setPartOfTraceName(partOfTraceName);
        setBarcodeSeparator(barcodeSeparator);
        setPartOfBarcodeName(partOfBarcodeName);
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
    public Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> map(Collection<AnnotatedPluginDocument> barcodes, Collection<AnnotatedPluginDocument> traces) throws DocumentOperationException {
        if (barcodes == null) {
            throw new IllegalArgumentException("barcodes cannot be null.");
        }

        if (barcodes.contains(null)) {
            throw new IllegalArgumentException("Barcode documents cannot be null.");
        }

        if (traces == null) {
            throw new IllegalArgumentException("traces cannot be null.");
        }

        if (traces.contains(null)) {
            throw new IllegalArgumentException("Trace documents cannot be null.");
        }

        return map(mapBarcodesToNameParts(barcodes), mapNamePartsToTraces(traces));
    }

    public void setTraceSeparator(String traceSeparator) {
        this.traceSeparator = traceSeparator;
    }

    public String getTraceSeparator() {
        return traceSeparator;
    }

    public void setBarcodeSeparator(String barcodeSeparator) {
        this.barcodeSeparator = barcodeSeparator;
    }

    public String getBarcodeSeparator() {
        return barcodeSeparator;
    }

    public void setPartOfTraceName(int partOfTraceName) {
        this.partOfTraceName = partOfTraceName;
    }

    public int getPartOfTraceName() {
        return partOfTraceName;
    }

    public void setPartOfBarcodeName(int partOfBarcodeName) {
        this.partOfBarcodeName = partOfBarcodeName;
    }

    public int getPartOfBarcodeName() {
        return partOfBarcodeName;
    }

    private Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> map(Map<AnnotatedPluginDocument, String> barcodesToNameParts,
                                                                           Multimap<String, AnnotatedPluginDocument> namePartsToTraces) throws DocumentOperationException {
        Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> barcodesToTraces = ArrayListMultimap.create();

        for (Map.Entry<AnnotatedPluginDocument, String> barcodeToNamePart: barcodesToNameParts.entrySet()) {
            barcodesToTraces.putAll(barcodeToNamePart.getKey(), namePartsToTraces.get(barcodeToNamePart.getValue()));
        }

        Collection<AnnotatedPluginDocument> tracesWithoutAnAssociatedBarcode = getTracesWithoutAnAssociatedBarcode(namePartsToTraces.values(), barcodesToTraces.values());

        if (!tracesWithoutAnAssociatedBarcode.isEmpty()) {
            throw new DocumentOperationException("Unmapped traces: " + StringUtilities.join(",", tracesWithoutAnAssociatedBarcode));
        }

        return barcodesToTraces;
    }

    private Map<AnnotatedPluginDocument, String> mapBarcodesToNameParts(Collection<AnnotatedPluginDocument> barcodes) throws DocumentOperationException {
        if (barcodeSeparator == null || barcodeSeparator.isEmpty()) {
            throw new IllegalStateException("barcodeSeparator cannot be " + (barcodeSeparator == null ? "null" : "empty") + ".");
        }

        if (partOfBarcodeName < 0) {
            throw new IllegalStateException("partOfBarcodeName cannot be less than 0.");
        }

        Map<AnnotatedPluginDocument, String> barcodesToNameParts = new HashMap<AnnotatedPluginDocument, String>();

        for (AnnotatedPluginDocument barcode : barcodes) {
            String barcodeNameSplit[] = barcode.getName().split(barcodeSeparator);

            if (partOfBarcodeName >= barcodeNameSplit.length) {
                throw new DocumentOperationException(
                        "Could not retrieve the " + getOrdinalString(partOfBarcodeName) + " string from \"" + barcode.getName() + "\" split by \"" + barcodeSeparator + "\"." +
                        "\"" + barcode.getName() + "\" split by \"" + barcodeSeparator + "\" results in " + barcodeNameSplit.length + " strings."
                );
            }

            barcodesToNameParts.put(barcode, barcodeNameSplit[partOfBarcodeName]);
        }

        return barcodesToNameParts;
    }

    private Multimap<String, AnnotatedPluginDocument> mapNamePartsToTraces(Collection<AnnotatedPluginDocument> traces) throws DocumentOperationException {
        if (traceSeparator == null || traceSeparator.isEmpty()) {
            throw new IllegalStateException("separator cannot be " + (traceSeparator == null ? "null" : "empty") + ".");
        }

        if (partOfTraceName < 0) {
            throw new IllegalStateException("partOfTraceName cannot be less than 0.");
        }

        Multimap<String, AnnotatedPluginDocument> namePartsToTraces = ArrayListMultimap.create();

        for (AnnotatedPluginDocument trace : traces) {
            String traceNameSplit[] = trace.getName().split(traceSeparator);

            if (partOfTraceName >= traceNameSplit.length) {
                throw new DocumentOperationException(
                        "Could not retrieve the " + getOrdinalString(partOfTraceName) + " string from \"" + trace.getName() + "\" split by \"" + traceSeparator + "\"." +
                        "\"" + trace.getName() + "\" split by \"" + traceSeparator + "\" results in " + traceNameSplit.length + " strings."
                );
            }

            namePartsToTraces.put(traceNameSplit[partOfTraceName], trace);
        }

        return namePartsToTraces;
    }

    /**
     * Returns the nth ordinal.
     *
     * @param n
     * @return Nth ordinal.
     */
    private static String getOrdinalString(int n) {
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