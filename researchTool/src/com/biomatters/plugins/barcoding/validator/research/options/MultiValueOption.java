package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.components.GPanel;
import com.biomatters.geneious.publicapi.components.GTextField;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
import org.jdom.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An option that supports setting multiple values.  Either by specifying them exactly using {@link com.biomatters.geneious.publicapi.plugin.Options.MultipleOptions}
 * or by specifying a min, max and step size.
 *
 * @author Matthew Cheung
 *         Created on 31/10/14 9:39 AM
 */
public abstract class MultiValueOption<T extends Number> extends Options.Option<List<T>, MultiValueOption.Component> {

    private static final String SEPARATOR = ",";

    public static final String SUFFIX = "_range";

    MultiValueOption(String name, String label, T... defaultValue) {
        super(name, label, Arrays.asList(defaultValue));
    }

    protected MultiValueOption(Element element) throws XMLSerializationException {
        super(element);
    }

    /**
     * 
     * @param valueString The string representation of a single value
     * @return The value or null if the string could not be converted
     */
    abstract T getSingleValueFromString(String valueString);

    abstract String singleValueToString(T singleValue);

    abstract Options.Option<T, ? extends JComponent> addOption(Options options, String name, String label, boolean useMinMax);

    abstract List<T> getForSteps(T min, T max, T step);

    @Override
    public List<T> getValueFromString(String s) {
        String[] valueStrings = s.split(SEPARATOR);
        List<T> list = new ArrayList<T>(valueStrings.length);
        for (String valueString : valueStrings) {
            T value = getSingleValueFromString(valueString);
            if(value == null) {
                return getDefaultValue();  // If the field contains something not right then use the default instead
            }
            list.add(value);
        }
        return list;
    }

    @Override
    public String getValueAsString(List<T> value) {
        List<String> stringList = new ArrayList<String>();
        for (T item : value) {
            stringList.add(singleValueToString(item));
        }
        return StringUtilities.join(SEPARATOR, stringList);
    }

    @Override
    protected void setValueOnComponent(MultiValueOption.Component component, List<T> list) {
        component.textField.setText(getValueAsString(list));
    }


    @Override
    protected MultiValueOption.Component<T> createComponent() {
        return new MultiValueOption.Component<T>(this, getDefaultValue());
    }

    static class Component<T extends Number> extends GPanel {

        private JTextField textField;

        private Component(final MultiValueOption<T> option, List<T> defaultValue) {
            super(new BorderLayout());
            textField = new GTextField(StringUtilities.join(SEPARATOR, defaultValue));
            textField.setMinimumSize(textField.getPreferredSize());
            textField.setEditable(false);
            textField.setColumns(Math.max(Math.min(option.getValueAsString().length(), 100), 15));
            add(textField, BorderLayout.CENTER);
            add(new JButton(new AbstractAction("Edit") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SetterOptions<T> options = new SetterOptions<T>(option);
                    boolean change = Dialogs.showOptionsDialog(options, "Edit", true);
                    if(change) {
                        option.setValue(options.getValues());
                    }
                }
            }), BorderLayout.EAST);
        }
    }

    private static class SetterOptions<T extends Number> extends Options {
        private static Options.OptionValue EXACT = new Options.OptionValue("exactly", "Specify Exactly");
        private static Options.OptionValue STEPS = new Options.OptionValue("steps", "Specify Using Steps");

        private MultiValueOption<T> option;
        private RadioOption<OptionValue> methodOption;
        StepSizeOptions<T> stepSizeOptions;
        private MultipleOptions exactValues;

        private SetterOptions(MultiValueOption<T> option) {
            super(MultiValueOption.class);
            this.option = option;

            methodOption = addRadioOption("method",
                    "", new Options.OptionValue[]{EXACT, STEPS}, STEPS,
                    Options.Alignment.VERTICAL_ALIGN);
            stepSizeOptions = new StepSizeOptions<T>(option);
            addChildOptions("steps", "", null, stepSizeOptions);
            methodOption.addDependent(STEPS, stepSizeOptions, true);

            Options exactOptions = new Options(MultiValueOption.class);
            addChildOptions("exact", "", null, exactOptions);
            Options template = new Options(MultiValueOption.class);
            option.addOption(template, option.getName(), option.getLabel(), true);
            exactValues = exactOptions.addMultipleOptions("value", template, false);
            methodOption.addDependent(EXACT, exactOptions, true);
        }

        private List<T> getValues() {
            if(methodOption.getValue() == EXACT) {
                List<T> result = new ArrayList<T>();
                List<Options> values = exactValues.getValues();
                for (Options value : values) {
                    result.add(option.getSingleValueFromString(value.getValueAsString(option.getName())));
                }
                return result;
            } else if(methodOption.getValue() == STEPS) {
                return stepSizeOptions.getValues();
            } else {
                throw new IllegalStateException("Impossible option chosen: " + methodOption.getValue());
            }
        }
    }
}
