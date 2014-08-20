package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;
import com.biomatters.plugins.barcoding.validator.research.options.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

/**
 * @author Matthew Cheung
 *         Created on 15/07/14 3:08 PM
 */
public class BarcodeValidatorMockOptions extends Options {

    public BarcodeValidatorMockOptions() throws DocumentOperationException {
        super(BarcodeValidatorMockupPlugin.class);

        Options inputOptions = new Options(BarcodeValidatorMockupPlugin.class);
        addLabelWithMoreHelpButton(inputOptions, "Specify any number of files or folders that contain traces or barcode sequences.",
                "Help text describing input formats and options");

        addCollapsibleChildOptions("input", "Input", "", inputOptions, false, false);
        addMultiInputOptions(inputOptions, null, "ab1 files", "traceInput", "Trace(s):");
        addMultiInputOptions(inputOptions, null, "FASTA", "barcodeInput", "Barcode Sequence(s):");


        Options matchFromBoldListOptions = new Options(BarcodeValidatorMockupPlugin.class);
        matchFromBoldListOptions.addFileSelectionOption("traceList", "Trace List: ", "");
        inputOptions.addChildOptions("bold", "tracelist.txt (BOLD)", "", matchFromBoldListOptions);

        Options matchFromGenbankXmlOptions = new Options(BarcodeValidatorMockupPlugin.class);
        matchFromGenbankXmlOptions.addFileSelectionOption("xmlFile", "XML File: ", "");
        inputOptions.addChildOptions("genbank", "XML File (Genbank)", "", matchFromGenbankXmlOptions);

        Options matchNamesOptions = new Options(BarcodeValidatorMockupPlugin.class);
        matchNamesOptions.beginAlignHorizontally(null, false);
        matchNamesOptions.addCustomOption(new NamePartOption("tracePartNum", ""));
        matchNamesOptions.addCustomOption(new NameSeparatorOption("traceSeparator", "part of trace name separated by "));
        matchNamesOptions.endAlignHorizontally();
        matchNamesOptions.beginAlignHorizontally(null, false);
        matchNamesOptions.addCustomOption(new NamePartOption("seqPartNum", ""));
        matchNamesOptions.addCustomOption(new NameSeparatorOption("seqSeparator", "part of sequence name separated by "));
        matchNamesOptions.endAlignHorizontally();
        inputOptions.addChildOptions("names", "part of names", "", matchNamesOptions);

        inputOptions.addChildOptionsPageChooser("method", "Match traces to seqeunces by: ", Collections.<String>emptyList(), PageChooserType.COMBO_BOX, false);

        addCollapsibleChildOptions("trim", "Trimming", "", new TrimmingOptions(), false, true);

        Options traceOptions = new Options(BarcodeValidatorMockupPlugin.class);

        Utilities.addQuestionToOptions(traceOptions, "Traces validated after or before trimming?");
        addCollapsibleChildOptions("traceValidation", "Trace Validation", "", traceOptions, false, true);

        Options traceValidationOptions = new Options(BarcodeValidatorMockupPlugin.class);
        traceOptions.addChildOptions("barcodeValidation", null, "", traceValidationOptions);
        traceValidationOptions.addChildOptions("quality", "Quality", null, new TraceQualityOptions());
        traceValidationOptions.addChildOptionsPageChooser("chooser", "Validation Steps:", Collections.<String>emptyList(), PageChooserType.BUTTONS, true);

        addCollapsibleChildOptions("assembly", "Assembly", "", new FakeCap3Options(), false, true);

        DocumentOperation op = PluginUtilities.getDocumentOperation("Generate_Consensus");
        Options consensusOptions;
        if(op != null) {
            consensusOptions = op.getGeneralOptions();
        } else {
            consensusOptions = new ConsensusOptions();
        }

        addCollapsibleChildOptions("consensus", "Consensus Generation", "", consensusOptions, false, true);

        Options barcodeOptions = new Options(BarcodeValidatorMockupPlugin.class);
        addCollapsibleChildOptions("barcodeValidation", "Barcode Validation", "", barcodeOptions, false, true);

        Options barcodeValidationOptions = new Options(BarcodeValidatorMockupPlugin.class);
        barcodeOptions.addChildOptions("barcodeValidation", null, "", barcodeValidationOptions);
        barcodeValidationOptions.addChildOptions("fasta", "FASTA Check", "", new FastaCheckOptions());
        barcodeValidationOptions.addChildOptions("quality", "Quality", "", new TraceQualityOptions());
        barcodeValidationOptions.addChildOptions("pci", "PCI", "", new PCIOptions());
        barcodeValidationOptions.addChildOptionsPageChooser("chooser", "Validation Steps:", Collections.<String>emptyList(), PageChooserType.BUTTONS, true);

        Options outputOptions = new Options(BarcodeValidatorMockupPlugin.class);
        outputOptions.addFileSelectionOption("output", "Output Folder:", "").setSelectionType(JFileChooser.DIRECTORIES_ONLY);
        addChildOptions("output", "Output", "", outputOptions);
    }

    private int count = 1;
    void addMultiInputOptions(Options inputOutputOptions, String descriptionLabel, final String helpText, String multiOptionsName, String inputOptionsLabel) {
        if(descriptionLabel != null) {
            addLabelWithMoreHelpButton(inputOutputOptions, descriptionLabel, helpText);
        }
        inputOutputOptions.addMultipleOptions(multiOptionsName, new InputFileOptions(inputOptionsLabel), false);
    }

    private void addLabelWithMoreHelpButton(Options options, String label, final String extraHelpText) {
        options.beginAlignHorizontally(null, false);
        options.addLabel(label);
        options.addButtonOption("help" + count++, "", "", IconUtilities.getIcons("help16.png").getIcon16(), ButtonOption.RIGHT)
                .addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        Dialogs.showMessageDialog(extraHelpText);
                    }
                });
        options.endAlignHorizontally();
    }
}