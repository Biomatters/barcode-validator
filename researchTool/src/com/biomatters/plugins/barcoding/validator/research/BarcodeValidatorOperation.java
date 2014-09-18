package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.plugins.barcoding.validator.validation.assembly.Cap3AssemblerProxy;
import com.biomatters.plugins.barcoding.validator.validation.assembly.Cap3AssemblerOptions;
import com.biomatters.plugins.barcoding.validator.validation.input.Input;
import com.biomatters.plugins.barcoding.validator.validation.input.InputOptions;
import com.biomatters.plugins.barcoding.validator.validation.trimming.ErrorProbabilityOptions;
import com.biomatters.plugins.barcoding.validator.validation.trimming.SequenceTrimmer;
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
        if (!(options instanceof BarcodeValidatorOptions))
            throw new DocumentOperationException("Unexpected Options type, " +
                                                 "expected: BarcodeValidatorOptions, " +
                                                 "actual: " + options.getClass().getSimpleName() + ".");

        List<AnnotatedPluginDocument> result = new ArrayList<AnnotatedPluginDocument>();

        BarcodeValidatorOptions barcodeValidatorOptions = (BarcodeValidatorOptions)options;

        /* Get options. */
        InputOptions inputSplitterOptions = barcodeValidatorOptions.getInputOptions();
        ErrorProbabilityOptions trimmingOptions = barcodeValidatorOptions.getTrimmingOptions();
        Cap3AssemblerOptions cap3AssemblerOptions = barcodeValidatorOptions.getAssemblyOptions();

        Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>
                suppliedBarcodesToSuppliedTraces;
        Map<List<NucleotideSequenceDocument>, List<NucleotideSequenceDocument>>
                suppliedTracesToTrimmedTraces
                = new HashMap<List<NucleotideSequenceDocument>, List<NucleotideSequenceDocument>>();
        Map<NucleotideSequenceDocument, SequenceAlignmentDocument>
                suppliedBarcodesToAssembledBarcodes
                = new HashMap<NucleotideSequenceDocument, SequenceAlignmentDocument>();

        CompositeProgressListener composite = new CompositeProgressListener(progressListener, 3);

        /* Split inputs. */
        composite.beginSubtask("Grouping traces to barcodes.");
        suppliedBarcodesToSuppliedTraces = groupTracesToBarcodes(inputSplitterOptions);

        /* Trim traces. */
        composite.beginSubtask("Trimming traces.");
        for (List<NucleotideSequenceDocument> traces : suppliedBarcodesToSuppliedTraces.values())
            suppliedTracesToTrimmedTraces.put(traces, trimTraces(traces, trimmingOptions));

        /* Assemble trimmed traces. */
        composite.beginSubtask("Assembling traces.");
        for (Map.Entry<List<NucleotideSequenceDocument>, List<NucleotideSequenceDocument>>
                suppliedTracesToTrimmedTracesEntry : suppliedTracesToTrimmedTraces.entrySet()) {
            NucleotideSequenceDocument suppliedBarcode = null;

            for (Map.Entry<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>
                    suppliedBarcodesToSuppliedTracesEntry : suppliedBarcodesToSuppliedTraces.entrySet())
                if (suppliedBarcodesToSuppliedTracesEntry.getValue().equals(suppliedTracesToTrimmedTracesEntry.getKey()))
                    suppliedBarcode = suppliedBarcodesToSuppliedTracesEntry.getKey();

            suppliedBarcodesToAssembledBarcodes.put(
                    suppliedBarcode,
                    assembleTraces(suppliedTracesToTrimmedTracesEntry.getValue(), cap3AssemblerOptions)
            );
        }

        for (SequenceAlignmentDocument assembledBarcode : suppliedBarcodesToAssembledBarcodes.values())
            result.add(DocumentUtilities.createAnnotatedPluginDocument(assembledBarcode));

        composite.setComplete();

        return result;
    }

    private Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>
    groupTracesToBarcodes(InputOptions options) throws DocumentOperationException {
        return Input.processInputs(options.getTraceFilePaths(),
                                   options.getBarcodeFilePaths(),
                                   options.getMethodOption());
    }

    private List<NucleotideSequenceDocument> trimTraces(List<NucleotideSequenceDocument> traces,
                                                        ErrorProbabilityOptions options)
            throws DocumentOperationException {
        return SequenceTrimmer.trim(traces, options.getErrorProbabilityLimit());
    }

    private SequenceAlignmentDocument assembleTraces(List<NucleotideSequenceDocument> traces,
                                                     Cap3AssemblerOptions options)
            throws DocumentOperationException {
        List<SequenceAlignmentDocument> result = Cap3AssemblerProxy.assemble(traces,
                                                                             options.getMinOverlapLength(),
                                                                             options.getMinOverlapIdentity());

        if (result.size() != 1)
            throw new DocumentOperationException("todo?");

        return result.get(0);
    }
}