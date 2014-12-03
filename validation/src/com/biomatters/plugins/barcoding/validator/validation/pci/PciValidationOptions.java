package com.biomatters.plugins.barcoding.validator.validation.pci;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import org.jdom.Element;

/**
 * @author Matthew Cheung
 *         Created on 3/12/14 1:02 PM
 */
public class PciValidationOptions extends ValidationOptions {

    private static final String BARCODES_OPTION = "barcodesFile";

    public PciValidationOptions() {
        super(PciValidationOptions.class);


        addFileSelectionOption(BARCODES_OPTION, "Barcodes:", "");
    }

    public PciValidationOptions(Element element) throws XMLSerializationException {
        super(element);
    }

    @Override
    public String getIdentifier() {
        return "pci";
    }

    @Override
    public String getLabel() {
        return "PCI";
    }

    @Override
    public String getDescription() {
        return "Determines if adding a sequence to the set of barcode sequences improves the probability of correct identification.";
    }

    public String getPathToBarcodesFile() {
        return getValueAsString(BARCODES_OPTION);
    }
}
