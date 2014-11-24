package com.biomatters.plugins.barcoding.validator.validation.results;


import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

/**
 * @author Gen Li
 *         Created on 21/11/14 8:02 AM
 */
public class StringResultColumn extends ResultColumn<String> {
    @SuppressWarnings("unused")
    public StringResultColumn(Element element) throws XMLSerializationException {
        super(element);
    }

    public StringResultColumn(String name) {
        super(name);
    }

    @Override
    protected void setDataFromString(String str) {
        setData(str);
    }
}
