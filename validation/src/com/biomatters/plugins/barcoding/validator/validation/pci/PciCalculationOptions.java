package com.biomatters.plugins.barcoding.validator.validation.pci;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import org.jdom.Element;

/**
 * @author Matthew Cheung
 *         Created on 3/12/14 1:02 PM
 */
public class PciCalculationOptions extends Options {

    private static final String BARCODES_OPTION = "barcodesFile";
    private static final String GENUS_OPTION = "genus";
    private static final String SPECIES_OPTION = "species";

    public PciCalculationOptions() {
        super(PciCalculationOptions.class);

        addStringOption(GENUS_OPTION, "Genus to Test:", "");
        addStringOption(SPECIES_OPTION, "Species to Test:", "");
        beginAlignHorizontally(null, false);
        addFileSelectionOption(BARCODES_OPTION, "Barcodes File:", "");
        addHelpButton("Help", "This file contains the reference barcode sequences used in the calculation of PCI values.  " +
                "The file must be in FASTA format and contain sequences named Genus_Species_ID.  " +
                "The ID cannot contain any spaces.");
        endAlignHorizontally();
    }

    public PciCalculationOptions(Element element) throws XMLSerializationException {
        super(element);
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
