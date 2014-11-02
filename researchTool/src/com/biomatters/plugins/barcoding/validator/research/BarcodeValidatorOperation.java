package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.databaseservice.DatabaseServiceException;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.plugins.barcoding.validator.output.ValidationDocumentOperationCallback;
import com.biomatters.plugins.barcoding.validator.output.ValidationOutputRecord;
import com.biomatters.plugins.barcoding.validator.output.ValidationReportDocument;
import com.biomatters.plugins.barcoding.validator.validation.*;
import com.biomatters.plugins.barcoding.validator.validation.assembly.CAP3Options;
import com.biomatters.plugins.barcoding.validator.validation.input.Input;
import com.biomatters.plugins.barcoding.validator.validation.input.InputOptions;
import com.biomatters.plugins.barcoding.validator.validation.trimming.TrimmingOptions;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import javax.swing.*;
import java.net.URL;
import java.util.*;

/**
 * @author Gen Li
 *         Created on 20/08/14 3:11 PM
 */
public class BarcodeValidatorOperation extends DocumentOperation {
    private static final Icons ICONS;
    static {
        URL icon = BarcodeValidatorOperation.class.getResource("barcodeTick24.png");
        if (icon != null) {
            ICONS = new Icons(new ImageIcon(icon));
        } else {
            ICONS = null;
        }
    }

    @Override
    public GeneiousActionOptions getActionOptions() {
        return new GeneiousActionOptions("Barcode Validator", "", ICONS).setInMainToolbar(true).setMainMenuLocation(GeneiousActionOptions.MainMenu.Tools);
    }

    @Override
    public String getHelp() { return null; }

