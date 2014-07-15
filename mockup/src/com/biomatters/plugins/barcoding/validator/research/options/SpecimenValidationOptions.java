package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorMockupPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthew Cheung
 *         Created on 15/07/14 4:00 PM
 */
public class SpecimenValidationOptions extends Options {

    public SpecimenValidationOptions() {
        super(BarcodeValidatorMockupPlugin.class);
        addStringOption("country", "Required Countries:", "USA, Mexico, Kenya");

        List<OptionValue> possibleValues = new ArrayList<OptionValue>();
        possibleValues.add(new OptionValue("kingdom", "Kingdom"));
        possibleValues.add(new OptionValue("phylum", "Phylum"));
        possibleValues.add(new OptionValue("class", "Class"));
        possibleValues.add(new OptionValue("order", "Order"));
        possibleValues.add(new OptionValue("family", "Family"));
        possibleValues.add(new OptionValue("genus", "Genus"));
        OptionValue defaultValue = new OptionValue("species", "Species");
        possibleValues.add(defaultValue);

        addComboBoxOption("taxon", "Required Lowest Taxon:", possibleValues, defaultValue);
    }
}
