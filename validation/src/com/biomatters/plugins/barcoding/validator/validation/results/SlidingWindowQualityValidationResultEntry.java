package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Frank Lee
 *         Created on 11/11/14 5:11 PM
 */
public class SlidingWindowQualityValidationResultEntry extends ValidationResultEntry {
    private List<SlidingWindowQualityValidationResultFact> facts = new ArrayList<SlidingWindowQualityValidationResultFact>();

    @SuppressWarnings("unused")
    protected SlidingWindowQualityValidationResultEntry(String name) {
        super(name);
    }

    @SuppressWarnings("unused")
    public SlidingWindowQualityValidationResultEntry(Element element) throws XMLSerializationException {
        super(element);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List getRow() {
        List ret = new ArrayList();
        for (SlidingWindowQualityValidationResultFact fact : facts) {
            for (ResultColumn column : fact.getColumns()) {
                ret.add(column.getDisplayValue());
            }
        }
        return ret;
    }

    @Override
    public List<String> getCol() {
        List<String> ret = new ArrayList<String>();
        for (SlidingWindowQualityValidationResultFact fact : facts) {
            for (ResultColumn column : fact.getColumns()) {
                ret.add(column.getName());
            }
        }
        return ret;
    }

    @Override
    public List<ResultColumn> getColumns() {
        List<ResultColumn> ret = new ArrayList<ResultColumn>();
        for (SlidingWindowQualityValidationResultFact fact : facts) {
            ret.addAll(fact.getColumns());
        }
        return ret;
    }

    public SlidingWindowQualityValidationResultEntry() {
        super("Quality Validation (Sliding Window)");
    }

    @Override
    public List<ResultFact> getResultFacts() {
        List<ResultFact> ret = new ArrayList<ResultFact>();
        ret.addAll(facts);
        return ret;
    }

    @Override
    public void addResultFact(ResultFact fact) {
        if (fact instanceof SlidingWindowQualityValidationResultFact) {
            addStatsFact((SlidingWindowQualityValidationResultFact)fact);
        }
    }

    public void addStatsFact(SlidingWindowQualityValidationResultFact fact) {
        facts.add(fact);
    }

    @Override
    public void align(List<ValidationResultEntry> list) {
        int max = 0;
        for (ValidationResultEntry entry : list) {
            if (!(entry instanceof SlidingWindowQualityValidationResultEntry)) continue;
            int size = entry.getResultFacts().size();
            max = max > size ? max : size;
        }

        for (ValidationResultEntry entry : list) {
            if (!(entry instanceof SlidingWindowQualityValidationResultEntry)) continue;
            int size = entry.getResultFacts().size();
            for (int i = 0; i < max - size; i++) {
                SlidingWindowQualityValidationResultFact tmp = new SlidingWindowQualityValidationResultFact("tmp");
                tmp.setFactName("Trace " + (size + i + 1));
                ((SlidingWindowQualityValidationResultEntry)entry).addStatsFact(tmp);
            }
        }
    }
}