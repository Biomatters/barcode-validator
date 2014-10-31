package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.documents.XMLSerializer;
import com.biomatters.geneious.publicapi.plugin.Options;
import org.jdom.Element;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthew Cheung
 *         Created on 31/10/14 11:18 AM
 */
public class DoubleMultiValueOption extends MultiValueOption<Double> {

    private Options.DoubleOption baseOption;
    public DoubleMultiValueOption(Options.DoubleOption option) {
        super(option.getName()+SUFFIX, option.getLabel(), option.getDefaultValue());
        this.baseOption = option;
    }

    private static final String BASE_OPTION_KEY = "baseOption";

    @Override
    public Element toXML() {
        Element root = super.toXML();
        root.addContent(XMLSerializer.classToXML(BASE_OPTION_KEY, baseOption));
        return root;
    }

    @SuppressWarnings("UnusedDeclaration")
    public DoubleMultiValueOption(Element element) throws XMLSerializationException {
        super(element);
        baseOption = XMLSerializer.classFromXML(element.getChild(BASE_OPTION_KEY), Options.DoubleOption.class);
    }

    @Override
    Double getSingleValueFromString(String valueString) {
        try {
            return Double.parseDouble(valueString);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    Options.Option<Double, ? extends JComponent> addOption(Options options, String name, String label) {
        return options.addDoubleOption(name, label, baseOption.getDefaultValue(), baseOption.getMinimum(), baseOption.getMaximum());
    }

    @Override
    List<Double> getForSteps(Double min, Double max, Double step) {
        List<Double> results = new ArrayList<Double>();
        for (double i = min; i < max; i=Double.valueOf(String.format("%.2f", i+step))) {
            results.add(i);
        }
        results.add(max);
        return results;
    }
}
