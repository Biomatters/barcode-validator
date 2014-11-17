package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

/**
 * @author Frank Lee
 *         Created on 13/11/14 6:42 PM
 */
public class IntegerResultColumn extends ResultColumn<Integer> {

    public IntegerResultColumn(String name) {
        super(name);
    }

    @SuppressWarnings("unused")
    public IntegerResultColumn(Element element) throws XMLSerializationException {
        super(element);
    }

    @Override
    protected void setDataFromString(String str) {
        setData(Integer.valueOf(str));
    }
}
