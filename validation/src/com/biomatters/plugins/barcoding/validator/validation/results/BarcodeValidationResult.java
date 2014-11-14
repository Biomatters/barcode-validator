package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Frank Lee
 *         Created on 11/11/14 5:11 PM
 */
public class BarcodeValidationResult extends ValidationResultEntry {
    private StatusFact consensusFact = new StatusFact("Consensus");

    public BarcodeValidationResult(Element element) throws XMLSerializationException {
        super(element);
    }

    public BarcodeValidationResult() {
        super("Barcode Validation");
    }

    @Override
    public List getRow() {
        List<ResultColumn> columns = consensusFact.getColumns();
        List ret = new ArrayList();
        for (ResultColumn column : columns) {
            ret.add(column.getDisplayValue());
        }
        return ret;
    }

    @Override
    public List<String> getCol() {
        List<ResultColumn> columns = consensusFact.getColumns();
        List<String> ret = new ArrayList<String>();
        for (ResultColumn column : columns) {
            ret.add(column.getName());
        }
        return ret;
    }

    @Override
    public List<ResultFact> getResultFacts() {
        List<ResultFact> ret = Collections.singletonList((ResultFact)consensusFact);
        return ret;
    }

    @Override
    public void addResultFact(ResultFact fact) {
        setConsensusFact((StatusFact) fact);
    }

    public void setConsensusFact(StatusFact consensusFact) {
        this.consensusFact = consensusFact;
    }
}
