package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.XMLSerializable;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

/**
 * @author Frank Lee
 *         Created on 12/11/14 12:19 PM
 */
public abstract class ResultColumn<T> implements XMLSerializable {
    protected static final String NAME = "name";
    protected static final String DATA = "data";
    protected String name;
    protected T data;

    public ResultColumn(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Object getDisplayValue() {
        return data;
    }

    protected abstract void setDataFromString(String str);

    @SuppressWarnings("UnusedDeclaration")
    public ResultColumn(Element element) throws XMLSerializationException {
        name = element.getChildText(NAME);
        setDataFromString(element.getChildText(DATA));
    }

    @Override
    public Element toXML() {
        Element root = new Element(XMLSerializable.ROOT_ELEMENT_NAME);
        root.addContent(new Element(NAME).setText(getName()));
        root.addContent(new Element(DATA).setText(data.toString()));
        return root;
    }

    @Override
    public void fromXML(Element element) throws XMLSerializationException {
        name = element.getChildText(NAME);
        setDataFromString(element.getChildText(DATA));
    }
}
