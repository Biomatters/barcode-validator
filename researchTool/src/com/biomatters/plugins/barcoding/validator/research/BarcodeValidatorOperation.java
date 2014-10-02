package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.databaseservice.DatabaseServiceException;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.plugins.barcoding.validator.output.ValidationDocumentOperationCallback;
import com.biomatters.plugins.barcoding.validator.output.ValidationOutputRecord;
import com.biomatters.plugins.barcoding.validator.output.ValidationReportDocument;
import com.biomatters.plugins.barcoding.validator.validation.*;
import com.biomatters.plugins.barcoding.validator.validation.assembly.CAP3Options;
import com.biomatters.plugins.barcoding.validator.validation.assembly.CAP3Runner;
import com.biomatters.plugins.barcoding.validator.validation.input.Input;
import com.biomatters.plugins.barcoding.validator.validation.input.InputOptions;
import com.biomatters.plugins.barcoding.validator.validation.trimming.ErrorProbabilityOptions;
import com.biomatters.plugins.barcoding.validator.validation.trimming.SequenceTrimmer;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        return new BarcodeValidatorOptions(BarcodeValidatorOperation.class);
    }

    @Override
    public void performOperation(AnnotatedPluginDocument[] annotatedPluginDocuments,
                                 ProgressListener progressListener,
                                 Options options,
                                 SequenceSelection sequenceSelection,
                                 OperationCallback operationCallback) throws DocumentOperationException {
        if (!(options instanceof BarcodeValidatorOptions)) {
            throw new DocumentOperationException("Wrong Options type, " +
                                                 "expected: BarcodeValidatorOptions, " +
                                                 "actual: " + options.getClass().getSimpleName() + ".");
        }

        BarcodeValidatorOptions barcodeValidatorOptions = (BarcodeValidatorOptions)options;
        List<ValidationOutputRecord> outputs = new ArrayList<ValidationOutputRecord>();
        CompositeProgressListener composite = new CompositeProgressListener(progressListener, 0.1, 0.8, 0.1);

        /* Get options. */
        InputOptions inputSplitterOptions = barcodeValidatorOptions.getInputOptions();
        ErrorProbabilityOptions trimmingOptions = barcodeValidatorOptions.getTrimmingOptions();
        CAP3Options CAP3Options = barcodeValidatorOptions.getAssemblyOptions();
        Map<String, ValidationOptions> traceValidationOptions = barcodeValidatorOptions.getTraceValidationOptions();
        Map<String, ValidationOptions> barcodeValidationOptions = barcodeValidatorOptions.getBarcodeValidationOptions();

        /* Process inputs. */
        composite.beginSubtask("Processing inputs");
        Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> suppliedBarcodesToSuppliedTraces
                = Input.processInputs(inputSplitterOptions.getTraceFilePaths(),
                                      inputSplitterOptions.getBarcodeFilePaths(),
                                      inputSplitterOptions.getMethodOption());

        composite.beginSubtask();
        CompositeProgressListener validationProgress = new CompositeProgressListener(composite, suppliedBarcodesToSuppliedTraces.size());
        for (Map.Entry<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>>
                suppliedBarcodeToSuppliedTrace : suppliedBarcodesToSuppliedTraces.entrySet()) {
            // This block could be moved into the validation module so it can be called from the future web
            // portal and distributed among different nodes as individual nodes (if we wanted to go that far).  Even now
            // it would mean we could easily multi-thread the operation.  Maybe after 0.1.

            ValidationDocumentOperationCallback callback = new ValidationDocumentOperationCallback(operationCallback, false);
            NucleotideSequenceDocument barcode = suppliedBarcodeToSuppliedTrace.getKey();
            List<NucleotideGraphSequenceDocument> traces = suppliedBarcodeToSuppliedTrace.getValue();
            String barcodeName = barcode.getName();

            validationProgress.beginSubtask(barcodeName);
            CompositeProgressListener stepsProgress = new CompositeProgressListener(validationProgress, 5);

            stepsProgress.beginSubtask();
            setSubFolder(operationCallback, null);
            callback.setInputs(barcode, traces, stepsProgress);
            setSubFolder(operationCallback, barcodeName);

            stepsProgress.beginSubtask("Trimming...");
            List<NucleotideGraphSequenceDocument> trimmedTraces = SequenceTrimmer.trimSequences(traces, trimmingOptions.getErrorProbabilityLimit());
            callback.addTrimmedTraces(trimmedTraces, stepsProgress);

            stepsProgress.beginSubtask("Validating Traces...");
            CompositeProgressListener traceValidationProgress = new CompositeProgressListener(stepsProgress, 2);
            traceValidationProgress.beginSubtask();
            List<ValidationRun> traceValidationResults = validateTraces(trimmedTraces, traceValidationOptions, traceValidationProgress);
            traceValidationProgress.beginSubtask();
            addValidationResultsToCallback(callback, traceValidationResults, traceValidationProgress);

            stepsProgress.beginSubtask("Assembling...");
            CompositeProgressListener assembleTracesProgress = new CompositeProgressListener(stepsProgress, 3);
            assembleTracesProgress.beginSubtask();
            SequenceAlignmentDocument contig = assembleTraces(trimmedTraces, CAP3Options, barcodeName, assembleTracesProgress);
            assembleTracesProgress.beginSubtask();
            callback.addAssembly(contig, assembleTracesProgress);
            assembleTracesProgress.beginSubtask();
            NucleotideSequenceDocument consensus = getConsensus(contig);
            callback.addConsensus(consensus, assembleTracesProgress);

            stepsProgress.beginSubtask("Validating Barcode Sequences...");
            CompositeProgressListener validateBarcodeProgress = new CompositeProgressListener(stepsProgress, 2);
            validateBarcodeProgress.beginSubtask();
            List<ValidationResult> barcodeValidationResults = validateBarcodes(barcode, consensus, barcodeValidationOptions, validateBarcodeProgress);
            validateBarcodeProgress.beginSubtask();
            addValidationResultsToCallback(callback, barcodeValidationResults, validateBarcodeProgress);
            outputs.add(callback.getRecord());
        }

        composite.beginSubtask();
        setSubFolder(operationCallback, null);
        operationCallback.addDocument(new ValidationReportDocument("Validation Report", outputs), false, composite);
        composite.setComplete();
    }

    private static void addValidationResultsToCallback(ValidationDocumentOperationCallback callback,
                                               List<ValidationRun> runs,
                                               ProgressListener progressListener) throws DocumentOperationException {
        CompositeProgressListener addTraceValidationResultsProgress = new CompositeProgressListener(progressListener, runs.size());

        for (ValidationRun run : runs) {
            addTraceValidationResultsProgress.beginSubtask();
            callback.addValidationResult(run.options, run.result, addTraceValidationResultsProgress);
        }
    }

    /**
     * Validates traces.
     *
     * @param traces to validate.
     * @param options Map of {@link com.biomatters.plugins.barcoding.validator.validation.ValidationOptions#getIdentifier()} to
     * {@link com.biomatters.plugins.barcoding.validator.validation.ValidationOptions} of tasks to run.
     * @param progressListener to report progress to
     * @return a map of {@link com.biomatters.plugins.barcoding.validator.validation.Validation} to
     * {@link com.biomatters.plugins.barcoding.validator.validation.ValidationResult} obtained from running them.
     *
     * @throws DocumentOperationException if a problem occurs during validation
     */
    private List<ValidationRun> validateTraces(List<NucleotideGraphSequenceDocument> traces,
                                                  Map<String, ValidationOptions> options,
                                                  ProgressListener progressListener)
            throws DocumentOperationException {
        List<ValidationRun> result = new ArrayList<ValidationRun>();
        List<TraceValidation> validationTasks = TraceValidation.getTraceValidations();
        CompositeProgressListener validationProgress = new CompositeProgressListener(progressListener, validationTasks.size());

        for (TraceValidation validation : validationTasks) {
            validationProgress.beginSubtask();
            ValidationOptions validationOptions = validation.getOptions();
            ValidationOptions optionsToRunWith = options.get(validationOptions.getIdentifier());
            result.add(new ValidationRun(optionsToRunWith, validation.validate(traces, optionsToRunWith)));
        }

        return result;
    }

    private static class ValidationRun {
        private ValidationOptions options;
        private ValidationResult result;

        private ValidationRun(ValidationOptions options, ValidationResult result) {
            this.options = options;
            this.result = result;
        }
    }

    private SequenceAlignmentDocument assembleTraces(List<NucleotideGraphSequenceDocument> traces,
                                                     CAP3Options options,
                                                     String contigName,
                                                     ProgressListener progressListener) throws DocumentOperationException {
        CompositeProgressListener assemblyProgress = new CompositeProgressListener(progressListener, 2);

        assemblyProgress.beginSubtask();
        SequenceAlignmentDocument contig = assembleTraces(traces, options);

        assemblyProgress.beginSubtask();
        DocumentUtilities.getAnnotatedPluginDocumentThatContains(contig).setName(contigName);

        if (contig.canSetSequenceNames()) {
            contig.setSequenceName(0, contigName + " Consensus Sequence", true);
        }

        return contig;
    }

    private NucleotideSequenceDocument getConsensus(SequenceAlignmentDocument contig) throws DocumentOperationException {
        SequenceDocument consensus = contig.getSequence(contig.getContigReferenceSequenceIndex());

        if (!(consensus instanceof NucleotideSequenceDocument)) {
            throw new DocumentOperationException(
                    "Assembly produced consensus of unexpected type.\n" +
                    "Expected: " + NucleotideSequenceDocument.class.getSimpleName() + "\n" +
                    "Actual: " + consensus.getClass().getSimpleName() + ".\n\n" +
                    "Please contact support@geneious.com with your input files and options."
            );
        }

        return (NucleotideSequenceDocument)consensus;
    }

    /**
     * Assembles contig.
     *
     * @param traces Traces.
     * @param options
     * @return Contig. Contig returned is always associated with an {@link AnnotatedPluginDocument}.
     * @throws DocumentOperationException
     */
    private SequenceAlignmentDocument assembleTraces(List<NucleotideGraphSequenceDocument> traces, CAP3Options options) throws DocumentOperationException {
        List<SequenceAlignmentDocument> result =
                CAP3Runner.assemble(traces, options.getExecutablePath(), options.getMinOverlapLength(), options.getMinOverlapIdentity());

        int resultSize = result.size();

        if (resultSize != 1) {
            throw new DocumentOperationException(
                    "Unexpected number of contigs assembled:" +
                    "Expected: 1," +
                    "actual: " + resultSize + ".\n\n" +
                    "Please contact support@geneious.com with your input files and options."
            );
        }

        return result.get(0);
    }

    /**
     * Validates barcodes.
     *
     * @param suppliedBarcode Supplied barcode.
     * @param assembedBarcode Assembled barcode.
     * @param options
     * @return Validation results;
     * @throws DocumentOperationException
     */
    private List<ValidationResult> validateBarcodes(SequenceDocument suppliedBarcode,
                                                    SequenceDocument assembedBarcode,
                                                    Map<String, ValidationOptions> options,
                                                    ProgressListener progressListener)
            throws DocumentOperationException {
        List<ValidationResult> result = new ArrayList<ValidationResult>();
        List<BarcodeValidation> barcodeValidations = BarcodeValidation.getBarcodeValidations();
        CompositeProgressListener validationProgress = new CompositeProgressListener(progressListener, barcodeValidations.size());

        for (BarcodeValidation validation : barcodeValidations) {
            ValidationOptions validationOptions = options.get(validation.getOptions().getIdentifier());

            if (validationOptions == null) {
                throw new DocumentOperationException("Could not find validation module '" + options.get(validation.getOptions().getIdentifier() + "'"));
            }

            if (validation instanceof BarcodeCompareValidation) {
                result.add(((BarcodeCompareValidation) validation).validate(suppliedBarcode, assembedBarcode, validationOptions));
            } else {
                throw new DocumentOperationException("Invalid validation options.");
            }
        }

        return result;
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