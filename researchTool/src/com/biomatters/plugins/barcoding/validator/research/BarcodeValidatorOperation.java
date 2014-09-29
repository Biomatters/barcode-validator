package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.databaseservice.DatabaseServiceException;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.plugins.barcoding.validator.validation.TraceValidation;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.ValidationResult;
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
import java.util.HashMap;
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
        if(icon != null) {
            ICONS = new Icons(new ImageIcon(icon));
        } else {
            ICONS = null;
        }
    }

    @Override
    public GeneiousActionOptions getActionOptions() {
        return new GeneiousActionOptions("Barcode Validator", "", ICONS)
                .setInMainToolbar(true)
                .setMainMenuLocation(GeneiousActionOptions.MainMenu.Tools);
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
    public void performOperation(AnnotatedPluginDocument[] annotatedPluginDocuments, ProgressListener progressListener, Options options, SequenceSelection sequenceSelection, OperationCallback operationCallback) throws DocumentOperationException {
        if (!(options instanceof BarcodeValidatorOptions)) {
            throw new DocumentOperationException("Wrong Options type, " +
                                                 "expected: BarcodeValidatorOptions, " +
                                                 "actual: " + options.getClass().getSimpleName() + ".");
        }

        BarcodeValidatorOptions barcodeValidatorOptions = (BarcodeValidatorOptions)options;

        /* Get options. */
        InputOptions inputSplitterOptions = barcodeValidatorOptions.getInputOptions();
        ErrorProbabilityOptions trimmingOptions = barcodeValidatorOptions.getTrimmingOptions();
        CAP3Options CAP3Options = barcodeValidatorOptions.getAssemblyOptions();
        Map<String, ValidationOptions> traceValidationOptions = barcodeValidatorOptions.getTraceValidationOptions();

        CompositeProgressListener composite = new CompositeProgressListener(progressListener, 0.1, 0.9);

        /* Split inputs. */
        composite.beginSubtask("Grouping traces to barcodes");
        Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> suppliedBarcodesToSuppliedTraces = groupTracesToBarcodes(inputSplitterOptions);

        composite.beginSubtask();
        CompositeProgressListener pipelineProgress = new CompositeProgressListener(composite, suppliedBarcodesToSuppliedTraces.size());
        for (Map.Entry<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> entry : suppliedBarcodesToSuppliedTraces.entrySet()) {
            String setName = getNameForInputSet(entry.getKey(), entry.getValue());
            pipelineProgress.beginSubtask("Validating " + setName);
            setSubFolder(operationCallback, setName);
            CompositeProgressListener stepsProgress = new CompositeProgressListener(pipelineProgress, 4);

            stepsProgress.beginSubtask("Trimming...");
            List<NucleotideGraphSequenceDocument> trimmedTraces = performTrimmingStep(operationCallback, trimmingOptions, entry.getValue(), stepsProgress);

            stepsProgress.beginSubtask("Validating Traces...");
            validateTraces(trimmedTraces, traceValidationOptions, stepsProgress, operationCallback);

            stepsProgress.beginSubtask("Assembling...");
            performAssemblyStep(operationCallback, CAP3Options, setName, stepsProgress, trimmedTraces);

            stepsProgress.beginSubtask("Validating Barcode Sequences...");
            // Should be done as part of BV-16, don't forget to add intermediate docs by calling addIntermediateResultsToCallback()
        }
        composite.setComplete();
    }

    public NucleotideSequenceDocument performAssemblyStep(OperationCallback operationCallback, CAP3Options options, String setName, CompositeProgressListener stepsProgress, List<NucleotideGraphSequenceDocument> trimmedTraces) throws DocumentOperationException {
        CompositeProgressListener assemblyProgress = new CompositeProgressListener(stepsProgress, 3);
        assemblyProgress.beginSubtask();
        SequenceAlignmentDocument assembly = assembleTraces(trimmedTraces, options);
        assemblyProgress.beginSubtask();
        AnnotatedPluginDocument contigDocument = DocumentUtilities.createAnnotatedPluginDocument(assembly);
        contigDocument.setName(setName + " Contig");
        operationCallback.addDocument(contigDocument, false, assemblyProgress);
        assemblyProgress.beginSubtask();
        SequenceDocument consensus = assembly.getSequence(assembly.getContigReferenceSequenceIndex());
        operationCallback.addDocument(consensus, false, assemblyProgress);
        if(consensus instanceof NucleotideSequenceDocument) {
            return (NucleotideSequenceDocument)consensus;
        } else {
            throw new DocumentOperationException("Assembly of nucleotide sequences produced non-nucleotide consensus: " +
                    "Was " + consensus.getClass().getSimpleName() + "\n\n" +
                    "Please contact support@geneious.com with your input files and options.");
        }
    }

    public List<NucleotideGraphSequenceDocument> performTrimmingStep(OperationCallback operationCallback, ErrorProbabilityOptions trimmingOptions, List<NucleotideGraphSequenceDocument> traces, CompositeProgressListener stepsProgress) throws DocumentOperationException {
        List<NucleotideGraphSequenceDocument> trimmedTraces = trimTraces(traces, trimmingOptions);
        CompositeProgressListener savingProgress = new CompositeProgressListener(stepsProgress, trimmedTraces.size());
        for (NucleotideGraphSequenceDocument trimmedTrace : trimmedTraces) {
            savingProgress.beginSubtask();
            operationCallback.addDocument(trimmedTrace, false, savingProgress);
        }
        return trimmedTraces;
    }

    /**
     *
     * @param barcodeSequence The barcode sequence in the set being validated
     * @param traces The traces in the set being validated
     * @return a name for the set of barcode and trace documents
     */
    static String getNameForInputSet(NucleotideSequenceDocument barcodeSequence, List<? extends SequenceDocument> traces) {
        if(barcodeSequence != null) {
            return barcodeSequence.getName();
        }
        String identicalPartOfNames = getIdenticalPartOfNames(traces);
        if(identicalPartOfNames.length() == 0) {
            return getDefaultName();
        } else {
            return identicalPartOfNames;
        }
    }

    private static String getIdenticalPartOfNames(List<? extends SequenceDocument> traces) {
        StringBuilder sameName = new StringBuilder();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String character = null;
            for (SequenceDocument trace : traces) {
                if(i >= trace.getName().length()) {
                    return sameName.toString();
                }
                char toCompare = trace.getName().charAt(i);
                if(character == null) {
                    character = String.valueOf(toCompare);
                } else if(!character.equals(String.valueOf(toCompare))) {
                    return sameName.toString();
                }
            }
            sameName.append(character);
        }
        return sameName.toString();
    }

    private static int SET_NUM = 1;

    /**
     *
     * @return A unique name (for this run) for a set of input
     */
    private static String getDefaultName() {
        return "Set " + SET_NUM++;
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


    /**
     * Groups traces to barcodes.
     *
     * @param options Trace and barcode file paths and Method options.
     * @return Map of barcodes to traces.
     * @throws DocumentOperationException
     */
    private Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>>
    groupTracesToBarcodes(InputOptions options) throws DocumentOperationException {
        return Input.processInputs(options.getTraceFilePaths(),
                                   options.getBarcodeFilePaths(),
                                   options.getMethodOption());
    }

    /**
     * Trims traces.
     *
     * @param traces Traces.
     * @param options
     * @return Trimmed traces.
     * @throws DocumentOperationException
     */
    private List<NucleotideGraphSequenceDocument> trimTraces(List<NucleotideGraphSequenceDocument> traces,
                                                             ErrorProbabilityOptions options)
            throws DocumentOperationException {
        return SequenceTrimmer.trimSequences(traces, options.getErrorProbabilityLimit());
    }

    private void validateTraces(List<NucleotideGraphSequenceDocument> traces, Map<String, ValidationOptions> options, CompositeProgressListener progress, OperationCallback operationCallback)
            throws DocumentOperationException {
        Map<String, ValidationResult> failures = new HashMap<String, ValidationResult>();

        List<TraceValidation> validationTasks = TraceValidation.getTraceValidations();
        CompositeProgressListener perTaskProgress = new CompositeProgressListener(progress, validationTasks.size());
        for (TraceValidation validation : validationTasks) {
            perTaskProgress.beginSubtask();
            ValidationOptions validationOptions = validation.getOptions();

            ValidationResult result = validation.validate(traces, options.get(validationOptions.getName()));

            if (!result.isPassed()) {
                failures.put(validationOptions.getLabel(), result);
            }
            addIntermediateResultsToCallback(result, perTaskProgress, operationCallback);
        }

        if (!failures.isEmpty()) {
            throw new DocumentOperationException(buildValidationFailureMessage(failures));
        }
    }

    private static void addIntermediateResultsToCallback(ValidationResult result, CompositeProgressListener perTaskProgress, OperationCallback operationCallback) throws DocumentOperationException {
        List<PluginDocument> docsToAddToResults = result.getIntermediateDocumentsToAddToResults();
        CompositeProgressListener resultAddingProgress = new CompositeProgressListener(perTaskProgress, docsToAddToResults.size());
        for (PluginDocument docToAdd : docsToAddToResults) {
            resultAddingProgress.beginSubtask();
            operationCallback.addDocument(docToAdd, false, resultAddingProgress);
        }
    }

    /**
     * Assembles contigs.
     *
     * @param traces Traces.
     * @param options
     * @return Contigs.
     * @throws DocumentOperationException
     */
    private SequenceAlignmentDocument assembleTraces(List<NucleotideGraphSequenceDocument> traces,
                                                     CAP3Options options)
            throws DocumentOperationException {
        List<SequenceAlignmentDocument> result = CAP3Runner.assemble(traces,
                                                                     options.getExecutablePath(),
                                                                     options.getMinOverlapLength(),
                                                                     options.getMinOverlapIdentity());

        if (result.size() != 1) {
            throw new DocumentOperationException("todo?");
        }

        return result.get(0);
    }

    private String buildValidationFailureMessage(Map<String, ValidationResult> results) {
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("Failed validations:\n\n");

        for (Map.Entry<String, ValidationResult> result : results.entrySet()) {
            messageBuilder.append(result.getKey()).append(" - ").append(result.getValue().getMessage()).append("\n\n");
        }

        return messageBuilder.toString();
    }
}