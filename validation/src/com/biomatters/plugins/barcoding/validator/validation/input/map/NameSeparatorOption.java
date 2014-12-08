package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.plugin.Options;

/**
 * I WOULD make this class extend EditableComboBox but unfortunately the constructors are private therein so it would
 * require a new release of Geneious.
 *
 * @author Richard
 * @version $Id$
 */
public class NameSeparatorOption extends Options.ComboBoxOption<Options.OptionValue> {
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
        this(name, label, 0);
    }

    public NameSeparatorOption(String name, String label, int defaultIndex) {
        super(name, label, VALUES, VALUES[defaultIndex]);

        setDescription("The character at which each name is split (there should be one of these before the identifier in each name).");
    }

    public String getSeparatorString() {
        return getValue().getName();
    }

    /**
     * @param separatorString The part number.  See {@link #getSeparatorString()} ()}
     * @return The label to show to the user for the specified separator string
     */
    public static String getLabelForPartNumber(String separatorString) {
        for (Options.OptionValue value : VALUES) {
            if (separatorString.equals(value.getName())) {
                return value.getLabel();
            }
        }
        
        return null;
    }
}