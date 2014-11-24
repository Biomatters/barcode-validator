package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Frank Lee
 *         Created on 11/11/14 5:11 PM
 */
public class MuscleAlignmentValidationResultEntry extends ValidationResultEntry {
    private List<MuscleAlignmentValidationResultFact> facts = new ArrayList<MuscleAlignmentValidationResultFact>();

    @SuppressWarnings("unused")
    public MuscleAlignmentValidationResultEntry(Element element) throws XMLSerializationException {
        super(element);
    }

    public MuscleAlignmentValidationResultEntry() {
        super("Barcode Validation");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List getRow() {
        List ret = new ArrayList();
        for (MuscleAlignmentValidationResultFact fact : facts) {
            for (ResultColumn column : fact.getColumns()) {
                ret.add(column.getDisplayValue());
            }
        }
        return ret;
    }

    @Override
    public List<String> getCol() {
        List<String> ret = new ArrayList<String>();
        for (MuscleAlignmentValidationResultFact fact : facts) {
            for (ResultColumn column : fact.getColumns()) {
                ret.add(column.getName());
            }
        }
        return ret;
    }

    @Override
    public List<ResultColumn> getColumns() {
        List<ResultColumn> ret = new ArrayList<ResultColumn>();
        for (MuscleAlignmentValidationResultFact fact : facts) {
            ret.addAll(fact.getColumns());
        }
        return ret;
    }

    @Override
    public List<ResultFact> getResultFacts() {
        List<ResultFact> ret = new ArrayList<ResultFact>();
        ret.addAll(facts);
        return ret;
    }

    @Override
    public void addResultFact(ResultFact fact) {
        if (fact instanceof MuscleAlignmentValidationResultFact) {
            facts.add((MuscleAlignmentValidationResultFact)fact);
        }
    }
}