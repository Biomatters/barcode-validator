package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.plugins.barcoding.validator.output.PCICalculatorReportDocument;
import com.biomatters.plugins.barcoding.validator.validation.pci.PCICalculator;
import com.biomatters.plugins.barcoding.validator.validation.pci.PCICalculatorOptions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import javax.swing.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Frank Lee
 *         Created on 20/01/15 5:14 PM
 */
public class PCICalculatorOperation extends DocumentOperation {
    private static final Icons ICONS;

    static {
        URL icon = BarcodeValidatorOperation.class.getResource("barcodePCI.png");
        if (icon != null) {
            ICONS = new Icons(new ImageIcon(icon));
        } else {
            ICONS = null;
        }
    }

    @Override
    public GeneiousActionOptions getActionOptions() {
        return new GeneiousActionOptions("PCI Calculator", "", ICONS).setInMainToolbar(true).setMainMenuLocation(GeneiousActionOptions.MainMenu.Tools);
    }

    @Override
    public String getHelp() {
        return null;
    }

    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[]{
                new DocumentSelectionSignature(SequenceAlignmentDocument.class, 1, Integer.MAX_VALUE)
        };
    }

    @Override
    public void performOperation(AnnotatedPluginDocument[] annotatedPluginDocuments,
                                 ProgressListener progressListener,
                                 Options options,
                                 SequenceSelection sequenceSelection,
                                 OperationCallback operationCallback) throws DocumentOperationException {
        if (annotatedPluginDocuments == null || annotatedPluginDocuments.length == 0) {
            Dialogs.showMessageDialog("Please select alignment document that you want to run PCI calculator on.");
            return;
        }

        CompositeProgressListener composite = new CompositeProgressListener(progressListener, 0.1, 0.9);
        PCICalculatorOptions pciCalculatorOptions = (PCICalculatorOptions) options;

        try {
            for (AnnotatedPluginDocument doc : annotatedPluginDocuments) {
                SequenceAlignmentDocument alignmentDocument = (SequenceAlignmentDocument) doc.getDocument();
                List<AnnotatedPluginDocument> referencedDocuments = alignmentDocument.getReferencedDocuments();
                Map<URN, Double> result = null;
                if (referencedDocuments != null && referencedDocuments.size() > 0) {
                    Map<String, PCICalculator.GenusAndSpecies> nameToGenusAndSpeciesMap = ValidationUtils.getNameToGenusAndSpeciesMap(pciCalculatorOptions, referencedDocuments);
                    BiMap<String, AnnotatedPluginDocument> newSamples = getNewSamples(nameToGenusAndSpeciesMap, referencedDocuments);
                    result = PCICalculator.parseAlignment(alignmentDocument, newSamples, composite);
                }

                result = result == null ? new HashMap<URN, Double>() : result;
                operationCallback.addDocument(new PCICalculatorReportDocument(alignmentDocument.getName(), result), false, composite);
            }
        } finally {
            composite.setComplete();
        }
    }

    private BiMap<String, AnnotatedPluginDocument> getNewSamples(Map<String, PCICalculator.GenusAndSpecies> nameToGenusAndSpeciesMap, List<AnnotatedPluginDocument> referencedDocuments) throws DocumentOperationException {
        BiMap<String, AnnotatedPluginDocument> ret = HashBiMap.create();
        for (AnnotatedPluginDocument apd : referencedDocuments) {
            String name = apd.getName();
            PCICalculator.GenusAndSpecies genusAndSpecies = nameToGenusAndSpeciesMap.get(name);

            String uidForNewSample = PCICalculator.getUid(genusAndSpecies, apd.getName());
            if (ret.containsKey(uidForNewSample)) {
                // If there are duplicate names, we'll just give it a random UUID
                uidForNewSample = PCICalculator.getUid(genusAndSpecies, UUID.randomUUID().toString());
            }
            ret.put(uidForNewSample, apd);
        }
        return ret;
    }

    @Override
    public Options getOptions(AnnotatedPluginDocument... documents) throws DocumentOperationException {
        PCICalculatorOptions pciCalculatorOptions = new PCICalculatorOptions(PCICalculatorOptions.class);
        pciCalculatorOptions.setBarcodesSelectionVisable(false);
        return pciCalculatorOptions;
    }
}
