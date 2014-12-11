package com.biomatters.plugins.barcoding.validator.validation.pci;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.validation.input.map.NamePartOption;
import com.biomatters.plugins.barcoding.validator.validation.input.map.NameSeparatorOption;

/**
 * Options used to obtain genus and species from a file
 *
 * @author Matthew Cheung
 *         Created on 11/12/14 3:01 PM
 */
public class TaxonMappingOptions extends Options {

    final FileSelectionOption mapFileOption;
    final BooleanOption useInputFiles;
    final NameSeparatorOption separatorOption;
    final NamePartOption genusPartOption;
    final NamePartOption speciesPartOption;

    public TaxonMappingOptions() {
        super(TaxonMappingOptions.class);

        beginAlignHorizontally(null, false);
        mapFileOption = addFileSelectionOption("taxonMap", "Taxon Mapping File:", "");
        addHelpButton("Help", "This file should contain the mapping from your barcode sequence names as they appear in " +
                "your FASTA files to the genus and species of the barcode.  You may choose your own separator and where " +
                "the taxons fall in each line.\n\n<strong>Note</strong>: The name must always be the first element.");
        endAlignHorizontally();

        useInputFiles = addBooleanOption("useFasta", "Use names from my input files", false);
        useInputFiles.setDescription("This option causes the names of sequences contained in your FASTA file to be used " +
                "to obtain the mapping instead of a separate mapping file");

        separatorOption = addCustomOption(new NameSeparatorOption("separatorOption", "Separate each line with: "));
        genusPartOption = addCustomOption(new NamePartOption("genusPartOption", "Genus is ", 1));
        speciesPartOption = addCustomOption(new NamePartOption("speciesPartOption", "Species is ", 2));

        useInputFiles.addDependent(mapFileOption, false);
    }

    String getGenus(String line) {
        return getPartOfLine(line, genusPartOption, separatorOption);
    }

    String getSpecies(String line) {
        return getPartOfLine(line, speciesPartOption, separatorOption);
    }

    private static String getPartOfLine(String line, NamePartOption partOption, NameSeparatorOption separatorOption) {
        String[] parts = line.split(separatorOption.getSeparatorString());
        int index = partOption.getPart();
        if(index < parts.length) {
            return parts[index];
        } else {
            return null;
        }
    }
}
