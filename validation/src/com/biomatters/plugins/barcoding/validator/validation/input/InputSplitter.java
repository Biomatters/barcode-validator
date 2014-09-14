package com.biomatters.plugins.barcoding.validator.validation.input;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.validation.utilities.ImportUtilities;
import com.biomatters.plugins.barcoding.validator.validation.input.map.BarcodesToTracesMapperFactory;
import com.biomatters.plugins.barcoding.validator.validation.input.map.BarcodesToTracesMapperOptions;

import java.util.List;
import java.util.Map;

/**
 * @author Gen Li
 *         Created on 3/09/14 5:22 PM
 */
public class InputSplitter {
    public static Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>
    split(List<String> traceFilePaths, List<String> barcodeFilePaths, BarcodesToTracesMapperOptions options)
            throws DocumentOperationException {
        List<NucleotideSequenceDocument> traces = ImportUtilities.importTraces(traceFilePaths);
        List<NucleotideSequenceDocument> barcodes = ImportUtilities.importBarcodes(barcodeFilePaths);

        return BarcodesToTracesMapperFactory.getBarcodesToTracesMapper(options).map(barcodes, traces);
    }
}