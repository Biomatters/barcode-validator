package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.plugin.Options;
import jebl.evolution.align.scores.Scores;
import jebl.evolution.align.scores.ScoresFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matt Kearse
 * @version $Id$
 */

public class CostMatrixOption extends Options.ComboBoxOption<CostMatrixOption.CostMatrixValue> {
    public CostMatrixOption(String name, String label, boolean isNucleotide) {
        super(name, label, getCostMatrices(isNucleotide).values, getCostMatrices(isNucleotide).defaultScore);
    }

    public static class CostMatrixValue extends Options.OptionValue {
        private Scores scores;

        private CostMatrixValue(Scores scores) {
            super(scores.getName(), scores.toString());
            this.scores = scores;
        }

        public Scores getScores() {
            return scores;
        }
    }

    protected void setValueOnComponent(JComboBox component, CostMatrixValue value) {
        super.setValueOnComponent(component,(Options.OptionValue)value);
    }

    private static class CostMatrixValues {
        final List<CostMatrixValue> values;
        final CostMatrixValue defaultScore;
        CostMatrixValues(List<CostMatrixValue> values, CostMatrixValue defaultScore){
            this.values = values;
            this.defaultScore = defaultScore;
        }
    }

    private static CostMatrixValues getCostMatrices(boolean isNucleotide){
        Scores[] availableScores;
        int defaultIndex = -1;
        if (isNucleotide) {
            availableScores = ScoresFactory.getAvailableNucleotideScores();
            for (int i = 0; i < availableScores.length; i++) {
                // searching for 5/-4 scores
                if (availableScores[i].getScore('A', 'G') == -4.0)
                    defaultIndex = i;
            }
        } else {
            availableScores = ScoresFactory.getAvailableAminoAcidScores();
            for (int i = 0; i < availableScores.length; i++) {
                if (availableScores[i].toString().equalsIgnoreCase("Blosum62"))
                    defaultIndex = i;
            }
        }
        CostMatrixValue defaultScore = null;
        List<CostMatrixValue> scores = new ArrayList<CostMatrixValue>();
        for (int i = 0; i < availableScores.length; i++) {
            Scores  score = availableScores[i];
            CostMatrixValue scoresValue = new CostMatrixValue(score);
            scores.add(scoresValue);
            if (i == defaultIndex)  defaultScore = scoresValue;
        }
        return new CostMatrixValues(scores, defaultScore);
    }
}