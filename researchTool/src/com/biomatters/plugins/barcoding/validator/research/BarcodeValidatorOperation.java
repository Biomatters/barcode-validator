package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.databaseservice.DatabaseService;
import com.biomatters.geneious.publicapi.databaseservice.DatabaseServiceException;
import com.biomatters.geneious.publicapi.databaseservice.WritableDatabaseService;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
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
import com.biomatters.plugins.barcoding.validator.validation.input.InputOptions;
import com.biomatters.plugins.barcoding.validator.validation.input.InputProcessor;
import com.biomatters.plugins.barcoding.validator.validation.pci.PciCalculator;
import com.biomatters.plugins.barcoding.validator.validation.pci.PciCalculatorOptions;
import com.biomatters.plugins.barcoding.validator.validation.SlidingWindowQualityValidationResultFact;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
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
    public static final String VALIDATION_REPORT_NAME_SUFFIX = " Validation Report";
    public static final String PARAMETER_SET_PREFIX = "Parameter Set ";

    private static final String SUB_SUB_FOLDER_SEPARATOR = "_";
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
            throw new DocumentOperationException("Wrong Options type, expected: BarcodeValidatorOptions, actual: " + options.getClass().getSimpleName() + ".");
        }

        BatchBarcodeValidatorOptions allOptions = (BatchBarcodeValidatorOptions)options;
        CompositeProgressListener composite = new CompositeProgressListener(progressListener, 0.1, 0.9);

        composite.beginSubtask("Processing inputs");

        InputOptions inputSplitterOptions = allOptions.getInputOptions();
        Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> suppliedBarcodesToSuppliedTraces = InputProcessor.run(
                inputSplitterOptions.getTraceFilePaths(),
                inputSplitterOptions.getBarcodeFilePaths(),
                inputSplitterOptions.getMethodOption(),
                operationCallback,
                composite
        );

        List<AnnotatedPluginDocument> barcodesWithMissingTraces = new ArrayList<AnnotatedPluginDocument>();
        for (AnnotatedPluginDocument suppliedBarcode : suppliedBarcodesToSuppliedTraces.keySet()) {
            if (suppliedBarcodesToSuppliedTraces.get(suppliedBarcode).isEmpty()) {
                barcodesWithMissingTraces.add(suppliedBarcode);
            }
        }

        if (barcodesWithMissingTraces.size() == suppliedBarcodesToSuppliedTraces.size()) {
            Dialogs.showMessageDialog(
                    "All barcode sequences have no associated traces.  Please double check your input sequences and mapping options.",
                    "Invalid Input",
                    null,
                    Dialogs.DialogIcon.INFORMATION
            );

            return;
        }

        if (!barcodesWithMissingTraces.isEmpty()) {
            String list = StringUtilities.join("\n", Collections2.transform(barcodesWithMissingTraces, new Function<AnnotatedPluginDocument, String>() {
                @Nullable
                @Override
                public String apply(@Nullable AnnotatedPluginDocument input) {
                    return input != null ? input.getName() : "missing";
                }
            }));

            boolean continueSelected = Dialogs.showContinueCancelDialog(
                    "The following <strong>" + barcodesWithMissingTraces.size() + "</strong> barcode sequences do not have associated traces and will be skipped.\n\n" + list,
                    "Missing Traces",
                    null,
                    Dialogs.DialogIcon.INFORMATION
            );

            if (!continueSelected) {
                throw new DocumentOperationException.Canceled();
            }
        }

        composite.beginSubtask();

        WritableDatabaseService resultsFolder = getResultsFolder(suppliedBarcodesToSuppliedTraces);
        Iterator<BarcodeValidatorOptions> parameterSetIterator = allOptions.iterator();
        int currentParameterSet = 1;
        CompositeProgressListener perIteration = new CompositeProgressListener(composite, allOptions.getBatchSize());
        while (parameterSetIterator.hasNext()) {
            String parameterSetName = PARAMETER_SET_PREFIX + currentParameterSet;

            if (resultsFolder.getChildService(parameterSetName) != null) {
                int renameIndex = 1;
                while (resultsFolder.getChildService(parameterSetName + " (" + renameIndex + ")") != null) {
                    renameIndex++;
                }

                parameterSetName += " (" + renameIndex + ")";
            }

            perIteration.beginSubtask(parameterSetName);

            runPipelineWithOptions(currentParameterSet, parameterSetName, suppliedBarcodesToSuppliedTraces, operationCallback, parameterSetIterator.next(), perIteration);

            // OperationCallback does not yet support sub sub folders.  So we need to do this manually afterwards.
            moveSubSubFoldersToCorrectLocation(resultsFolder, parameterSetName);

            currentParameterSet++;
        }

        composite.setComplete();
    }

    private static void moveSubSubFoldersToCorrectLocation(WritableDatabaseService resultsFolder, String parameterSetName) throws DocumentOperationException {
        try {
            String prefix = parameterSetName + SUB_SUB_FOLDER_SEPARATOR;
            WritableDatabaseService subFolder = resultsFolder.getChildService(parameterSetName);

            if (subFolder == null) {
                throw new DocumentOperationException("Results folder for " + parameterSetName + " is missing.");
            }

            for (GeneiousService geneiousService : resultsFolder.getChildServices()) {
                if (geneiousService instanceof WritableDatabaseService) {
                    WritableDatabaseService database = (WritableDatabaseService)geneiousService;
                    String oldName = database.getFolderName();
                    if (oldName.startsWith(prefix)) {
                        database.moveTo(subFolder);
                    }
                }
            }

            for (GeneiousService child : subFolder.getChildServices()) {
                if (child instanceof WritableDatabaseService) {
                    WritableDatabaseService database = (WritableDatabaseService)child;
                    String oldName = database.getFolderName();
                    if (oldName.startsWith(prefix)) {
                        database.renameFolder(database.getFolderName().substring(prefix.length()));
                    }
                }
            }
        } catch (DatabaseServiceException e) {
            throw new DocumentOperationException("Failed to make changes to database: " + e.getMessage(), e);
        }
    }

    private static WritableDatabaseService getResultsFolder(Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> validatorInput) throws DocumentOperationException {
        AnnotatedPluginDocument sampleTrace = getOneTraceFromInput(validatorInput);

        if (sampleTrace == null) {
            throw new DocumentOperationException("Cannot continue operation.  There were no trace documents.");
        }

        DatabaseService resultsFolder = sampleTrace.getDatabase();

        if (!(resultsFolder instanceof WritableDatabaseService)) {
            throw new DocumentOperationException("Cannot continue operation.  Results are being saved to a non writable database: " + (resultsFolder == null ? "null" : resultsFolder.getClass()));
        }

        return (WritableDatabaseService)resultsFolder;
    }

    private static AnnotatedPluginDocument getOneTraceFromInput(Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> validatorInput) {
        Collection<AnnotatedPluginDocument> inputTraces = validatorInput.values();
        return inputTraces.isEmpty() ? null : inputTraces.iterator().next();
    }

    private static void runPipelineWithOptions(int parameterSetNumber,
                                               String parameterSetName,
                                               Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> suppliedBarcodesToSuppliedTraces,
                                               OperationCallback operationCallback,
                                               BarcodeValidatorOptions barcodeValidatorOptions,
                                               ProgressListener progressListener) throws DocumentOperationException {
        PciCalculatorOptions pciCalculatorOptions = barcodeValidatorOptions.getPciCalculatorOptions();
        boolean runPCICalculation = pciCalculatorOptions.canPerformPCICalculation();
        CompositeProgressListener validationProgress = new CompositeProgressListener(progressListener, suppliedBarcodesToSuppliedTraces.size() + (runPCICalculation ? 2 : 1));

        List<ValidationOutputRecord> outputs = new ArrayList<ValidationOutputRecord>();
        for (AnnotatedPluginDocument suppliedBarcode : suppliedBarcodesToSuppliedTraces.keySet()) {
            NucleotideSequenceDocument barcode = (NucleotideSequenceDocument)suppliedBarcode.getDocument();
            String barcodeName = barcode.getName();
            ValidationDocumentOperationCallback callback = new ValidationDocumentOperationCallback(operationCallback, false);

            validationProgress.beginSubtask(barcodeName);

            Function<AnnotatedPluginDocument, NucleotideGraphSequenceDocument> getPluginDocFunction = new Function<AnnotatedPluginDocument, NucleotideGraphSequenceDocument>() {
                @Nullable
                @Override
                public NucleotideGraphSequenceDocument apply(@Nullable AnnotatedPluginDocument input) {
                    if (input == null) {
                        return null;
                    }

                    return (NucleotideGraphSequenceDocument)input.getDocumentOrNull();
                }
            };
            List<NucleotideGraphSequenceDocument> traces = new ArrayList<NucleotideGraphSequenceDocument>(Collections2.transform(suppliedBarcodesToSuppliedTraces.get(suppliedBarcode), getPluginDocFunction));

            if (traces.isEmpty()) {
                continue;
            }

            CompositeProgressListener pipelineProgress = new CompositeProgressListener(validationProgress, 0.2, 0.8);

            pipelineProgress.beginSubtask();

            setSubFolder(operationCallback, null);
            callback.setInputs(barcode, traces, pipelineProgress);
            setSubFolder(operationCallback, parameterSetName + SUB_SUB_FOLDER_SEPARATOR + barcodeName);

            pipelineProgress.beginSubtask();

            Pipeline.runValidationPipeline(
                    barcode,
                    traces,
                    barcodeValidatorOptions.getTrimmingOptions(),
                    barcodeValidatorOptions.getAssemblyOptions(),
                    barcodeValidatorOptions.getValidationOptions(),
                    callback,
                    pipelineProgress
            );
            ValidationOutputRecord record = callback.getRecord();
            record.setParameterSetName(String.valueOf(parameterSetNumber));
            saveChangesToSequencesMadeByValidationPipeline(record);
            setSubFolder(operationCallback, parameterSetName);
            outputs.add(record);
        }

        Map<URN, Double> PCIValues = new HashMap<URN, Double>();

        if (!runPCICalculation) {
            validationProgress.beginSubtask("Calculating PCI...");
            PCIValues = PciCalculator.calculate(
                    getUniqueConsensusURNs(outputs),
                    pciCalculatorOptions.getGenus(),
                    pciCalculatorOptions.getSpecies(),
                    pciCalculatorOptions.getPathToBarcodesFile()
            );
        }

        validationProgress.beginSubtask("Saving Report...");
        operationCallback.addDocument(new ValidationReportDocument(parameterSetName + VALIDATION_REPORT_NAME_SUFFIX, outputs, barcodeValidatorOptions, PCIValues), false, validationProgress);
    }

    private static Set<URN> getUniqueConsensusURNs(List<ValidationOutputRecord> outputRecords) {
        Set<URN> getUniqueConsensusURNs = new HashSet<URN>();

        for (ValidationOutputRecord output : outputRecords) {
            URN consensusUrn = output.getConsensusUrn();

            if (consensusUrn != null) {
                getUniqueConsensusURNs.add(consensusUrn);
            }

            getUniqueConsensusURNs.addAll(output.getTrimmedDocumentUrns());
        }

        return getUniqueConsensusURNs;
    }

    private static void saveChangesToSequencesMadeByValidationPipeline(ValidationOutputRecord record) {
        List<URN> docsToSave = new ArrayList<URN>(record.getTrimmedDocumentUrns());

        // There is probably a more general way of doing this.  But it would involve making a fact aware of if the
        // input sequence needed saving.  This will do for now since it is isolated to the research tool.
        Map<URN, RecordOfValidationResult> qualityValidationResults = record.getValidationResultsMap().get(SlidingWindowQualityValidationResultFact.class);
        for (Map.Entry<URN, RecordOfValidationResult> entry : qualityValidationResults.entrySet()) {
            if (!entry.getValue().isPassed()) {
                docsToSave.add(entry.getKey());
            }
        }

        for (URN urn : docsToSave) {
            AnnotatedPluginDocument apd = DocumentUtilities.getDocumentByURN(urn);
            if (apd != null) {
                apd.saveDocument();
            }
        }
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