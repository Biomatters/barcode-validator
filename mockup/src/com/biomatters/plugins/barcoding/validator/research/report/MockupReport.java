package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.documents.AbstractPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentField;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
import com.biomatters.plugins.barcoding.validator.research.data.Set;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Matthew Cheung
 *         Created on 19/08/14 4:26 PM
 */
public class MockupReport extends AbstractPluginDocument {

    public MockupReport() {
    }

    private List<Set> passed;
    // Map from set to comma separated failure reasons
    private Map<Set, String> failed;  // The real thing will actually need to provide a lot more info here.  Since it is a mockup we'll just use a simple String
    public MockupReport(String name, List<Set> passed, Map<Set, String> failed) {
        setFieldValue(DocumentField.NAME_FIELD, name);
        this.passed = passed;
        this.failed = failed;
    }

    private static final String PASSED = "passedSets";
    private static final String FAILED = "failedSets";
    private static final String REASON = "failureReason";

    @Override
    public Element toXML() {
        Element root = super.toXML();
        Element passedElement = new Element(PASSED);
        root.addContent(passedElement);
        for (Set set : passed) {
            passedElement.addContent(set.toXML());
        }
        Element failedElement = new Element(FAILED);
        root.addContent(failedElement);
        for (Map.Entry<Set, String> entry : failed.entrySet()) {
            Element failSetElement = entry.getKey().toXML();
            failSetElement.setAttribute(REASON, entry.getValue());
            failedElement.addContent(failSetElement);
        }
        return root;
    }

    @Override
    public void fromXML(Element root) throws XMLSerializationException {
        super.fromXML(root);
        Element passedSets = root.getChild(PASSED);
        passed = new ArrayList<Set>();
        for (Element element : passedSets.getChildren()) {
            passed.add(new Set(element));
        }

        failed = new LinkedHashMap<Set, String>();
        Element failedSets = root.getChild(FAILED);
        for (Element element : failedSets.getChildren()) {
            failed.put(new Set(element), element.getAttributeValue(REASON));
        }
    }

    @Override
    public String getName() {
        return String.valueOf(getFieldValue(DocumentField.NAME_FIELD.getCode()));
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String toHTML() {
        return getHeaderOfReport(passed.size(), failed.size()) + getResultsTableForReport(passed, failed);
    }

    private String getResultsTableForReport(List<Set> passed, Map<Set, String> failed) {
        StringBuilder builder = new StringBuilder("<h2>Results</h3>" +
                "<table border=\"1\">" +
                "<tr><td></td><td colspan=\"1\">Trace Validation</td><td colspan=\"3\">Barcode Validation</td></tr>" +
                "<tr>" +
                "<td>Set Name</td><td>Quality " + getSelectPassFailHtml(5, 2) + "<br>Show/List Options?</td>" +
                "<td>Matches FASTA " + getSelectPassFailHtml(3, 4) + "<br>Show/List? Options</td>" +
                "<td>Quality " + getSelectPassFailHtml(5, 2) + "<br>Show/List? Options</td>" +
                "<td>PCI " + getSelectPassFailHtml(7, 0) + "<br>Show/List? Options</td>" +
                "</tr>");
        int count = 1;
        for (Set set : passed) {
            builder.append("<tr><td>").append(getDocumentSelectionLink("Set " + count++, set)).append("</td>");
            for(int i=0; i<4; i++) {
                builder.append("<td>").append(tickHtml).append("</td>");
            }
            builder.append("</tr>");
        }
        for (Map.Entry<Set, String> entry : failed.entrySet()) {
            builder.append("<tr><td>").append(getDocumentSelectionLink("Set " + count++, entry.getKey())).append("</td>");
            String[] reasons = entry.getValue().split(",");
            for (String reason : reasons) {
                if(reason == null || reason.trim().isEmpty()) {
                    builder.append("<td>").append(tickHtml).append("</td>");
                } else {
                    if(reason.contains("do not match barcode")) {
                        reason = "Traces do not match FASTA <a href=\"urn:local:matthew:1w1-3nouyxu\">View Alignment</a>";
                    }
                    builder.append("<td title=\"More Details\">").append(crossHtml).append(reason).append("</td>");
                }
            }
            builder.append("</tr>");
        }
        return builder.toString();
    }

    private String getSelectPassFailHtml(int numPassed, int numFailed) {
        return " (<a href=\"\">" + numPassed + " Passed</a> / <a href=\"\">" + numFailed + " Failed</a>)";
    }

    private static String getDocumentSelectionLink(String name, Set set) {
        return "<a href=\"" + StringUtilities.join(",", set.getUrnStringList()) + "\">" + name + "</a>";
    }

    private String getHeaderOfReport(int passCount, int failedCount) {
        return "<h1>Validation Report Mockup</h1>" +
                "The following trimming and assembly parameters were used.<br>" +
                "Trimming: Max low quality bases=0, Min overlap identity=90%<br>" +
                "Assembly: Error Probability Limit=0.05<br>" +
                "<br><br>" +
                "Ran validations on <strong>" + (passCount + failedCount) + "</strong> sets of barcode data:" +
                "<ul>" +
                "<li><font color=\"green\"><strong>" + passCount + "</strong></font> Sets passed all validations. " +
                    "<a href=\"\">Select all passed</a>.</li>" +
                "<li><font color=\"red\"><strong>" + failedCount + "</strong></font> Sets failed at least one validation. See below." +
                " <a href=\"\">Select all failed</a>.</li>" +
                "</ul>" +
                "<br><br>";
    }

    private String tickHtml = getImageTag("tick16.png");
    private String crossHtml = getImageTag("x16.png");

    private static String getImageTag(String icon) {
        return "<img src=\"" + IconUtilities.createIconUrl(IconUtilities.getIcons(icon).getIcon16()) + "\"></img>"; // We need a matching </img> (rather than <img />) if running under Java 5
    }
}
