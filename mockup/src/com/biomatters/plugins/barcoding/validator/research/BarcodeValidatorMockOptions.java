package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.components.GPanel;
import com.biomatters.geneious.publicapi.plugin.Options;
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

    private int widthOfArrowLine = 20;
    private int widthOfArrowTriangle = 40;
    private int heightOfArrow = 40;

    public BarcodeValidatorMockOptions() {
        super(BarcodeValidatorMockupPlugin.class);

        Options traceOptions = new Options(BarcodeValidatorMockupPlugin.class);
        addCollapsibleChildOptions("traceValidation", "Trace Validation", "", traceOptions, false, false);

        Options traceInputOptions = new Options(BarcodeValidatorMockupPlugin.class);
        addMultiInputOptions(traceInputOptions, "Specify any number of trace files or folders to validate",
                "Some help", "traceInput", "Trace File/Folder:");
        traceOptions.addCollapsibleChildOptions("sequenceInput", "Input", "", traceInputOptions, false, false);

        Options traceValidationOptions = new Options(BarcodeValidatorMockupPlugin.class);
        traceOptions.addChildOptions("barcodeValidation", null, "", traceValidationOptions);
        traceValidationOptions.addChildOptions("quality", "Quality", null, new TraceQualityOptions());
        traceValidationOptions.addChildOptionsPageChooser("chooser", "Validation Steps:", Collections.<String>emptyList(), PageChooserType.BUTTONS, true);

//        addCustomComponent(new GPanel() {
//            @Override
//            public void paint(Graphics g) {
//                super.paint(g);
//                int width = getWidth();
//                int middleOfPanel = getX() + width/2;
//                g.setColor(Color.BLUE);
//                g.fillRect(middleOfPanel - widthOfArrowLine / 2, getY(), widthOfArrowLine, getHeight());
//            }
//
//            @Override
//            public Dimension getPreferredSize() {
//                return new Dimension(widthOfArrowTriangle, heightOfArrow);
//            }
//
//            @Override
//            public int getHeight() {
//                return heightOfArrow;
//            }
//        });


        addCollapsibleChildOptions("trim", "Trimming", "", new TrimmingOptions(), false, false);
        addCollapsibleChildOptions("assembly", "Assembly", "", new FakeCap3Options(), false, false);
        addCollapsibleChildOptions("consensus", "Consensus Generation", "", new FakeCap3Options(), false, false);

        Options barcodeOptions = new Options(BarcodeValidatorMockupPlugin.class);
        addCollapsibleChildOptions("barcodeValidation", "Barcode Validation", "", barcodeOptions, false, false);

        Options barcodeInputOptions = new Options(BarcodeValidatorMockupPlugin.class);
        addMultiInputOptions(barcodeInputOptions, "Specify any number of barcode files to validate",
                "Some help", "barcodeInput", "Barcode Sequence File:");
        barcodeOptions.addCollapsibleChildOptions("sequenceInput", "Input", "", barcodeInputOptions, false, false);

        Options barcodeValidationOptions = new Options(BarcodeValidatorMockupPlugin.class);
        barcodeOptions.addChildOptions("barcodeValidation", null, "", barcodeValidationOptions);
        barcodeValidationOptions.addChildOptions("pci", "PCI", "", new PCIOptions());
        barcodeValidationOptions.addChildOptions("fasta", "FASTA Check", "", new FastaCheckOptions());
        barcodeValidationOptions.addChildOptionsPageChooser("chooser", "Validation Steps:", Collections.<String>emptyList(), PageChooserType.BUTTONS, true);

        Options outputOptions = new Options(BarcodeValidatorMockupPlugin.class);
        outputOptions.addFileSelectionOption("output", "Output Folder:", "").setSelectionType(JFileChooser.DIRECTORIES_ONLY);
        outputOptions.addBooleanOption("reportAmbig", "Report # ambiguous bases:", true);  // todo Do we need this?  Maybe it's always on
        addChildOptions("output", "Output", "", outputOptions);
    }

    private int count = 1;
    void addMultiInputOptions(Options inputOutputOptions, String descriptionLabel, final String helpText, String multiOptionsName, String inputOptionsLabel) {
        inputOutputOptions.beginAlignHorizontally(null, false);
        inputOutputOptions.addLabel(descriptionLabel);
        inputOutputOptions.addButtonOption("help" + count++, "", "", IconUtilities.getIcons("help16.png").getIcon16(), ButtonOption.RIGHT)
            .addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    Dialogs.showMessageDialog(helpText);
                }
            });

        inputOutputOptions.endAlignHorizontally();
        inputOutputOptions.addMultipleOptions(multiOptionsName, new InputFileOptions(inputOptionsLabel), false);
    }
}
