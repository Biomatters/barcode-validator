package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.components.GPanel;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;
import com.biomatters.plugins.barcoding.validator.research.options.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

/**
 * @author Matthew Cheung
 *         Created on 15/07/14 3:08 PM
 */
public class BarcodeValidatorMockOptions extends Options {

    private static final Options.OptionValue[] VALUES = new Options.OptionValue[] {
            new Options.OptionValue("0", "1st"),
            new Options.OptionValue("1", "2nd"),
            new Options.OptionValue("2", "3rd"),
            new Options.OptionValue("3", "4th"),
            new Options.OptionValue("4", "5th"),
            new Options.OptionValue("5", "6th"),
            new Options.OptionValue("6", "7th"),
            new Options.OptionValue("7", "8th"),
            new Options.OptionValue("8", "9th"),
            new Options.OptionValue("9", "10th")

    };

    public BarcodeValidatorMockOptions() throws DocumentOperationException {
        super(BarcodeValidatorMockupPlugin.class);

        Options inputOptions = new Options(BarcodeValidatorMockupPlugin.class);
        addLabelWithMoreHelpButton(inputOptions, "Specify any number of files or folders that contain traces or barcode sequences.",
                "Help text describing input formats and options");

        addCollapsibleChildOptions("input", "Input", "", inputOptions, false, false);
        addMultiInputOptions(inputOptions, null, "ab1 files", "traceInput", "Trace(s):");
        addMultiInputOptions(inputOptions, null, "FASTA", "barcodeInput", "Barcode Sequence(s):");

        Options matchOptions = new Options(BarcodeValidatorMockupPlugin.class);
        Utilities.addQuestionToOptions(inputOptions, "How are these files normally named?");
        matchOptions.beginAlignHorizontally(null, false);
        matchOptions.addCustomOption(new NamePartOption("tracePartNum", ""));
        matchOptions.addCustomOption(new NameSeparatorOption("traceSeparator", "part of trace name separated by "));
        matchOptions.endAlignHorizontally();
        matchOptions.beginAlignHorizontally(null, false);
        matchOptions.addCustomOption(new NamePartOption("seqPartNum", ""));
        matchOptions.addCustomOption(new NameSeparatorOption("seqSeparator", "part of sequence name separated by "));
        matchOptions.endAlignHorizontally();
        inputOptions.addChildOptions("match", "Match traces to sequences by matching", "", matchOptions);

        addCollapsibleChildOptions("trim", "Trimming", "", new TrimmingOptions(), false, true);

        Options traceOptions = new Options(BarcodeValidatorMockupPlugin.class);

        Utilities.addQuestionToOptions(traceOptions, "Traces validated after trimming?");
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
