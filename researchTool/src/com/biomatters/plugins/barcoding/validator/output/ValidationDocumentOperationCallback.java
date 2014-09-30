package com.biomatters.plugins.barcoding.validator.output;

import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.validation.ValidationResult;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link com.biomatters.plugins.barcoding.validator.output.ValidationCallback} that adds results to
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

    private URN saveDocument(PluginDocument pluginDocument, ProgressListener progressListener) throws DocumentOperationException {
        return operationCallback.addDocument(pluginDocument, !selectResultDocs, progressListener).getURN();
    }

    @Override
    public void setInputs(NucleotideSequenceDocument barcodeSequence, List<NucleotideGraphSequenceDocument> traces, ProgressListener progressListener) throws DocumentOperationException {
        CompositeProgressListener compositeProgress = new CompositeProgressListener(progressListener, (barcodeSequence == null ? 0 : 1) + traces.size());
        if(barcodeSequence != null) {
            compositeProgress.beginSubtask();
            outputRecord.barcodeSequenceUrn = saveDocument(barcodeSequence, compositeProgress);
        }
        for (NucleotideGraphSequenceDocument trace : traces) {
            compositeProgress.beginSubtask();
            outputRecord.traceDocumentUrns.add(saveDocument(trace, compositeProgress));
        }
    }

    @Override
    public void addTrimmedTraces(List<NucleotideGraphSequenceDocument> traces, ProgressListener progressListener) throws DocumentOperationException {
        CompositeProgressListener savingProgress = new CompositeProgressListener(progressListener, traces.size());
        for (NucleotideGraphSequenceDocument trimmedTrace : traces) {
            savingProgress.beginSubtask();
            outputRecord.trimmedDocumentUrns.add(saveDocument(trimmedTrace, savingProgress));

        }

    }

    @Override
    public void addAssembly(SequenceAlignmentDocument contigAssembly, ProgressListener progressListener) throws DocumentOperationException {
        outputRecord.assemblyUrn = saveDocument(contigAssembly, progressListener);
    }

    @Override
    public void addConsensus(SequenceDocument consensusSequence, ProgressListener progressListener) throws DocumentOperationException {
        outputRecord.consensusUrn = saveDocument(consensusSequence, progressListener);
    }

    @Override
    public void addValidationResult(ValidationResult validationResult, ProgressListener progressListener) throws DocumentOperationException {
        List<PluginDocument> docsToAddToResults = validationResult.getIntermediateDocumentsToAddToResults();
        CompositeProgressListener resultAddingProgress = new CompositeProgressListener(progressListener, docsToAddToResults.size());

        List<URN> supplementaryDocUrns = new ArrayList<URN>();
        for (PluginDocument docToAdd : docsToAddToResults) {
            resultAddingProgress.beginSubtask();
            supplementaryDocUrns.add(saveDocument(docToAdd, resultAddingProgress));
        }

        outputRecord.validationRecords.add(new RecordOfValidationResult(validationResult.isPassed(), validationResult.getMessage(), supplementaryDocUrns));
    }

    public ValidationOutputRecord getRecord() {
        return outputRecord;
    }
}
