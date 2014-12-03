package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.OligoSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionOption;
import com.biomatters.geneious.publicapi.plugin.Options;
import jebl.evolution.align.scores.Scores;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 21/10/14 3:13 PM
 */
public class PrimerTrimmingOptions extends Options {
    static final double DEFAULT_GAP_OPEN = 12.0;
    static final double DEFAULT_GAP_EXTEND = 3.0;

    private DocumentSelectionOption primerSelectionOption;
    private DoubleOption gapOpenPenaltyOption;
    private DoubleOption gapExtensionPenaltyOption;
    private CostMatrixOption costMatrixOption;
    private IntegerOption maximumMismatchesOption;
    private IntegerOption minimumMatchLengthOption;

    public PrimerTrimmingOptions(Class cls) {
        super(cls);

        addPrimerSelectionOption();
        addGapOpenPenaltyOption();
        addGapExtensionPenaltyOption();
        addSimilarityOption();
        addMaximumMismatchesOption();
        addMinimumMatchLengthOption();
    }

    public List<OligoSequenceDocument> getPrimers() throws DocumentOperationException {
        List<OligoSequenceDocument> primers = new ArrayList<OligoSequenceDocument>();

        for (AnnotatedPluginDocument document : primerSelectionOption.getDocuments()) {
            if (document.getDocumentClass().isAssignableFrom(OligoSequenceDocument.class)) {
                primers.add((OligoSequenceDocument)document.getDocument());
            }
        }

        return primers;
    }

    public double getGapOptionPenalty() {
        return gapOpenPenaltyOption.getValue();
    }

    public double getGapExtensionPenalty() {
        return gapExtensionPenaltyOption.getValue();
    }

    public Scores getScores() {
        return costMatrixOption.getValue().getScores();
    }

    public int getMaximumMismatches() {
        return maximumMismatchesOption.getValue();
    }

    public int getMinimumMatchLength() {
        return minimumMatchLengthOption.getValue();
    }

    private void addPrimerSelectionOption() {
        primerSelectionOption = addPrimerSelectionOption(
                "primers",
                "Primers:",
                DocumentSelectionOption.FolderOrDocuments.EMPTY,
                true,
                Collections.<AnnotatedPluginDocument>emptyList()
        );

        addButtonOption("clear", "", "Clear Selection").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                primerSelectionOption.setValue(DocumentSelectionOption.FolderOrDocuments.EMPTY);
            }
        });
    }

    private void addGapOpenPenaltyOption() {
        gapOpenPenaltyOption = addDoubleOption("gapOpenPenalty", "Gap Open Penalty:", DEFAULT_GAP_OPEN, 0.0, 99999.0);
    }

    private void addGapExtensionPenaltyOption() {
        gapExtensionPenaltyOption = addDoubleOption("gapExtensionPenalty", "Gap Extension Penalty:", DEFAULT_GAP_EXTEND, 0.0, 99999.0);
    }

    private void addSimilarityOption() {
        costMatrixOption = addCustomOption(new CostMatrixOption("similarity", "Similarity (%):", true));
    }

    private void addMaximumMismatchesOption() {
        maximumMismatchesOption = addIntegerOption("maximumMismatches", "Maximum Mismatches", 5, 1, Integer.MAX_VALUE);
    }

    private void addMinimumMatchLengthOption() {
        minimumMatchLengthOption = addIntegerOption("minimumMatchLength", "Minimum Match Length", 15, 1, Integer.MAX_VALUE);
    }

    public boolean getHasPrimerTrimmed() {
        try {
            return getPrimers().size() > 0;
        } catch (DocumentOperationException e) {
            e.printStackTrace();
            return false;
        }
    }

}