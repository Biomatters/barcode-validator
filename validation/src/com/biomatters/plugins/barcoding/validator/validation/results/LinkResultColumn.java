package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.*;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
    public void fromXML(Element element) throws XMLSerializationException  {
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

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]+");
        return pattern.matcher(str).matches();
    }

    public static class LinkBox implements Comparable<LinkBox> {
        protected static final String LABEL = "label";
        protected static final String LINK = "link";

        private String label;
        private List<URN> links;
        private List<PluginDocument> pluginDocuments = new ArrayList<PluginDocument>();

        public void addPluginDocument(PluginDocument pluginDocument) {
            pluginDocuments.add(pluginDocument);
        }

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
            if (links == null || links.size() == 0) {
                links = new ArrayList<URN>();

                for (PluginDocument pluginDocument : pluginDocuments) {
                    AnnotatedPluginDocument doc = DocumentUtilities.getAnnotatedPluginDocumentThatContains(pluginDocument);
                    if (doc != null) {
                        links.add(doc.getURN());
                    }
                }
            }
            return links;
        }

        public void setLinks(List<URN> links) {
            this.links = links;
        }

        @Override
        public String toString() {
            return "<html><a href=\"" + links + "\">" + label + "</a></html>";
        }

        public void openLink() {
            DocumentUtilities.selectDocuments(getLinks());
        }

        @Override
        public int compareTo(LinkBox o) {
            if (isNumeric(label) && isNumeric(o.getLabel())) {
                return Integer.parseInt(label) - Integer.parseInt(o.getLabel());
            }

            return label.compareTo(o.getLabel());
        }
    }
}