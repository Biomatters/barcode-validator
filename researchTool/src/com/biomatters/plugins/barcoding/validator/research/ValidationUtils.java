package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.documents.*;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.GeneralUtilities;
import com.biomatters.plugins.barcoding.validator.output.ValidationOutputRecord;
import com.biomatters.plugins.barcoding.validator.validation.pci.PCICalculator;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Frank Lee
 *         Created on 21/01/15 12:01 PM
 */
public class ValidationUtils {

    public static final String PCI_VALUES_KEY = "pciValues";
    public static final String PCI_ENTRY_KEY = "entry";
    public static final String URN_KEY = "urn";
    public static final String VALUE_KEY = "pci";


    public static Map<String, PCICalculator.GenusAndSpecies> getNameToGenusAndSpeciesMap(
            com.biomatters.plugins.barcoding.validator.validation.pci.PCICalculatorOptions pciCalculatorOptions, Collection<AnnotatedPluginDocument> barcodeSequences
    ) throws DocumentOperationException {

        Map<String, PCICalculator.GenusAndSpecies> nameToGenusAndSpecies = new HashMap<String, PCICalculator.GenusAndSpecies>();
        File taxonMappingFile = pciCalculatorOptions.getTaxonMappingFile();
        if(pciCalculatorOptions.isUseInputFile()) {
            for (AnnotatedPluginDocument barcode : barcodeSequences) {
                String barcodeName = barcode.getName();
                PCICalculator.GenusAndSpecies genusAndSpecies = pciCalculatorOptions.getGenusAndSpeciesFromLine(barcodeName);
                if (genusAndSpecies != null) {
                    nameToGenusAndSpecies.put(barcodeName, genusAndSpecies);
                }
            }
        } else if(taxonMappingFile != null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(taxonMappingFile));
                String currentLine;
                while((currentLine = reader.readLine()) != null) {
                    nameToGenusAndSpecies.put(pciCalculatorOptions.getNameFromLine(currentLine), pciCalculatorOptions.getGenusAndSpeciesFromLine(currentLine));
                }
            } catch (IOException e) {
                throw new DocumentOperationException("Failed to read from mapping file");
            } finally {
                GeneralUtilities.attemptClose(reader);
            }
        }
        return nameToGenusAndSpecies;
    }

    /**
     *
     * @param outputRecords Records of the output from running the validation pipeline
     * @param genusAndSpeciesMap A map from barcode name to genus and species
     * @return A map of all URNs to genus and species for the documents that will be validated.  This includes the
     * trimmed traces and any consensus sequences assembled from them.
     */
    public static Map<URN, PCICalculator.GenusAndSpecies> getUrnToGenusAndSpecies(List<ValidationOutputRecord> outputRecords, Map<String, PCICalculator.GenusAndSpecies> genusAndSpeciesMap) throws DocumentOperationException {
        Map<URN, PCICalculator.GenusAndSpecies> result = new HashMap<URN, PCICalculator.GenusAndSpecies>();

        for (ValidationOutputRecord output : outputRecords) {
            if(output.getBarcodeSequenceUrn() == null) {
                throw new IllegalStateException("No barcode URN for output record");
            }
            AnnotatedPluginDocument barcodeDoc = DocumentUtilities.getDocumentByURN(output.getBarcodeSequenceUrn());
            if(barcodeDoc == null) {
                throw new DocumentOperationException("Failed to find barcode sequence (" +
                        output.getBarcodeSequenceUrn().toString() + ").  This should have been saved to the database.");
            }

            PCICalculator.GenusAndSpecies genusAndSpecies = genusAndSpeciesMap.get(barcodeDoc.getName());
            URN consensusUrn = output.getConsensusUrn();
            if (consensusUrn != null) {
                result.put(consensusUrn, genusAndSpecies);
            }
            for (URN urn : output.getTrimmedDocumentUrns()) {
                result.put(urn, genusAndSpecies);
            }
        }

        return result;
    }

    /**
     * The UID consists of Genus_Species_ID.
     *
     * <strong>Note</strong>: Any white space in the sequence name will be replaced by an dash "-".  This is because the
     * compressed barcode format used as input for the PCI program is based on strict FASTA, which does not have spaces
     * in the names of sequences.  <a href="http://www.ncbi.nlm.nih.gov/CBBresearch/Spouge/html_ncbi/html/bib/119.html#The%20Format%20of%20an%20Compressed%20Barcode%20File">Details here.</a>.
     * Underscore is replaced because it is a special character in the UID used as a separator.
     *
     * @param genusAndSpecies The genus and species
     * @param name The name of the sequence
     * @return The UID of the sequence as it should appear in the compressed barcode format.
     *
     */
    public static String getUid(@Nullable PCICalculator.GenusAndSpecies genusAndSpecies, @Nonnull String name) {
        String sanitizedName = name.replaceAll("[_\\s]+", "-");
        if(genusAndSpecies == null) {
            return "Unknown_Unknown_" + sanitizedName;
        } else {
            return genusAndSpecies.genus + "_" + genusAndSpecies.species + "_" + sanitizedName;
        }
    }

    public static Element pciValuesToXml(Map<URN, Double> pciValues) {
        Element pciElement = new Element(PCI_VALUES_KEY);
        for (Map.Entry<URN, Double> entry : pciValues.entrySet()) {
            Element valueElement = new Element(PCI_ENTRY_KEY);
            valueElement.addContent(entry.getKey().toXML(URN_KEY));
            valueElement.addContent(new Element(VALUE_KEY).setText(String.valueOf(entry.getValue())));
            pciElement.addContent(valueElement);
        }
        return pciElement;
    }

    public static Map<URN, Double> pciValuesMapFromXml(Element pciElement, boolean silence) throws XMLSerializationException {
        Map<URN, Double> result = new HashMap<URN, Double>();
        List<Element> entryElements = pciElement.getChildren(PCI_ENTRY_KEY);
        for (Element entryElement : entryElements) {
            String valueText = entryElement.getChildText(VALUE_KEY);
            try {
                URN urn = URN.fromXML(entryElement.getChild(URN_KEY));
                Double pci = Double.valueOf(valueText);
                result.put(urn, pci);
            } catch (MalformedURNException e) {
                if (!silence) {
                    throw new XMLSerializationException("Bad URN stored in report document: " + e.getMessage(), e);
                }
            } catch (NumberFormatException e) {
                if (!silence) {
                    throw new XMLSerializationException("Bad PCI value (" + valueText + ") stored in report document: " + e.getMessage(), e);
                }
            }
        }
        return result;
    }
}
