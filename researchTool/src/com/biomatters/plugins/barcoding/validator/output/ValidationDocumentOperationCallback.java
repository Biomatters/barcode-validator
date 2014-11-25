package com.biomatters.plugins.barcoding.validator.output;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.validation.ValidationCallback;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.ValidationResult;
import com.biomatters.plugins.barcoding.validator.validation.results.ResultFact;
import com.biomatters.plugins.barcoding.validator.validation.trimming.SequenceTrimmer;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link com.biomatters.plugins.barcoding.validator.validation.ValidationCallback} that adds results to
 * {@link com.biomatters.geneious.publicapi.plugin.DocumentOperation.OperationCallback}.  Can be used to produce a
 * {@link com.biomatters.plugins.barcoding.validator.output.ValidationOutputRecord}
 *
 * @author Matthew Cheung
 *         Created on 30/09/14 4:17 PM
 */
public class ValidationDocumentOperationCallback implements ValidationCallback {

    public Map<PluginDocument, URN> sequenceURNMap = new HashMap<PluginDocument, URN>();

    private DocumentOperation.OperationCallback operationCallback;
    private boolean selectResultDocs = false;
    private ValidationOutputRecord outputRecord;


    public ValidationDocumentOperationCallback(DocumentOperation.OperationCallback operationCallback, boolean selectResultDocs) {
        this.operationCallback = operationCallback;
        this.selectResultDocs = selectResultDocs;
        this.outputRecord = new ValidationOutputRecord();
    }

    private URN saveDocumentAndGetUrn(PluginDocument pluginDocument, ProgressListener progressListener) throws DocumentOperationException {
        return saveDocument(pluginDocument, progressListener).annotatedPluginDocument.getURN();
    }

    private <T extends PluginDocument> AnnotatedAndPluginDocument<T> saveDocument(T pluginDocument, ProgressListener progressListener) throws DocumentOperationException {
        AnnotatedPluginDocument apd = operationCallback.addDocument(pluginDocument, !selectResultDocs, progressListener);
        if(!apd.getDocumentClass().isAssignableFrom(pluginDocument.getClass())) {
            throw new IllegalStateException("Saved document is of different type than original (" +
                    pluginDocument.getClass() + ", " + apd.getDocumentClass() + ")");
        }
        // Ignore warning because we already check using getDocumentClass()
        //noinspection unchecked
        return new AnnotatedAndPluginDocument<T>(apd, (T)apd.getDocumentOrNull());
    }

    private class AnnotatedAndPluginDocument<T extends PluginDocument> {
        private AnnotatedPluginDocument annotatedPluginDocument;
        private T pluginDocument;

        public AnnotatedAndPluginDocument(AnnotatedPluginDocument annotatedPluginDocument, T pluginDocument) {
            this.annotatedPluginDocument = annotatedPluginDocument;
            this.pluginDocument = pluginDocument;
        }
    }

    @Override
    public void setInputs(NucleotideSequenceDocument barcodeSequence, List<NucleotideGraphSequenceDocument> traces, ProgressListener progressListener) throws DocumentOperationException {
        CompositeProgressListener compositeProgress = new CompositeProgressListener(progressListener, (barcodeSequence == null ? 0 : 1) + traces.size());
        if(barcodeSequence != null) {
            compositeProgress.beginSubtask();
            AnnotatedPluginDocument apd = DocumentUtilities.getAnnotatedPluginDocumentThatContains(barcodeSequence);
            outputRecord.barcodeSequenceUrn = apd != null ? apd.getURN() :
                    saveDocumentAndGetUrn(barcodeSequence, compositeProgress);
        }
        for (NucleotideGraphSequenceDocument trace : traces) {
            compositeProgress.beginSubtask();
            AnnotatedPluginDocument apd = DocumentUtilities.getAnnotatedPluginDocumentThatContains(trace);
            if (apd != null) {
                outputRecord.addTraceDocumentUrns(apd.getName(), apd.getURN());
            } else {
                AnnotatedAndPluginDocument<NucleotideGraphSequenceDocument> saved = saveDocument(trace, compositeProgress);
                AnnotatedPluginDocument annotatedPluginDocument = saved.annotatedPluginDocument;
                outputRecord.addTraceDocumentUrns(annotatedPluginDocument.getName(), annotatedPluginDocument.getURN());
            }
        }
    }

    @Override
    public List<NucleotideGraphSequenceDocument> addTrimmedTraces(List<NucleotideGraphSequenceDocument> traces, ProgressListener progressListener) throws DocumentOperationException {
        List<NucleotideGraphSequenceDocument> results = new ArrayList<NucleotideGraphSequenceDocument>();

        CompositeProgressListener savingProgress = new CompositeProgressListener(progressListener, 2 * traces.size());
        for (NucleotideGraphSequenceDocument trimmedTrace : traces) {
            String name = trimmedTrace.getName();

            savingProgress.beginSubtask();
            ((DefaultSequenceDocument)trimmedTrace).setName(name + " " + SequenceTrimmer.ANNOTATION_SUFFIX);
            saveDocument(trimmedTrace, savingProgress);

            savingProgress.beginSubtask();
            ((DefaultSequenceDocument)trimmedTrace).setName(name + " " + SequenceTrimmer.TRIMMED_SUFFIX);
            AnnotatedAndPluginDocument<NucleotideGraphSequenceDocument> saved = saveDocument(SequenceTrimmer.trimSequenceUsingAnnotations(trimmedTrace), savingProgress);
            NucleotideGraphSequenceDocument document = saved.pluginDocument;
            AnnotatedPluginDocument doc = saved.annotatedPluginDocument;
            results.add(document);
            sequenceURNMap.put(document, doc.getURN());
            outputRecord.addTrimmedDocumentUrns(doc.getName(), doc.getURN());
        }
        return results;
    }

    @Override
    public SequenceAlignmentDocument addAssembly(SequenceAlignmentDocument contigAssembly, ProgressListener progressListener) throws DocumentOperationException {
        AnnotatedAndPluginDocument<SequenceAlignmentDocument> saved = saveDocument(contigAssembly, progressListener);
        outputRecord.assemblyUrn = saved.annotatedPluginDocument.getURN();
        return saved.pluginDocument;
    }

    @Override
    public NucleotideGraphSequenceDocument addConsensus(NucleotideGraphSequenceDocument consensusSequence, ProgressListener progressListener) throws DocumentOperationException {
        AnnotatedAndPluginDocument<NucleotideGraphSequenceDocument> saved = saveDocument(consensusSequence, progressListener);
        outputRecord.consensusUrn = saved.annotatedPluginDocument.getURN();
        consensusSequence = saved.pluginDocument;
        sequenceURNMap.put(consensusSequence, outputRecord.consensusUrn);
        return consensusSequence;
    }

    @Override
    public void addValidationResult(ValidationOptions options, ValidationResult validationResult, ProgressListener progressListener) throws DocumentOperationException {
        for (Map.Entry<SequenceDocument, ResultFact> entry : validationResult.getFacts().entrySet()) {
            ResultFact fact = entry.getValue();
            SequenceDocument seq = entry.getKey();
            AnnotatedPluginDocument apd = DocumentUtilities.getAnnotatedPluginDocumentThatContains(seq);
            assert apd != null : "Input sequence should have been saved to the database";
            outputRecord.addValidationResult(apd.getURN(), new RecordOfValidationResult(options, validationResult.isPassed(), fact));
        }
    }


    public ValidationOutputRecord getRecord() {
        return outputRecord;
    }
}
