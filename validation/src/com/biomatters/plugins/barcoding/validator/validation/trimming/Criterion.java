package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import jebl.evolution.sequences.NucleotideState;
import jebl.evolution.sequences.Nucleotides;

/**
 * @author Amy Wilson
 *          <p/>
 *          Created on 4/12/2008 10:57:12 AM
 */
abstract class Criterion {
    private int failedBases = 0;
    private final int allowedFailedBases;

    Criterion(int allowedFailedBases) {
        this.allowedFailedBases = allowedFailedBases;
    }

    abstract boolean meets(int index); // 1-offset


    static boolean hasQualityInformation(NucleotideSequenceDocument document) {
        return document instanceof NucleotideGraphSequenceDocument
                && ((NucleotideGraphSequenceDocument)document).hasSequenceQualities();
    }

    void incrementFailedBases() {
        assert failedBases <= allowedFailedBases;
        failedBases++;
    }
    void decrementFailedBases() {
        failedBases--;
        assert failedBases >= 0;
    }

    boolean exceededFailedBaseLimit() {
        return failedBases > allowedFailedBases;
    }

}
class AmbiguityCriterion extends Criterion {
    private final CharSequence sequence;

    AmbiguityCriterion(CharSequence sequence, int allowedFailedBases) {
        super(allowedFailedBases);
        this.sequence = sequence;
    }

    public boolean meets(int index) {
        NucleotideState state = Nucleotides.getState(sequence.charAt(index - 1));
        return state == null || !state.isAmbiguous();
    }
}
class QualityCriterion extends Criterion {
    private final NucleotideGraphSequenceDocument document;

    QualityCriterion(NucleotideGraphSequenceDocument document, int allowedFailedBases) {
        super(allowedFailedBases);
        this.document = document;
    }

    public boolean meets(int index) {
        return document.getSequenceQuality(index -1) >= 20; // todo: Add option for user to change this
    }
}
