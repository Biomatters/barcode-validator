package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

/**
 * @author Frank Lee
 *         Created on 13/11/14 6:42 PM
 */
public class BooleanResultColumn extends ResultColumn<Boolean>  {

    public BooleanResultColumn(String name) {
        super(name);
    }

    @SuppressWarnings("unused")
    public BooleanResultColumn(Element element) throws XMLSerializationException {
        super(element);
    }

    @Override
    protected void setDataFromString(String str) {
        setData(Boolean.valueOf(str));
    }

    @Override
    public Object getDisplayValue() {
        String displayValue = data ? "Pass" : "Fail";
        String colour = data ? "green" : "red";
        return "<html><font color=\"" + colour + "\">" + displayValue + "</font></html>";
    }
}
