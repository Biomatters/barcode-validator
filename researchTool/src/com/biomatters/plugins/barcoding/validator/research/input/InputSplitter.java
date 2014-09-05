package com.biomatters.plugins.barcoding.validator.research.input;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.research.common.ImportUtilities;
import com.biomatters.plugins.barcoding.validator.research.input.map.InputSplitterTraceToBarcodeMapperFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Gen Li
 *         Created on 3/09/14 5:22 PM
 */
public class InputSplitter {
    public static Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>> split(InputSplitterOptions options)
            throws DocumentOperationException {
        List<NucleotideSequenceDocument> traces = ImportUtilities.importTraces(options.getTraceFilePaths());
        List<NucleotideSequenceDocument> barcodes = ImportUtilities.importBarcodes(options.getBarcodeFilePaths());

        return InputSplitterTraceToBarcodeMapperFactory.getTraceToBarcodeMapper(options).map(traces, barcodes);
    }
}