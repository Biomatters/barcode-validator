package com.biomatters.plugins.barcoding.validator.validation.input;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.validation.input.map.BarcodesToTracesMapperFactory;
import com.biomatters.plugins.barcoding.validator.validation.input.map.BarcodesToTracesMapperOptions;
import com.biomatters.plugins.barcoding.validator.validation.utilities.ImportUtilities;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import java.util.List;
import java.util.Map;

/**
 * Functionality for processing Barcode Validator inputs. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 3/09/14 5:22 PM
 */
public class InputProcessor {
    private InputProcessor() {
    }

    /**
     * Imports/maps traces and/to barcodes.
     *
     * @param traceFilePaths Trace source file paths.
     * @param barcodeFilePaths Barcode source file paths.
     * @param options Method and associated settings for the mapping.
     * @param operationCallback
     * @param progressListener
     * @return Map of barcodes to traces.
     * @throws DocumentOperationException
     */
    public static Map<AnnotatedPluginDocument, List<AnnotatedPluginDocument>> run(List<String> traceFilePaths,
                                                                                  List<String> barcodeFilePaths,
                                                                                  BarcodesToTracesMapperOptions options,
                                                                                  DocumentOperation.OperationCallback operationCallback,
                                                                                  ProgressListener progressListener) throws DocumentOperationException {
        CompositeProgressListener inputProcessingProgress = new CompositeProgressListener(progressListener, 3);

        /* Import documents. */
        inputProcessingProgress.beginSubtask("Importing traces...");
        List<AnnotatedPluginDocument> traces = ImportUtilities.importTraces(traceFilePaths, operationCallback, inputProcessingProgress);

        inputProcessingProgress.beginSubtask("Importing barcodes...");
        List<AnnotatedPluginDocument> barcodes = ImportUtilities.importBarcodes(barcodeFilePaths, operationCallback, inputProcessingProgress);

        /* Map barcodes to traces and return result. */
        inputProcessingProgress.beginSubtask("Mapping traces to barcodes...");
        return BarcodesToTracesMapperFactory.getBarcodesToTracesMapper(options).map(barcodes, traces);
    }
}