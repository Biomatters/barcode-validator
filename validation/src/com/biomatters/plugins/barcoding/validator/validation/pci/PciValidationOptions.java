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
    private static final String GENUS_OPTION = "genus";
    private static final String SPECIES_OPTION = "species";

    public PciValidationOptions() {
        super(PciValidationOptions.class);

        addStringOption(GENUS_OPTION, "Genus to Test:", "");
        addStringOption(SPECIES_OPTION, "Species to Test:", "");
        addFileSelectionOption(BARCODES_OPTION, "Barcodes:", "");
        addHelpButton("Help", "The file contains reference barcode sequences used in the calculation of PCI values.  " +
                "The file must be in FASTA format and contain sequences named Genus_Species_ID.  " +
                "The ID cannot contain any spaces.");
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

    public String getGenus() {
        return getValueAsString(GENUS_OPTION);
    }

    public String getSpecies() {
        return getValueAsString(SPECIES_OPTION);
    }
}
