package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

/**
 * @author Frank Lee
 *         Created on 13/11/14 6:42 PM
 */
public class StringResultColumn extends ResultColumn<String> {

    public StringResultColumn(String name) {
        super(name);
    }

    public StringResultColumn(Element element) throws XMLSerializationException {
        super(element);
    }

    @Override
    protected void setDataFromString(String str) {
        setData(str);
    }
}
