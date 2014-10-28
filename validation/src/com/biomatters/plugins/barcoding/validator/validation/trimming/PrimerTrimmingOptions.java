package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.plugin.Options;
import jebl.evolution.align.scores.Scores;

/**
 * @author Gen Li
 *         Created on 21/10/14 3:13 PM
 */
public class PrimerTrimmingOptions extends Options {
    private static final String GAP_OPEN_PENALTY_OPTION_NAME = "gapOpenPenalty";
    private static final String GAP_EXTENSION_PENALTY_OPTION_NAME = "gapExtensionPenality";
    private static final String COST_MATRIX_OPTION_NAME = "costMatrix";

    public PrimerTrimmingOptions(Class cls) {
        super(cls);

        addGapOpenPenaltyOptions();
        addGapExtensionPenaltyOptions();
        addCostMatrixOptions();
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
