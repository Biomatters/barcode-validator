package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.databaseservice.DatabaseService;
import com.biomatters.geneious.publicapi.databaseservice.DatabaseServiceException;
import com.biomatters.geneious.publicapi.databaseservice.WritableDatabaseService;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
import com.biomatters.plugins.barcoding.validator.output.RecordOfValidationResult;
import com.biomatters.plugins.barcoding.validator.output.ValidationDocumentOperationCallback;
import com.biomatters.plugins.barcoding.validator.output.ValidationOutputRecord;
import com.biomatters.plugins.barcoding.validator.output.ValidationReportDocument;
import com.biomatters.plugins.barcoding.validator.validation.*;
import com.biomatters.plugins.barcoding.validator.validation.assembly.CAP3Options;
import com.biomatters.plugins.barcoding.validator.validation.input.Input;
import com.biomatters.plugins.barcoding.validator.validation.input.InputOptions;
import com.biomatters.plugins.barcoding.validator.validation.results.SlidingWindowQualityValidationResultFact;
import com.biomatters.plugins.barcoding.validator.validation.trimming.TrimmingOptions;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import javax.annotation.Nullable;
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
        WritableDatabaseService resultsFolder = getResultsFolder(suppliedBarcodesToSuppliedTraces);
        List<NucleotideSequenceDocument> barcodesWithMissingTraces = new ArrayList<NucleotideSequenceDocument>();
        for (Map.Entry<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> entry : suppliedBarcodesToSuppliedTraces.entrySet()) {
            if(entry.getValue().isEmpty()) {
                barcodesWithMissingTraces.add(entry.getKey());
            }
        }
        if(barcodesWithMissingTraces.size() == suppliedBarcodesToSuppliedTraces.size()) {
            Dialogs.showMessageDialog("All barcode sequences have no associated traces.  Please double " +
                    "check your input sequences and mapping options.", "Invalid Input", null, Dialogs.DialogIcon.INFORMATION);
            return;
        }
        if(!barcodesWithMissingTraces.isEmpty()) {
            String list = StringUtilities.join("\n", Collections2.transform(barcodesWithMissingTraces, new Function<NucleotideSequenceDocument, String>() {
                @Nullable
                @Override
                public String apply(@Nullable NucleotideSequenceDocument input) {
                    return input != null ? input.getName() : "missing";
                }
            }));
            if(!Dialogs.showContinueCancelDialog("The following <strong>" + barcodesWithMissingTraces.size() +
                    "</strong> barcode sequences do not have associated " +
                    "traces and will be skipped.\n\n" + list, "Missing Traces", null, Dialogs.DialogIcon.INFORMATION)) {
                throw new DocumentOperationException.Canceled();
            }
        }

        composite.beginSubtask();
        Iterator<BarcodeValidatorOptions> iterator = allOptions.iterator();
        int batchSize = allOptions.getBatchSize();
        CompositeProgressListener perIteration = new CompositeProgressListener(composite, batchSize);

        int maxLengthOfCounter = String.valueOf(batchSize).length();
        String formatPattern = "%0" + maxLengthOfCounter + "d";

        int i = 1;
        while(iterator.hasNext()) {
            String setName = "Parameter Set " + String.format(formatPattern, i++);
            perIteration.beginSubtask(setName);
            runPipelineWithOptions(setName, SUB_SUB_FOLDER_SEPARATOR, suppliedBarcodesToSuppliedTraces, operationCallback, iterator.next(), perIteration);
            // OperationCallback does not yet support sub sub folders.  So we need to do this manually afterwards.
            moveSubSubFoldersToCorrectLocation(resultsFolder, setName);
        }

        composite.setComplete();
    }


    public static void moveSubSubFoldersToCorrectLocation(WritableDatabaseService resultsFolder, String setName) throws DocumentOperationException {
        try {
            String prefix = setName + SUB_SUB_FOLDER_SEPARATOR;
            WritableDatabaseService subFolder = resultsFolder.getChildService(setName);
            if(subFolder == null) {
                throw new DocumentOperationException("Results folder for " + setName + " is missing.");
            }

            for (GeneiousService geneiousService : resultsFolder.getChildServices()) {
                if(geneiousService instanceof WritableDatabaseService) {
                    WritableDatabaseService database = (WritableDatabaseService) geneiousService;
                    String oldName = database.getFolderName();
                    if(oldName.startsWith(prefix)) {
                        database.moveTo(subFolder);
                    }
                }
            }
            for (GeneiousService child : subFolder.getChildServices()) {
                if(child instanceof WritableDatabaseService) {
                    WritableDatabaseService database = (WritableDatabaseService) child;
                    String oldName = database.getFolderName();
                    if(oldName.startsWith(prefix)) {
                        database.renameFolder(database.getFolderName().substring(prefix.length()));
                    }
                }
            }
        } catch (DatabaseServiceException e) {
            throw new DocumentOperationException("Failed to make changes to database: " + e.getMessage(), e);
        }
    }

    private static WritableDatabaseService getResultsFolder(Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> validatorInput) throws DocumentOperationException {
        NucleotideGraphSequenceDocument sampleTrace = getOneTraceFromInput(validatorInput);
        if(sampleTrace == null) {
            throw new DocumentOperationException("Cannot continue operation.  There were no trace documents.");
        }
        AnnotatedPluginDocument apd = DocumentUtilities.getAnnotatedPluginDocumentThatContains(sampleTrace);
        if(apd == null) {
            throw new DocumentOperationException("Cannot continue operation.  Input was not saved to a database.");
        }
        DatabaseService resultsFolder = apd.getDatabase();
        if(!(resultsFolder instanceof WritableDatabaseService)) {
            throw new DocumentOperationException("Cannot continue operation.  Results are being saved to a non writable database: " + resultsFolder.getClass());
        }
        return (WritableDatabaseService)resultsFolder;
    }

    private static NucleotideGraphSequenceDocument getOneTraceFromInput(Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> validatorInput) {
        for (List<NucleotideGraphSequenceDocument> traces : validatorInput.values()) {
            for (NucleotideGraphSequenceDocument trace : traces) {
                if(trace != null) {
                    return trace;
                }
            }
        }
        return null;
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

    private static final String SUB_SUB_FOLDER_SEPARATOR = "_";
    private static void runPipelineWithOptions(String setName, String subSubFolderSeparator, Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> suppliedBarcodesToSuppliedTraces, OperationCallback operationCallback, BarcodeValidatorOptions barcodeValidatorOptions, ProgressListener progressListener) throws DocumentOperationException {
        TrimmingOptions trimmingOptions = barcodeValidatorOptions.getTrimmingOptions();
        CAP3Options CAP3Options = barcodeValidatorOptions.getAssemblyOptions();
        Map<String, ValidationOptions> validationOptions = barcodeValidatorOptions.getValidationOptions();

        List<ValidationOutputRecord> outputs = new ArrayList<ValidationOutputRecord>();
        CompositeProgressListener validationProgress = new CompositeProgressListener(progressListener, suppliedBarcodesToSuppliedTraces.size()+1);
        for (Map.Entry<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>>
                suppliedBarcodeToSuppliedTrace : suppliedBarcodesToSuppliedTraces.entrySet()) {

            ValidationDocumentOperationCallback callback = new ValidationDocumentOperationCallback(operationCallback, false);
            NucleotideSequenceDocument barcode = suppliedBarcodeToSuppliedTrace.getKey();
            List<NucleotideGraphSequenceDocument> traces = suppliedBarcodeToSuppliedTrace.getValue();
            String barcodeName = barcode.getName();

            validationProgress.beginSubtask(barcodeName);
            if(traces.isEmpty()) {
                continue;
            }

            CompositeProgressListener pipelineProgress = new CompositeProgressListener(validationProgress, 0.2, 0.8);
            pipelineProgress.beginSubtask();
            setSubFolder(operationCallback, null);
            callback.setInputs(barcode, traces, pipelineProgress);
            setSubFolder(operationCallback, setName + subSubFolderSeparator + barcodeName);

            pipelineProgress.beginSubtask();
            ValidationOutputRecord record = callback.getRecord();
            record.setSetName(setName);
            Pipeline.runValidationPipeline(barcode, traces, trimmingOptions, CAP3Options, validationOptions, callback, pipelineProgress);

            saveChangesToSequencesMadeByValidationPipeline(record);

            setSubFolder(operationCallback, setName);
            outputs.add(record);
        }

        validationProgress.beginSubtask();
        operationCallback.addDocument(new ValidationReportDocument(setName + REPORT_NAME_SUFFIX, outputs, barcodeValidatorOptions), false, validationProgress);
    }

    private static void saveChangesToSequencesMadeByValidationPipeline(ValidationOutputRecord record) {
        List<URN> docsToSave = new ArrayList<URN>(record.getTrimmedDocumentUrns());

        // There is probably a more general way of doing this.  But it would involve making a fact aware of if the
        // input sequence needed saving.  This will do for now since it is isolated to the research tool.
        Map<URN, RecordOfValidationResult> qualityValidationResults = record.getValidationResultsMap().get(
                SlidingWindowQualityValidationResultFact.class);
        for (Map.Entry<URN, RecordOfValidationResult> entry : qualityValidationResults.entrySet()) {
            if(!entry.getValue().isPassed()) {
                docsToSave.add(entry.getKey());
            }
        }
        for (URN urn : docsToSave) {
            AnnotatedPluginDocument apd = DocumentUtilities.getDocumentByURN(urn);
            if(apd != null) {
                apd.saveDocument();
            }
        }
    }

    public static final String REPORT_NAME_SUFFIX = " Validation Report";


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