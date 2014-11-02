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
 *         Created on 31/10/14 10:02 AM
 */
public class IntegerMultiValueOption extends MultiValueOption<Integer> {

    private Options.IntegerOption baseOption;
    public IntegerMultiValueOption(Options.IntegerOption option) {
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
    public IntegerMultiValueOption(Element element) throws XMLSerializationException {
        super(element);
        baseOption = XMLSerializer.classFromXML(element.getChild(BASE_OPTION_KEY), Options.IntegerOption.class);
    }

    @Override
    Integer getSingleValueFromString(String valueString) {
        try {
            return Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    String singleValueToString(Integer singleValue) {
        return singleValue.toString();
    }

    @Override
    Options.Option<Integer, ? extends JComponent> addOption(Options options, String name, String label, boolean useMinMax) {
        if(useMinMax) {
            return options.addIntegerOption(name, label, baseOption.getDefaultValue(), baseOption.getMinimum(), baseOption.getMaximum());
        } else {
            return options.addIntegerOption(name, label, 0);
        }
    }

    @Override
    List<Integer> getForSteps(Integer min, Integer max, Integer step) {
        List<Integer> results = new ArrayList<Integer>();
        for (int i = min; i < max; i+=step) {
            results.add(i);
        }
        results.add(max);
        return results;
    }
}