    @Override
    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[] {
                new DocumentSelectionSignature(Object.class, 0, Integer.MAX_VALUE)
        };
    }

    @Override
    public Options getOptions(AnnotatedPluginDocument... documents) throws DocumentOperationException {
        return new BatchBarcodeValidatorOptions();
    }

    @Override
    public void performOperation(AnnotatedPluginDocument[] annotatedPluginDocuments,
                                 ProgressListener progressListener,
                                 Options options,
                                 SequenceSelection sequenceSelection,
                                 OperationCallback operationCallback) throws DocumentOperationException {
        if (!(options instanceof BatchBarcodeValidatorOptions)) {
            throw new DocumentOperationException("Wrong Options type, " +
                                                 "expected: BarcodeValidatorOptions, " +
                                                 "actual: " + options.getClass().getSimpleName() + ".");
        }

        BatchBarcodeValidatorOptions allOptions = (BatchBarcodeValidatorOptions)options;

        CompositeProgressListener composite = new CompositeProgressListener(progressListener, 0.1, 0.9);

        InputOptions inputSplitterOptions = allOptions.getInputOptions();

        composite.beginSubtask("Processing inputs");
        Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> suppliedBarcodesToSuppliedTraces =
                getBarcodesToTraces(composite, operationCallback, inputSplitterOptions);

        composite.beginSubtask();
        Iterator<BarcodeValidatorOptions> iterator = allOptions.iterator();
        CompositeProgressListener perIteration = new CompositeProgressListener(composite, allOptions.getBatchSize());
        int i = 1;
        while(iterator.hasNext()) {
            String setName = "Parameter Set " + i++;
            perIteration.beginSubtask(setName);
            runPipelineWithOptions(setName, suppliedBarcodesToSuppliedTraces, operationCallback, iterator.next(), perIteration);
        }

        composite.setComplete();
    }

    private Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> getBarcodesToTraces(ProgressListener progressListener, OperationCallback operationCallback, InputOptions inputSplitterOptions) throws DocumentOperationException {
        Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> suppliedBarcodesToSuppliedTraces
                = Input.processInputs(inputSplitterOptions.getTraceFilePaths(),
                inputSplitterOptions.getBarcodeFilePaths(),
                inputSplitterOptions.getMethodOption());

        CompositeProgressListener progressPerEntry = new CompositeProgressListener(progressListener, suppliedBarcodesToSuppliedTraces.size());
        Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> resultMap = new HashMap<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>>();
        for (Map.Entry<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> entry : suppliedBarcodesToSuppliedTraces.entrySet()) {
            progressPerEntry.beginSubtask();
            NucleotideSequenceDocument barcodeSequence = entry.getKey();
            List<NucleotideGraphSequenceDocument> traces = entry.getValue();
            CompositeProgressListener compositeProgress = new CompositeProgressListener(progressPerEntry, (barcodeSequence == null ? 0 : 1) + traces.size());

            NucleotideSequenceDocument savedBarcode = null;
            if(barcodeSequence != null) {
                compositeProgress.beginSubtask();
                savedBarcode = saveDatabaseCopyAndReturnNew(NucleotideSequenceDocument.class, barcodeSequence, operationCallback, compositeProgress);
            }
            List<NucleotideGraphSequenceDocument> savedTraces = new ArrayList<NucleotideGraphSequenceDocument>();
            for (NucleotideGraphSequenceDocument trace : traces) {
                compositeProgress.beginSubtask();
                NucleotideGraphSequenceDocument traceCopy = saveDatabaseCopyAndReturnNew(NucleotideGraphSequenceDocument.class, trace, operationCallback, compositeProgress);
                savedTraces.add(traceCopy);
            }

            resultMap.put(savedBarcode, savedTraces);
        }
        return resultMap;
    }

    private static <T extends PluginDocument> T saveDatabaseCopyAndReturnNew(Class<T> docClass, T pluginDoc, OperationCallback operationCallback, ProgressListener progressListener) throws DocumentOperationException {
        AnnotatedPluginDocument apd = operationCallback.addDocument(pluginDoc, true, progressListener);
        PluginDocument copyInDatabase = apd.getDocument();
        if(docClass.isAssignableFrom(copyInDatabase.getClass())) {
            return docClass.cast(copyInDatabase);
        } else {
            throw new IllegalStateException("Saving document of type " + docClass.getSimpleName() + " returned " +
                    copyInDatabase.getClass().getSimpleName());
        }
    }

    private static void runPipelineWithOptions(String setName, Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> suppliedBarcodesToSuppliedTraces, OperationCallback operationCallback, BarcodeValidatorOptions barcodeValidatorOptions, ProgressListener progressListener) throws DocumentOperationException {
        TrimmingOptions trimmingOptions = barcodeValidatorOptions.getTrimmingOptions();
        CAP3Options CAP3Options = barcodeValidatorOptions.getAssemblyOptions();
        Map<String, ValidationOptions> traceValidationOptions = barcodeValidatorOptions.getTraceValidationOptions();
        Map<String, ValidationOptions> barcodeValidationOptions = barcodeValidatorOptions.getBarcodeValidationOptions();

        List<ValidationOutputRecord> outputs = new ArrayList<ValidationOutputRecord>();
        CompositeProgressListener validationProgress = new CompositeProgressListener(progressListener, suppliedBarcodesToSuppliedTraces.size()+1);
        for (Map.Entry<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>>
                suppliedBarcodeToSuppliedTrace : suppliedBarcodesToSuppliedTraces.entrySet()) {

            ValidationDocumentOperationCallback callback = new ValidationDocumentOperationCallback(operationCallback, false);
            NucleotideSequenceDocument barcode = suppliedBarcodeToSuppliedTrace.getKey();
            List<NucleotideGraphSequenceDocument> traces = suppliedBarcodeToSuppliedTrace.getValue();
            String barcodeName = barcode.getName();

            validationProgress.beginSubtask(barcodeName);

            CompositeProgressListener pipelineProgress = new CompositeProgressListener(validationProgress, 0.2, 0.8);
            pipelineProgress.beginSubtask();
            setSubFolder(operationCallback, null);
            callback.setInputs(barcode, traces, pipelineProgress);
            setSubFolder(operationCallback, setName);

            pipelineProgress.beginSubtask();
            Pipeline.runValidationPipeline(barcode, traces, trimmingOptions, CAP3Options, traceValidationOptions, barcodeValidationOptions, callback, pipelineProgress);
            outputs.add(callback.getRecord());
        }

        validationProgress.beginSubtask();
        operationCallback.addDocument(new ValidationReportDocument("Validation Report", outputs, barcodeValidatorOptions), false, validationProgress);
    }


    /**
     * Sets the sub folder for the {@link com.biomatters.geneious.publicapi.plugin.DocumentOperation.OperationCallback}
     * and handles the {@link com.biomatters.geneious.publicapi.databaseservice.DatabaseServiceException} that can
     * occur if the sub folder cannot be created.
     * <br/>
     * If a {@link com.biomatters.geneious.publicapi.databaseservice.DatabaseServiceException} it is logged and all
     * results will be instead delivered to the original destination folder.  The user will not be notified.
     *
     * @param operationCallback The callback provided to {@link #performOperation(com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument[], jebl.util.ProgressListener, com.biomatters.geneious.publicapi.plugin.Options, com.biomatters.geneious.publicapi.plugin.SequenceSelection, com.biomatters.geneious.publicapi.plugin.DocumentOperation.OperationCallback)}
     * @param barcodeName The barcode name.  Is used as the name of the sub folder.
     */
    private static void setSubFolder(OperationCallback operationCallback, String barcodeName) {
        try {
            operationCallback.setSubFolder(barcodeName);
        } catch (DatabaseServiceException e) {
            // When running through the plugin this is OK.  However we should decide on a proper logging system before
            // moving this to the cloud.
            e.printStackTrace();
        }
    }
}