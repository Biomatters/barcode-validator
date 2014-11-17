package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

/**
 * @author Frank Lee
 *         Created on 13/11/14 6:42 PM
 */
public class DoubleResultColumn extends ResultColumn<Double> {

    public DoubleResultColumn(String name) {
        super(name);
    }

    @SuppressWarnings("unused")
    public DoubleResultColumn(Element element) throws XMLSerializationException {
        super(element);
    }

    @Override
    protected void setDataFromString(String str) {
        setData(Double.valueOf(str));
    }

    @Override
    public Object getDisplayValue() {
        return data * 100;
    }
}
