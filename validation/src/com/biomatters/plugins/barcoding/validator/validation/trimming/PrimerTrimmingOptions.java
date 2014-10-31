package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.OligoSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionOption;
import com.biomatters.geneious.publicapi.plugin.Options;
import jebl.evolution.align.scores.Scores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 21/10/14 3:13 PM
 */
public class PrimerTrimmingOptions extends Options {
    private static final String PRIMER_SELECTION_OPTION_NAME      = "primers";
    private static final String GAP_OPEN_PENALTY_OPTION_NAME      = "gapOpenPenalty";
    private static final String GAP_EXTENSION_PENALTY_OPTION_NAME = "gapExtensionPenalty";
    private static final String COST_MATRIX_OPTION_NAME           = "costMatrix";

    public PrimerTrimmingOptions(Class cls) {
        super(cls);

        addPrimerSelectionOption();
        addGapOpenPenaltyOptions();
        addGapExtensionPenaltyOptions();
        addCostMatrixOptions();
    }

    public List<OligoSequenceDocument> getPrimers() throws DocumentOperationException {
        List<OligoSequenceDocument> primers = new ArrayList<OligoSequenceDocument>();

        for (AnnotatedPluginDocument document : ((DocumentSelectionOption)getOption(PRIMER_SELECTION_OPTION_NAME)).getDocuments()) {
            if (document.getDocumentClass().isAssignableFrom(OligoSequenceDocument.class)) {
                primers.add((OligoSequenceDocument)document.getDocument());
            }
        }

        return primers;
    }

    public Scores getScores() {
        return ((CostMatrixOption)getOption(COST_MATRIX_OPTION_NAME)).getValue().getScores();
    }

    public double getGapOptionPenalty() {
        return ((DoubleOption)getOption(GAP_OPEN_PENALTY_OPTION_NAME)).getValue();
    }

    public double getGapExtensionPenalty() {
        return ((DoubleOption)getOption(GAP_EXTENSION_PENALTY_OPTION_NAME)).getValue();
    }

    private void addPrimerSelectionOption() {
        addPrimerSelectionOption(PRIMER_SELECTION_OPTION_NAME, "Primers", DocumentSelectionOption.FolderOrDocuments.EMPTY, true, Collections.<AnnotatedPluginDocument>emptyList());
    }

    private void addCostMatrixOptions() {
        addCustomOption(new CostMatrixOption(COST_MATRIX_OPTION_NAME, "Cost Matrix:", true));
    }

    private void addGapOpenPenaltyOptions() {
        addDoubleOption(GAP_OPEN_PENALTY_OPTION_NAME, "Gap open penalty:", 12.0, 0.0, 99999.0);
    }

    private void addGapExtensionPenaltyOptions() {
        addDoubleOption(GAP_EXTENSION_PENALTY_OPTION_NAME, "Gap extension penalty:", 3.0, 0.0, 99999.0);
    }
}
