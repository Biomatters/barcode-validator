package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.XMLSerializable;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.documents.XMLSerializer;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Frank Lee
 *         Created on 11/11/14 4:56 PM
 */
public abstract class ValidationResultEntry implements XMLSerializable {
    private String name;
    private List<PluginDocument> intermediateDocumentsToAddToResults = new ArrayList<PluginDocument>();

    public abstract List<ResultFact> getResultFacts();
    public abstract void addResultFact(ResultFact fact);

    public static final String NAME = "name";
    public static final String RESULT_FACT = "resultFact";

    protected ValidationResultEntry(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PluginDocument> getIntermediateDocumentsToAddToResults() {
        return Collections.unmodifiableList(intermediateDocumentsToAddToResults);
    }

    public void addIntermediateDocument(PluginDocument document) {
        intermediateDocumentsToAddToResults.add(document);
    }

    @SuppressWarnings("UnusedDeclaration")
    public ValidationResultEntry(Element element) throws XMLSerializationException {
        String nameString = element.getChildText(NAME);
        setName(nameString);

        for (Element factElement : element.getChildren(RESULT_FACT)) {
            addResultFact(XMLSerializer.classFromXML(factElement, ResultFact.class));
        }
    }

    @Override
    public Element toXML() {
        Element root = new Element(XMLSerializable.ROOT_ELEMENT_NAME);
        root.addContent(new Element(NAME).setText(getName()));

        for (ResultFact fact : getResultFacts()) {
            root.addContent((XMLSerializer.classToXML(RESULT_FACT, fact)));
        }

        return root;
    }

    @Override
    public void fromXML(Element element) throws XMLSerializationException {
        String nameString = element.getChildText(NAME);
        setName(nameString);

        for (Element factElement : element.getChildren(RESULT_FACT)) {
            addResultFact(XMLSerializer.classFromXML(factElement, ResultFact.class));
        }
    }

    public abstract List getRow();
    public abstract List<String> getCol();
}
