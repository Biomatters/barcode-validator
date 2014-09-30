package com.biomatters.plugins.barcoding.validator.validation.input;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.validation.input.map.BarcodesToTracesMapperFactory;
import com.biomatters.plugins.barcoding.validator.validation.input.map.BarcodesToTracesMapperOptions;
import com.biomatters.plugins.barcoding.validator.validation.utilities.ImportUtilities;

import java.util.List;
import java.util.Map;

/**
 * Functionality for processing inputs to the Barcode Validator. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 3/09/14 5:22 PM
 */
public class Input {
    private Input() {
    }

    /**
     * Imports/maps traces and/to barcodes.
     *
     * @param traceFilePaths Trace source file paths.
     * @param barcodeFilePaths Barcode source file paths.
     * @param options Method and associated settings for the mapping.
     * @return Map of barcodes to traces.
     * @throws DocumentOperationException
     */
    public static Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>>
    processInputs(List<String> traceFilePaths, List<String> barcodeFilePaths, BarcodesToTracesMapperOptions options)
            throws DocumentOperationException {
        /* Import documents. */
        List<NucleotideGraphSequenceDocument> traces = ImportUtilities.importTraces(traceFilePaths);
        List<NucleotideSequenceDocument> barcodes = ImportUtilities.importBarcodes(barcodeFilePaths);

        /* Map barcodes to traces and return result. */
        return BarcodesToTracesMapperFactory.getBarcodesToTracesMapper(options).map(barcodes, traces);
    }
}