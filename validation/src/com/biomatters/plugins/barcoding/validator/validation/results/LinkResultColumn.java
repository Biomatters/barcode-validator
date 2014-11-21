package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.*;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Frank Lee
 *         Created on 13/11/14 6:42 PM
 */
public class LinkResultColumn extends ResultColumn<LinkResultColumn.LinkBox> {

    public LinkResultColumn(String name) {
        super(name);
        data = new LinkBox();
    }

    @SuppressWarnings("UnusedDeclaration")
    public LinkResultColumn(Element element) throws XMLSerializationException {
        super(element);
        fromXML(element);
    }

    @Override
    protected void setDataFromString(String str) {}

    @Override
    public Element toXML() {
        Element root = new Element(XMLSerializable.ROOT_ELEMENT_NAME);
        root.addContent(new Element(NAME).setText(getName()));
        if (data != null) {
            root.addContent(new Element(LinkBox.LABEL).setText(data.getLabel()));
            List<URN> links = data.getLinks();
            for (URN link : links) {
                if (link != null) root.addContent(link.toXML(LinkBox.LINK));
            }
        }

        return root;
    }

    @Override
    public void fromXML(Element element) throws XMLSerializationException {
        name = element.getChildText(NAME);
        String lable = element.getChildText(LinkBox.LABEL);
        ArrayList<URN> links = new ArrayList<URN>();

        try {
            for (Element ele : element.getChildren(LinkBox.LINK)) {
                links.add(URN.fromXML(ele));
            }
        } catch (MalformedURNException e) {
            throw new XMLSerializationException("Failed to de-serialize validation record: " + e.getMessage(), e);
        }

        data = new LinkBox(lable, links);
    }

    @Override
    public LinkBox getData() {
        if (data == null) data = new LinkBox();
        return data;
    }

    public static class LinkBox {
        protected static final String LABEL = "label";
        protected static final String LINK = "link";

        private String label;
        private List<URN> links;

        public LinkBox(String label, List<URN> links) {
            this.label = label;
            this.links = links;
        }

        public LinkBox() {
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public List<URN> getLinks() {
            if (links == null) links = new ArrayList<URN>();
            return links;
        }

        public void setLinks(List<URN> links) {
            this.links = links;
        }

        public void addLink(URN link) {
            getLinks().add(link);
        }

        @Override
        public String toString() {
            return label;
        }

        public void openLink() {
            if (links != null) DocumentUtilities.selectDocuments(links);
        }
    }
}