package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.databaseservice.DatabaseServiceException;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.plugins.barcoding.validator.output.ValidationCallback;
import com.biomatters.plugins.barcoding.validator.output.ValidationDocumentOperationCallback;
import com.biomatters.plugins.barcoding.validator.output.ValidationOutputRecord;
import com.biomatters.plugins.barcoding.validator.output.ValidationReportDocument;
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

        CompositeProgressListener composite = new CompositeProgressListener(progressListener, 0.1, 0.8, 0.1);

        /* Split inputs. */
        composite.beginSubtask("Grouping traces to barcodes");
        Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> suppliedBarcodesToSuppliedTraces = groupTracesToBarcodes(inputSplitterOptions);

        List<ValidationOutputRecord> outputs = new ArrayList<ValidationOutputRecord>();
        composite.beginSubtask();
        CompositeProgressListener pipelineProgress = new CompositeProgressListener(composite, suppliedBarcodesToSuppliedTraces.size());
        for (Map.Entry<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> entry : suppliedBarcodesToSuppliedTraces.entrySet()) {
            // This block could be moved into the validation module so it can be called from the future web
            // portal and distributed among different nodes as individual nodes (if we wanted to go that far).  Even now
            // it would mean we could easily multi-thread the operation.  Maybe after 0.1.

            ValidationDocumentOperationCallback callback = new ValidationDocumentOperationCallback(operationCallback, false);

            String setName = getNameForInputSet(entry.getKey(), entry.getValue());
            pipelineProgress.beginSubtask("Validating " + setName);
            CompositeProgressListener stepsProgress = new CompositeProgressListener(pipelineProgress, 5);

            stepsProgress.beginSubtask();
            setSubFolder(operationCallback, null);
            callback.setInputs(entry.getKey(), entry.getValue(), stepsProgress);

            setSubFolder(operationCallback, setName);
            stepsProgress.beginSubtask("Trimming...");
            List<NucleotideGraphSequenceDocument> trimmedTraces = trimTraces(entry.getValue(), trimmingOptions);
            callback.addTrimmedTraces(trimmedTraces, stepsProgress);

            stepsProgress.beginSubtask("Validating Traces...");
            validateTraces(trimmedTraces, traceValidationOptions, stepsProgress, callback);

            stepsProgress.beginSubtask("Assembling...");
            performAssemblyStep(callback, CAP3Options, setName, stepsProgress, trimmedTraces);

            stepsProgress.beginSubtask("Validating Barcode Sequences...");
            // Should be done as part of BV-16, don't forget to add intermediate docs by calling callback.addValidationResult(result, perTaskProgress);()

            outputs.add(callback.getRecord());
        }

        composite.beginSubtask();
        setSubFolder(operationCallback, null);
        operationCallback.addDocument(new ValidationReportDocument("Validation Report", outputs), false, composite);
        composite.setComplete();
    }

    public NucleotideSequenceDocument performAssemblyStep(ValidationCallback callback, CAP3Options options, String setName, CompositeProgressListener stepsProgress, List<NucleotideGraphSequenceDocument> trimmedTraces) throws DocumentOperationException {
        CompositeProgressListener assemblyProgress = new CompositeProgressListener(stepsProgress, 3);
        assemblyProgress.beginSubtask();
        SequenceAlignmentDocument assembly = assembleTraces(trimmedTraces, options);
        assemblyProgress.beginSubtask();
//        assembly.setName(setName + " Contig"); todo Need to rename in some other way
        callback.addAssembly(assembly, assemblyProgress);
        assemblyProgress.beginSubtask();
        SequenceDocument consensus = assembly.getSequence(assembly.getContigReferenceSequenceIndex());
        callback.addConsensus(consensus, assemblyProgress);
        if(consensus instanceof NucleotideSequenceDocument) {
            return (NucleotideSequenceDocument)consensus;
        } else {
            throw new DocumentOperationException("Assembly of nucleotide sequences produced non-nucleotide consensus: " +
                    "Was " + consensus.getClass().getSimpleName() + "\n\n" +
                    "Please contact support@geneious.com with your input files and options.");
        }
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

    /**
     * Validates traces.
     *
     * @param traces Traces.
     * @param options
     * @throws DocumentOperationException
     */
    private void validateTraces(List<NucleotideGraphSequenceDocument> traces, Map<String, ValidationOptions> options, CompositeProgressListener progress, ValidationDocumentOperationCallback operationCallback)
            throws DocumentOperationException {
        List<TraceValidation> validationTasks = TraceValidation.getTraceValidations();
        CompositeProgressListener perTaskProgress = new CompositeProgressListener(progress, validationTasks.size());
        for (TraceValidation validation : validationTasks) {
            perTaskProgress.beginSubtask();
            ValidationOptions validationOptions = validation.getOptions();

            ValidationResult result = validation.validate(traces, options.get(validationOptions.getName()));
            operationCallback.addValidationResult(result, perTaskProgress);
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
}