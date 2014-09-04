package com.biomatters.plugins.barcoding.validator.research.input;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.DefaultSequenceListDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentImportException;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.PluginUtilities;
import jebl.util.ProgressListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Gen Li
 *         Created on 3/09/14 5:22 PM
 */
public class InputSplitter {
    public static Map<NucleotideSequenceDocument, NucleotideSequenceDocument> split(InputSplitterOptions options)
            throws DocumentOperationException {
        List<NucleotideSequenceDocument> traces = importTraces(options.getTraceFilePaths());
        List<NucleotideSequenceDocument> barcodes = importBarcodes(options.getBarcodeFilePaths());

        return null;
    }

    private static List<NucleotideSequenceDocument>
    importTraces(List<String> traceFilePaths) throws DocumentOperationException {
        List<NucleotideSequenceDocument> traces = new ArrayList<NucleotideSequenceDocument>();
        try {
            for (String filePath : traceFilePaths) {
                for (AnnotatedPluginDocument importedDocument :
                        PluginUtilities.importDocuments(new File(filePath), ProgressListener.EMPTY)) {
                    if (!DefaultNucleotideGraphSequence.class.isAssignableFrom(importedDocument.getDocumentClass())) {
                        throw new DocumentOperationException("Could not import traces: " +
                                                             "Document of invalid type imported, " +
                                                             "expected type: " +
                                                                "<? extends DefaultNucleotideGraphSequence>, " +
                                                             "actual type: " +
                                                                importedDocument.getDocumentClass().getSimpleName());
                    }
                    traces.add((NucleotideSequenceDocument)importedDocument.getDocument());
                }
            }
        } catch (IOException e) {
            throw new DocumentOperationException("Could not import traces: " + e.getMessage(), e);
        } catch (DocumentImportException e) {
            throw new DocumentOperationException("Could not import traces: " + e.getMessage(), e);
        }
        return traces;
    }

    private static List<NucleotideSequenceDocument>
    importBarcodes(List<String> barcodeFilePaths) throws DocumentOperationException {
        List<NucleotideSequenceDocument> barcodes = new ArrayList<NucleotideSequenceDocument>();
        try {
            for (String filePath : barcodeFilePaths) {
                for (AnnotatedPluginDocument importedDocument :
                        PluginUtilities.importDocuments(new File(filePath), ProgressListener.EMPTY)) {
                    if (!DefaultSequenceListDocument.class.isAssignableFrom(importedDocument.getDocumentClass())) {
                        throw new DocumentOperationException("Could not import barcodes: " +
                                                             "Document of invalid type imported, " +
                                                             "expected type: " +
                                                                "<? extends DefaultSequenceListDocument, " +
                                                             "actual type: " +
                                                                importedDocument.getDocumentClass().getSimpleName());
                    }
                    barcodes.addAll(((DefaultSequenceListDocument)importedDocument.getDocument()).getNucleotideSequences());
                }
            }
        } catch (IOException e) {
            throw new DocumentOperationException("Could not import barcodes: " + e.getMessage(), e);
        } catch (DocumentImportException e) {
            throw new DocumentOperationException("Could not import barcodes: " + e.getMessage(), e);
        }
        return barcodes;
    }
}