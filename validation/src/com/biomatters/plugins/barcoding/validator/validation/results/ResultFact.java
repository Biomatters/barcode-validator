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

    private String name;

    public ResultFact() {
    }

    public ResultFact(String name) {
        this.name = name;
    }

    public String getFactName() {
        return name;
    }

    public void setFactName(String name) {
        this.name = name;
    }

    public abstract List<ResultColumn> getColumns();
    public abstract void addColumns(ResultColumn column);

    @SuppressWarnings("UnusedDeclaration")
    public ResultFact(Element element) throws XMLSerializationException {
        String nameString = element.getChildText(NAME);
        setFactName(nameString);

        for (Element colElement : element.getChildren(RESULT_COLUMN)) {
            addColumns(XMLSerializer.classFromXML(colElement, ResultColumn.class));
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
            addColumns(XMLSerializer.classFromXML(colElement, ResultColumn.class));
        }
    }
}
