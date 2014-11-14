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
import com.biomatters.plugins.barcoding.validator.validation.trimming.SequenceTrimmer;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link com.biomatters.plugins.barcoding.validator.validation.ValidationCallback} that adds results to
 * {@link com.biomatters.geneious.publicapi.plugin.DocumentOperation.OperationCallback}.  Can be used to produce a
 * {@link com.biomatters.plugins.barcoding.validator.output.ValidationOutputRecord}
 *
 * @author Matthew Cheung
 *         Created on 30/09/14 4:17 PM
 */
public class ValidationDocumentOperationCallback implements ValidationCallback {

    private DocumentOperation.OperationCallback operationCallback;
    private boolean selectResultDocs = false;
    private ValidationOutputRecord outputRecord;


    public ValidationDocumentOperationCallback(DocumentOperation.OperationCallback operationCallback, boolean selectResultDocs) {
        this.operationCallback = operationCallback;
        this.selectResultDocs = selectResultDocs;
        this.outputRecord = new ValidationOutputRecord();
    }

    private URN saveDocumentAndGetUrn(PluginDocument pluginDocument, ProgressListener progressListener) throws DocumentOperationException {
        return saveDocument(pluginDocument, progressListener).getURN();
    }

    private AnnotatedPluginDocument saveDocument(PluginDocument pluginDocument, ProgressListener progressListener) throws DocumentOperationException {
        return operationCallback.addDocument(pluginDocument, !selectResultDocs, progressListener);
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
                AnnotatedPluginDocument annotatedPluginDocument = saveDocument(trace, progressListener);
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
            ((DefaultSequenceDocument)trimmedTrace).setName(name + SequenceTrimmer.ANNOTATION_SUFFIX);
            saveDocument(trimmedTrace, savingProgress);
            ((DefaultSequenceDocument)trimmedTrace).setName(name);

            savingProgress.beginSubtask();
            ((DefaultSequenceDocument)trimmedTrace).setName(name + SequenceTrimmer.TRIMMED_SUFFIX);
            AnnotatedPluginDocument doc = saveDocument(SequenceTrimmer.trimSequenceUsingAnnotations(trimmedTrace), savingProgress);
            ((DefaultSequenceDocument)trimmedTrace).setName(name);
            if(!NucleotideGraphSequenceDocument.class.isAssignableFrom(doc.getDocumentClass())) {
                throw new IllegalStateException("Saving NucleotideGraphSequenceDocument to database created " + doc.getDocumentClass().getSimpleName());
            }
            results.add((NucleotideGraphSequenceDocument)doc.getDocument());
            outputRecord.getTrimmedDocumentUrns(doc.getName(), doc.getURN());
        }
        return results;
    }

    @Override
    public void addAssembly(SequenceAlignmentDocument contigAssembly, ProgressListener progressListener) throws DocumentOperationException {
        outputRecord.assemblyUrn = saveDocumentAndGetUrn(contigAssembly, progressListener);
    }

    @Override
    public void addConsensus(SequenceDocument consensusSequence, ProgressListener progressListener) throws DocumentOperationException {
        outputRecord.consensusUrn = saveDocumentAndGetUrn(consensusSequence, progressListener);
    }

    @Override
    public void addValidationResult(ValidationOptions options, ValidationResult validationResult, ProgressListener progressListener) throws DocumentOperationException {
        List<PluginDocument> docsToAddToResults = validationResult.getIntermediateDocumentsToAddToResults();
        CompositeProgressListener resultAddingProgress = new CompositeProgressListener(progressListener, docsToAddToResults.size());

        List<URN> supplementaryDocUrns = new ArrayList<URN>();
        for (PluginDocument docToAdd : docsToAddToResults) {
            resultAddingProgress.beginSubtask();
            supplementaryDocUrns.add(saveDocumentAndGetUrn(docToAdd, resultAddingProgress));
        }

        outputRecord.validationRecords.add(
                new RecordOfValidationResult(options, validationResult.isPassed(), validationResult.getMessage(), validationResult.getEntry(),
                        supplementaryDocUrns)
        );
    }

    public ValidationOutputRecord getRecord() {
        return outputRecord;
    }
}
