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
import java.util.Collections;

/**
 * @author Gen Li
 *         Created on 3/09/14 3:58 PM
 */
public class InputSplitterOptions extends Options {
    public InputSplitterOptions() {
        super(InputSplitterOptions.class);

        /* Help button. */
        beginAlignHorizontally(null, false);
        addButtonOption("helpButton",
                        "Specify any number of files or folders that contain traces or barcode sequences.",
                        "",
                        IconUtilities.getIcons("help16.png").getIcon16(),
                        ButtonOption.RIGHT)
                .addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Dialogs.showMessageDialog("Help text describing input formats and options");
                    }
                });
        endAlignHorizontally();

        /* Trace input option. */
        addMultipleOptions("traceInput", new InputFileOptions("Trace(s):"), false);

        /* Barcode input option. */
        addMultipleOptions("barcodeInput", new InputFileOptions("Barcode Sequence(s):"), false);

        /* Method chooser option. */
        Options matchFromBoldListOptions = new Options(InputSplitterOptions.class);
        matchFromBoldListOptions.addFileSelectionOption("traceList", "Trace List: ", "");
        addChildOptions("bold", "tracelist.txt (BOLD)", "", matchFromBoldListOptions);

        Options matchFromGenbankXmlOptions = new Options(InputSplitterOptions.class);
        matchFromGenbankXmlOptions.addFileSelectionOption("xmlFile", "XML File: ", "");
        addChildOptions("genbank", "XML File (Genbank)", "", matchFromGenbankXmlOptions);

        Options matchNamesOptions = new Options(InputSplitterOptions.class);

        matchNamesOptions.beginAlignHorizontally(null, false);
        matchNamesOptions.addCustomOption(new NamePartOption("tracePartNum", ""));
        matchNamesOptions.addCustomOption(new NameSeparatorOption("traceSeparator", "part of trace name separated by "));
        matchNamesOptions.endAlignHorizontally();

        matchNamesOptions.beginAlignHorizontally(null, false);
        matchNamesOptions.addCustomOption(new NamePartOption("seqPartNum", ""));
        matchNamesOptions.addCustomOption(new NameSeparatorOption("seqSeparator", "part of sequence name separated by "));
        matchNamesOptions.endAlignHorizontally();

        addChildOptions("names", "part of names", "", matchNamesOptions);

        addChildOptionsPageChooser("method", "Match traces to sequences by: ", Collections.<String>emptyList(), PageChooserType.COMBO_BOX, false);
    }

    /**
     * @author Matthew Cheung
     *         Created on 15/07/14 4:09 PM
     */
    private static class InputFileOptions extends Options {

        private String label;

        public InputFileOptions(String label) {
            super(BarcodeValidatorPlugin.class);
            this.label = label;
            beginAlignHorizontally(null, false);
            addFileSelectionOption("input", this.label, "").setSelectionType(JFileChooser.FILES_ONLY);
            endAlignHorizontally();
        }

        public InputFileOptions(Element element) throws XMLSerializationException {
            this(element.getText());
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