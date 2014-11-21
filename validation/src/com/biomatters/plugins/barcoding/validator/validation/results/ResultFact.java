package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.XMLSerializable;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.documents.XMLSerializer;
import org.jdom.Element;

import java.util.List;

/**
 * @author Frank Lee
 *         Created on 11/11/14 5:00 PM
 */
public abstract class ResultFact implements XMLSerializable {
    public static final String NAME = "name";
    public static final String RESULT_COLUMN = "resultColumn";

    private String factName;

    public ResultFact() {
    }

    public ResultFact(String factName) {
        setFactName(factName);
    }

    public String getFactName() {
        return factName;
    }

    public void setFactName(String factName) {
        this.factName = factName;
    }

    public abstract List<ResultColumn> getColumns();
    public abstract void addColumn(ResultColumn column);

    public abstract boolean getPass();
    public abstract void setPass(boolean pass);

    @SuppressWarnings("UnusedDeclaration")
    public ResultFact(Element element) throws XMLSerializationException {
        String nameString = element.getChildText(NAME);
        setFactName(nameString);

        for (Element colElement : element.getChildren(RESULT_COLUMN)) {
            addColumn(XMLSerializer.classFromXML(colElement, ResultColumn.class));
        }
    }

    @Override
    public Element toXML() {
        Element root = new Element(XMLSerializable.ROOT_ELEMENT_NAME);
        root.addContent(new Element(NAME).setText(getFactName()));

        for (ResultColumn column : getColumns()) {
            root.addContent((XMLSerializer.classToXML(RESULT_COLUMN, column)));
        }

        return root;
    }

    @Override
    public void fromXML(Element element) throws XMLSerializationException {
        String nameString = element.getChildText(NAME);
        setFactName(nameString);

        for (Element colElement : element.getChildren(RESULT_COLUMN)) {
            addColumn(XMLSerializer.classFromXML(colElement, ResultColumn.class));
        }
    }
}
