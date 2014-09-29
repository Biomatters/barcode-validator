package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.plugins.barcoding.validator.validation.TraceValidation;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.ValidationResult;
import com.biomatters.plugins.barcoding.validator.validation.assembly.Cap3AssemblerOptions;
import com.biomatters.plugins.barcoding.validator.validation.assembly.Cap3AssemblerRunner;
import com.biomatters.plugins.barcoding.validator.validation.input.Input;
import com.biomatters.plugins.barcoding.validator.validation.input.InputOptions;
import com.biomatters.plugins.barcoding.validator.validation.trimming.ErrorProbabilityOptions;
import com.biomatters.plugins.barcoding.validator.validation.trimming.SequenceTrimmer;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;
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
    public List<AnnotatedPluginDocument> performOperation(AnnotatedPluginDocument[] annotatedDocuments,
                                                          ProgressListener progressListener,
                                                          Options options) throws DocumentOperationException {
        if (!(options instanceof BarcodeValidatorOptions)) {
            throw new DocumentOperationException("Wrong Options type, " +
                                                 "expected: BarcodeValidatorOptions, " +
                                                 "actual: " + options.getClass().getSimpleName() + ".");
        }

        List<AnnotatedPluginDocument> result = new ArrayList<AnnotatedPluginDocument>();

        BarcodeValidatorOptions barcodeValidatorOptions = (BarcodeValidatorOptions)options;

        /* Get options. */
        InputOptions inputSplitterOptions = barcodeValidatorOptions.getInputOptions();
        ErrorProbabilityOptions trimmingOptions = barcodeValidatorOptions.getTrimmingOptions();
        Cap3AssemblerOptions cap3AssemblerOptions = barcodeValidatorOptions.getAssemblyOptions();
        Map<String, Options> traceValidationOptions = barcodeValidatorOptions.getTraceValidationOptions();

        Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> suppliedBarcodesToSuppliedTraces;

        Map<List<NucleotideGraphSequenceDocument>, List<NucleotideGraphSequenceDocument>> suppliedTracesToTrimmedTraces
                = new HashMap<List<NucleotideGraphSequenceDocument>, List<NucleotideGraphSequenceDocument>>();

        Map<NucleotideSequenceDocument, SequenceAlignmentDocument> suppliedBarcodesToAssembledBarcodes
                = new HashMap<NucleotideSequenceDocument, SequenceAlignmentDocument>();

        CompositeProgressListener composite = new CompositeProgressListener(progressListener, 3);

        /* Split inputs. */
        composite.beginSubtask("Grouping traces to barcodes");

        suppliedBarcodesToSuppliedTraces = groupTracesToBarcodes(inputSplitterOptions);

        /* Trim traces. */
        composite.beginSubtask("Trimming traces");

        CompositeProgressListener trimmingProgress
                = new CompositeProgressListener(composite, suppliedBarcodesToSuppliedTraces.size());

        for (List<NucleotideGraphSequenceDocument> traces : suppliedBarcodesToSuppliedTraces.values()) {
            trimmingProgress.beginSubtask();

            suppliedTracesToTrimmedTraces.put(traces, trimTraces(traces, trimmingOptions));
        }

        /* Validate traces. */
        List<NucleotideGraphSequenceDocument> trimmedTracesAll = new ArrayList<NucleotideGraphSequenceDocument>();
        for (List<NucleotideGraphSequenceDocument> trimmedTraces : suppliedTracesToTrimmedTraces.values()) {
            trimmedTracesAll.addAll(trimmedTraces);
        }

        validateTraces(trimmedTracesAll, traceValidationOptions);

        /* Assemble contigs from trimmed traces. */
        composite.beginSubtask("Assembling traces");

        CompositeProgressListener assemblyProgress
                = new CompositeProgressListener(composite, suppliedTracesToTrimmedTraces.size());

        for (Map.Entry<List<NucleotideGraphSequenceDocument>, List<NucleotideGraphSequenceDocument>>
                suppliedTracesToTrimmedTracesEntry : suppliedTracesToTrimmedTraces.entrySet()) {
            assemblyProgress.beginSubtask();

            NucleotideSequenceDocument suppliedBarcode = null;

            for (Map.Entry<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>>
                    suppliedBarcodesToSuppliedTracesEntry : suppliedBarcodesToSuppliedTraces.entrySet()) {
                if (suppliedBarcodesToSuppliedTracesEntry.getValue().equals(suppliedTracesToTrimmedTracesEntry.getKey())) {
                    suppliedBarcode = suppliedBarcodesToSuppliedTracesEntry.getKey();
                }
            }

            suppliedBarcodesToAssembledBarcodes.put(
                    suppliedBarcode,
                    assembleTraces(suppliedTracesToTrimmedTracesEntry.getValue(), cap3AssemblerOptions)
            );
        }

        for (Map.Entry<NucleotideSequenceDocument, SequenceAlignmentDocument>
                suppliedBarcodeToAssembledBarcode : suppliedBarcodesToAssembledBarcodes.entrySet()) {
            AnnotatedPluginDocument contigDocument
                    = DocumentUtilities.createAnnotatedPluginDocument(suppliedBarcodeToAssembledBarcode.getValue());

            /* Rename contig. */
            contigDocument.setName(suppliedBarcodeToAssembledBarcode.getKey().getName() + " Contig");

            result.add(contigDocument);
        }

        composite.setComplete();

        return result;
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

    private void validateTraces(List<NucleotideGraphSequenceDocument> traces, Map<String, Options> options)
            throws DocumentOperationException {
        Map<String, ValidationResult> failures = new HashMap<String, ValidationResult>();

        for (TraceValidation validation : TraceValidation.getTraceValidations()) {
            ValidationOptions validationOptions = validation.getOptions();

            ValidationResult result = validation.validate(traces, options.get(validationOptions.getName()));

            if (!result.isPassed()) {
                failures.put(validationOptions.getLabel(), result);
            }
        }

        if (!failures.isEmpty()) {
            throw new DocumentOperationException(buildValidationFailureMessage(failures));
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
                                                     Cap3AssemblerOptions options)
            throws DocumentOperationException {
        List<SequenceAlignmentDocument> result = Cap3AssemblerRunner.assemble(traces,
                                                                              options.getExecutable(),
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
            messageBuilder.append(result.getKey() + " - " + result.getValue().getMessage()).append("\n\n");
        }

        return messageBuilder.toString();
    }
}