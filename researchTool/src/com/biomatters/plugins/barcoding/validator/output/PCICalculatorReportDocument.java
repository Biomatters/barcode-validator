package com.biomatters.plugins.barcoding.validator.output;

import com.biomatters.geneious.publicapi.documents.*;
import com.biomatters.geneious.publicapi.plugin.Geneious;
import com.biomatters.plugins.barcoding.validator.research.ValidationUtils;
import jebl.util.ProgressListener;
import org.jdom.Element;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Frank Lee
 *         Created on 22/01/15 5:08 PM
 */
public class PCICalculatorReportDocument implements PluginDocument, XMLSerializable.OldVersionCompatible {
    private static final String NAME_KEY = "name";
    private Map<URN, Double> result;
    private String name;

    @SuppressWarnings("unused")
    public PCICalculatorReportDocument() {
    }

    public PCICalculatorReportDocument(String name, Map<URN, Double> result) {
        this.name = name;
        this.result = result;
    }

    public Map<URN, Double> getResult() {
        return result;
    }

    @Override
    public Geneious.MajorVersion getVersionSupport(VersionSupportType versionType) {
        return Geneious.MajorVersion.Version6_0;
    }

    @Override
    public List<DocumentField> getDisplayableFields() {
        return null;
    }

    @Override
    public Object getFieldValue(String fieldCodeName) {
        return null;
    }

    @Override
    public String getName() {
        return "PCI score for " + name;
    }

    @Override
    public URN getURN() {
        return null;
    }

    @Override
    public Date getCreationDate() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String toHTML() {
        return null;
    }

    @Override
    public Element toXML(Geneious.MajorVersion majorVersion, ProgressListener progressListener) {
        Element element = new Element(XMLSerializable.ROOT_ELEMENT_NAME);
        element.addContent(new Element(NAME_KEY).setText(name));
        element.addContent(ValidationUtils.pciValuesToXml(result));
        return element;
    }

    @Override
    public Element toXML() {
        return toXML(Geneious.getMajorVersion(), ProgressListener.EMPTY);
    }

    @Override
    public void fromXML(Element element) throws XMLSerializationException {
        name = element.getChildText(NAME_KEY);
        if(name == null) {
            name = "";
        }

        Element pciElement = element.getChild(ValidationUtils.PCI_VALUES_KEY);
        if(pciElement != null) {
            result = ValidationUtils.pciValuesMapFromXml(pciElement, true);
        }
    }
}
