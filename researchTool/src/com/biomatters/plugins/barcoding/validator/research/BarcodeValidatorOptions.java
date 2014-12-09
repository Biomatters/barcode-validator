package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.validation.Validation;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.assembly.CAP3Options;
import com.biomatters.plugins.barcoding.validator.validation.pci.PciCalculatorOptions;
import com.biomatters.plugins.barcoding.validator.validation.trimming.TrimmingOptions;

import java.util.*;

/**
 * @author Gen Li
 *         Created on 3/09/14 2:42 PM
 */
public class BarcodeValidatorOptions extends Options {
    private TrimmingOptions trimmingOptions = new TrimmingOptions(BarcodeValidatorOptions.class);
    private CAP3Options assemblyOptions = new CAP3Options(BarcodeValidatorOptions.class);
    private Options validationOptions = new Options(BarcodeValidatorOptions.class);
    private PciCalculatorOptions PciCalculatorOptions = new PciCalculatorOptions(BarcodeValidatorOptions.class);
    
    public BarcodeValidatorOptions() {
        super(BarcodeValidatorOperation.class);

        addTrimmingOptions();
        addAssemblyOptions();
        addValidationOptions();
        addPCICalculationOptions();
    }

    public TrimmingOptions getTrimmingOptions() {
        return trimmingOptions;
    }

    public CAP3Options getAssemblyOptions() {
        return assemblyOptions;
    }

    public PciCalculatorOptions getPciCalculatorOptions() {
        return PciCalculatorOptions;
    }

    /**
     * @return Map of {@link com.biomatters.plugins.barcoding.validator.validation.ValidationOptions#getIdentifier()}
     * to {@link com.biomatters.plugins.barcoding.validator.validation.ValidationOptions} for all loaded
     * {@link com.biomatters.plugins.barcoding.validator.validation.Validation} objects.
     */
    public Map<String, ValidationOptions> getValidationOptions() {
        Map<String, ValidationOptions> identifiersToValidationOptions = new HashMap<String, ValidationOptions>();

        for (Map.Entry<String, Options> entry : validationOptions.getChildOptions().entrySet()) {
            identifiersToValidationOptions.put(entry.getKey(), (ValidationOptions)entry.getValue());
        }

        return Collections.unmodifiableMap(identifiersToValidationOptions);
    }

    private void addTrimmingOptions() {
        addCollapsibleChildOptions("trimming", "Trimming", "", trimmingOptions, false, false);
    }

    private void addValidationOptions() {
        List<Validation> validations = Validation.getValidations();
        if (!validations.isEmpty()) {
            for (Validation validation : Validation.getValidations()) {
                ValidationOptions options = validation.getOptions();
                validationOptions.addChildOptions(options.getIdentifier(), options.getLabel(), options.getDescription(), options, true);
            }

            validationOptions.addChildOptionsPageChooser("validationChooser", "Validation steps: ", Collections.<String>emptyList(), PageChooserType.BUTTONS, true);
        }

        addCollapsibleChildOptions("validation", "Validation", "", validationOptions, false, true);
    }

    private void addAssemblyOptions() {
        addCollapsibleChildOptions("assembly", "Assembly", "", assemblyOptions, false, true);
    }
    
    private void addPCICalculationOptions() {
        addCollapsibleChildOptions("pciCalculation", "PCI Calculation", "", PciCalculatorOptions, false, true);
    }
}