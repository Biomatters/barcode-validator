package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.plugins.barcoding.validator.research.assembly.Cap3Assembler;
import com.biomatters.plugins.barcoding.validator.research.assembly.Cap3AssemblerOptions;
import com.biomatters.plugins.barcoding.validator.research.input.InputSplitter;
import com.biomatters.plugins.barcoding.validator.research.input.InputSplitterOptions;
import com.biomatters.plugins.barcoding.validator.research.trimming.ErrorProbabilityOptions;
import com.biomatters.plugins.barcoding.validator.research.trimming.NucleotideSequenceDocumentTrimmer;
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
        return new BarcodeValidatorOptions();
    }

    @Override
    public List<AnnotatedPluginDocument> performOperation(AnnotatedPluginDocument[] annotatedDocuments,
                                                          ProgressListener progressListener,
                                                          Options options) throws DocumentOperationException {
        if (!(options instanceof BarcodeValidatorOptions))
            throw new DocumentOperationException("Unexpected Options type, " +
                                                 "expected: BarcodeValidatorOptions, " +
                                                 "actual: " + options.getClass().getSimpleName() + ".");

        BarcodeValidatorOptions barcodeValidatorOptions = (BarcodeValidatorOptions)options;

        /* Get options. */
        InputSplitterOptions inputSplitterOptions = barcodeValidatorOptions.getInputOptions();
        ErrorProbabilityOptions trimmingOptions = barcodeValidatorOptions.getTrimmingOptions();
        Cap3AssemblerOptions cap3AssemblerOptions = barcodeValidatorOptions.getAssemblyOptions();

        Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>> barcodesToTraces;

        try {
            /* Split inputs. */
            barcodesToTraces = splitInput(inputSplitterOptions);

            List<NucleotideSequenceDocument> traces = new ArrayList<NucleotideSequenceDocument>();

            for (List<NucleotideSequenceDocument> traceSubset : barcodesToTraces.values())
                traces.addAll(traceSubset);

            /* Trim and assemble traces. */
            List<SequenceAlignmentDocument> result = assembleTraces(trimTraces(traces, trimmingOptions),
                                                                    cap3AssemblerOptions);

            System.out.println("End of method.");
        } catch (DocumentOperationException e) {
            Dialogs.showMessageDialog(e.getMessage());
        }

        return null;
    }

    private Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>> splitInput(InputSplitterOptions options)
            throws DocumentOperationException {
        return InputSplitter.split(options.getTraceFilePaths(),
                                   options.getBarcodeFilePaths(),
                                   options.getMethodOption());
    }

    private List<NucleotideSequenceDocument> trimTraces(List<NucleotideSequenceDocument> traces,
                                                        ErrorProbabilityOptions options)
            throws DocumentOperationException {
        return NucleotideSequenceDocumentTrimmer.trim(traces, options.getErrorProbabilityLimit());
    }

    private List<SequenceAlignmentDocument> assembleTraces(List<NucleotideSequenceDocument> traces,
                                                           Cap3AssemblerOptions options)
            throws DocumentOperationException {
        return Cap3Assembler.assemble(traces, options.getMinOverlapLength(), options.getMinOverlapIdentity());
    }
}