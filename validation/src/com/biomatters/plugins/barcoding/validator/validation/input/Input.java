package com.biomatters.plugins.barcoding.validator.validation.input;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.validation.utilities.ImportUtilities;
import com.biomatters.plugins.barcoding.validator.validation.input.map.BarcodesToTracesMapperFactory;
import com.biomatters.plugins.barcoding.validator.validation.input.map.BarcodesToTracesMapperOptions;

import java.util.List;
import java.util.Map;

/**
 * Functionality for processing validation pipeline inputs. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 3/09/14 5:22 PM
 */
public class Input {
    private Input() {
    }

    /**
     * Imports traces and barcodes from supplied source files and groups the traces to the barcodes.
     *
     * @param traceFilePaths Paths of trace source files.
     * @param barcodeFilePaths Paths of barcode source files.
     * @param options Configuration details for the mapping of traces to barcodes.
     * @return Mapping of barcodes to traces.
     * @throws DocumentOperationException
     */
    public static Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>
    processInputs(List<String> traceFilePaths, List<String> barcodeFilePaths, BarcodesToTracesMapperOptions options)
            throws DocumentOperationException {
        try {
            List<NucleotideSequenceDocument> traces = ImportUtilities.importTraces(traceFilePaths);
            List<NucleotideSequenceDocument> barcodes = ImportUtilities.importBarcodes(barcodeFilePaths);

            return BarcodesToTracesMapperFactory.getBarcodesToTracesMapper(options).map(barcodes, traces);
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not process inputs: " + e.getMessage(), e);
        }
    }
}