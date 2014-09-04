package com.biomatters.plugins.barcoding.validator.research.input;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.documents.XMLSerializable;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorPlugin;
import org.jdom.Element;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 3/09/14 3:58 PM
 */
public class InputSplitterOptions extends Options {
    private static final String TRACE_INPUT_OPTION_NAME            = "traceInput";
    private static final String BARCODE_INPUT_OPTION_NAME          = "barcodeInput";
    private static final String MATCH_TRACE_TO_BARCODE_OPTION_NAME = "matchTraceToBarcode";
    private static final String MATCH_USING_BOLD_OPTION_NAME       = "matchUsingBold";
    private static final String MATCH_USING_GENBANK_OPTION_NAME    = "matchUsingGenbank";
    private static final String MATCH_USING_FILE_NAME_OPTION_NAME  = "matchUsingFilename";

    public InputSplitterOptions() {
        super(InputSplitterOptions.class);

        addHelpButton();
        addTraceInput();
        addBarcodeInput();
        addTraceToSequenceMethodSelection();
    }

    public List<String> getTraceFilePaths() {
        return getFilePathsFromMultipleInputFileOptions(TRACE_INPUT_OPTION_NAME);
    }

    public List<String> getBarcodeFilePaths() {
        return getFilePathsFromMultipleInputFileOptions(BARCODE_INPUT_OPTION_NAME);
    }

    private void addHelpButton() {
        beginAlignHorizontally(null, false);
        addButtonOption("helpButton",
                        "Specify any number of files or folders that contain traces or barcode sequences.",
                        "",
                        IconUtilities.getIcons("help16.png").getIcon16(),
                        ButtonOption.RIGHT).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dialogs.showMessageDialog("Help text describing input formats and options");
            }
        });
        endAlignHorizontally();
    }

    private void addTraceInput() {
        addMultipleOptions(TRACE_INPUT_OPTION_NAME, new InputFileOptions("Trace(s):"), false);
    }

    private void addBarcodeInput() {
        addMultipleOptions(BARCODE_INPUT_OPTION_NAME, new InputFileOptions("Barcode Sequence(s):"), false);
    }

    private void addTraceToSequenceMethodSelection() {
        Options matchUsingBoldListOptions = new Options(InputSplitterOptions.class);
        matchUsingBoldListOptions.addFileSelectionOption("traceList", "Trace List: ", "");
        addChildOptions(MATCH_USING_BOLD_OPTION_NAME, "tracelist.txt (BOLD)", "", matchUsingBoldListOptions);

        Options matchUsingGenbankXmlOptions = new Options(InputSplitterOptions.class);
        matchUsingGenbankXmlOptions.addFileSelectionOption("xmlFile", "XML File: ", "");
        addChildOptions(MATCH_USING_GENBANK_OPTION_NAME, "XML File (Genbank)", "", matchUsingGenbankXmlOptions);

        Options matchUsingFileNamesOptions = new Options(InputSplitterOptions.class);
        matchUsingFileNamesOptions.beginAlignHorizontally(null, false);
        matchUsingFileNamesOptions.addCustomOption(new NamePartOption("tracePartNum", ""));
        matchUsingFileNamesOptions.addCustomOption(new NameSeparatorOption("traceSeparator", "part of trace name separated by "));
        matchUsingFileNamesOptions.endAlignHorizontally();
        matchUsingFileNamesOptions.beginAlignHorizontally(null, false);
        matchUsingFileNamesOptions.addCustomOption(new NamePartOption("seqPartNum", ""));
        matchUsingFileNamesOptions.addCustomOption(new NameSeparatorOption("seqSeparator", "part of sequence name separated by "));
        matchUsingFileNamesOptions.endAlignHorizontally();
        addChildOptions(MATCH_USING_FILE_NAME_OPTION_NAME, "part of names", "", matchUsingFileNamesOptions);

        addChildOptionsPageChooser(MATCH_TRACE_TO_BARCODE_OPTION_NAME,
                "Match traces to sequences by: ",
                Collections.<String>emptyList(),
                PageChooserType.COMBO_BOX,
                false);
    }

    private List<String> getFilePathsFromMultipleInputFileOptions(String optionName) {
        List<String> filePaths = new ArrayList<String>();
        for (Options traceInput : getMultipleOptions(optionName).getValues()) {
            filePaths.add(((InputFileOptions)traceInput).getFilePath());
        }
        return filePaths;
    }

    /**
     * @author Matthew Cheung
     *         Created on 15/07/14 4:09 PM
     */
    private static class InputFileOptions extends Options {
        private final String FILE_SELECTION_OPTION_NAME = "fileInput";

        private String label;

        public InputFileOptions(String label) {
            super(BarcodeValidatorPlugin.class);
            this.label = label;
            beginAlignHorizontally(null, false);
            addFileSelectionOption(FILE_SELECTION_OPTION_NAME, this.label, "").setSelectionType(JFileChooser.FILES_ONLY);
            endAlignHorizontally();
        }

        public InputFileOptions(Element element) throws XMLSerializationException {
            this(element.getText());
        }

        public String getFilePath() {
            return getOption(FILE_SELECTION_OPTION_NAME).getValueAsString();
        }

        @Override
        public Element toXML() {
            return new Element(XMLSerializable.ROOT_ELEMENT_NAME).setText(label);
        }
    }

    /**
     * Combo box contain 1st, 2nd, 3rd etc
     *
     * @author Richard
     * @version $Id$
     */
    private static class NamePartOption extends Options.ComboBoxOption<Options.OptionValue> {
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

        public NamePartOption(String name, String label) {
            super(name, label, VALUES, VALUES[0]);
        }

        @Override
        protected JComboBox createComponent() {
            JComboBox box = super.createComponent();
            box.setPrototypeDisplayValue("1stab");
            return box;
        }

        /**
         *
         * @return part of name to use, from 0 to 5
         */
        public int getPart() {
            return Integer.parseInt(getValue().getName());
        }
    }

    /**
     * I WOULD make this class extend EditableComboBox but unfortunately the constructors are private therein so it would
     * require a new release of Geneious.
     *
     * @author Richard
     * @version $Id$
     */
    private static class NameSeparatorOption extends Options.ComboBoxOption<Options.OptionValue> {

        private static final Options.OptionValue[] VALUES = new Options.OptionValue[] {
                new Options.OptionValue("_", "_ (Underscore)"),
                new Options.OptionValue("\\*", "* (Asterisk)"),
                new Options.OptionValue("\\|", "| (Vertical Bar)"),
                new Options.OptionValue("-", "- (Hyphen)"),
                new Options.OptionValue(":", ": (Colon)"),
                new Options.OptionValue("\\$", "$ (Dollar)"),
                new Options.OptionValue("=", "= (Equals)"),
                new Options.OptionValue("\\.", ". (Full Stop)"),
                new Options.OptionValue(",", ", (Comma)"),
                new Options.OptionValue("\\+", "+ (Plus)"),
                new Options.OptionValue("\\~", "~ (Tilde)"),
                new Options.OptionValue("\\s+", "(Space)")
        };

        public NameSeparatorOption(String name, String label) {
            super(name, label, VALUES, VALUES[0]);
            setDescription("The character at which each name is split (there should be one of these before the identifier in each name).");
        }

        public String getSeparatorString() {
            return getValue().getName();
        }
    }
}