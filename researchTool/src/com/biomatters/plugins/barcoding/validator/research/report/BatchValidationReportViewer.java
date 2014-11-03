package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.MalformedURNException;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.plugins.barcoding.validator.output.ValidationOutputRecord;
import com.biomatters.plugins.barcoding.validator.output.ValidationReportDocument;

import javax.swing.event.HyperlinkListener;
import java.util.*;

/**
 * @author Matthew Cheung
 *         Created on 4/11/14 11:36 AM
 */
public class BatchValidationReportViewer extends HtmlReportDocumentViewer {
    private Map<URN, ValidationReportDocument> reports = new HashMap<URN, ValidationReportDocument>();

    public BatchValidationReportViewer(AnnotatedPluginDocument[] reportApds) {
        for (AnnotatedPluginDocument reportApd : reportApds) {
            if(ValidationReportDocument.class.isAssignableFrom(reportApd.getDocumentClass())) {
                ValidationReportDocument report = (ValidationReportDocument) reportApd.getDocumentOrCrash();
                reports.put(reportApd.getURN(), report);
            }
        }
    }


    @Override
    public String getHtml() {
        if(reports.isEmpty()) {
            return null;
        }

        boolean barcodesNotTheSame = false;
        Set<URN> barcodeUrns = null;
        List<Row> rows = new ArrayList<Row>();
        int numPassedEverything = 0;
        for (Map.Entry<URN, ValidationReportDocument> entry : reports.entrySet()) {
            HashSet<URN> barcodesInThisReport = new HashSet<URN>();
            ValidationReportDocument report = entry.getValue();
            int count = 0;
            boolean passedEverything = true;
            for (ValidationOutputRecord validationOutputRecord : report.getRecords()) {
                barcodesInThisReport.add(validationOutputRecord.getBarcodeSequenceUrn());
                if(validationOutputRecord.isAllPassed()) {
                    count++;
                } else {
                    passedEverything = false;
                }
            }
            rows.add(new Row(entry.getKey(), count, report.getRecords().size() - count));
            if(passedEverything) {
                numPassedEverything++;
            }
            if(barcodeUrns == null) {
                barcodeUrns = barcodesInThisReport;
            } else {
                if(!barcodeUrns.equals(barcodesInThisReport)) {
                    barcodesNotTheSame = true;
                }
            }
        }
        if(rows.isEmpty()) {
            return null;
        }

        Collections.sort(rows, Collections.reverseOrder(new Comparator<Row>() {
            @Override
            public int compare(Row o1, Row o2) {
                return o1.numAllPassed - o2.numAllPassed;
            }
        }));

        return "<h1>Summary of " + reports.size() + " Validation Runs</h1>" +
                "<br><br>" +
                (barcodesNotTheSame ? "<font color=\"red\">Warning: Input barcode sequences for selected reports do not match</font><br><br>" : "") +
                numPassedEverything + " out of " + reports.size() + " passed all validations on all data sets." +
                "<br><br>" + getHtmlTable(rows);
    }

    private static final String SHOW_OPTIONS_PREFIX = "showOptions:";

    private static String getHtmlTable(List<Row> rows) {
        StringBuilder html = new StringBuilder("<table border=\"1\"><tr>" +
                "<td>Parameters</td>" +
                "<td># Validations Passing All</td>" +
                "<td># Validations Failed At Least One</td>" +
        "</tr>");
        for (Row row : rows) {
            html.append("<tr>");
            html.append("<td>").append(getReportLink(row)).append(" - ").append(getShowOptionsLink(row)).append("</td>");
            html.append("<td>").append(row.numAllPassed).append("</td>");
            html.append("<td>").append(row.numFailedAtLeastOne).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        return html.toString();
    }

    private static String getReportLink(Row row) {
        return "<a href=\"" + row.urn.toString() + "\">Report</a>";

    }

    private static String getShowOptionsLink(Row row) {
        return "<a href=\"" + SHOW_OPTIONS_PREFIX + row.urn.toString() + "\">Show Options</a>";
    }

    private class Row {
        URN urn;
        int numAllPassed;
        int numFailedAtLeastOne;

        private Row(URN urn, int numAllPassed, int numFailedAtLeastOne) {
            this.urn = urn;
            this.numAllPassed = numAllPassed;
            this.numFailedAtLeastOne = numFailedAtLeastOne;
        }
    }

    @Override
    public HyperlinkListener getHyperlinkListener() {
        return new DocumentOpeningHyperlinkListener("BatchValidationReportViewerFactory",
                Collections.<String, DocumentOpeningHyperlinkListener.UrlProcessor>singletonMap(SHOW_OPTIONS_PREFIX,
                new DocumentOpeningHyperlinkListener.UrlProcessor() {
                    @Override
                    void process(String url) {
                        String urnString = url.substring(SHOW_OPTIONS_PREFIX.length());
                        try {
                            URN urn = new URN(urnString);
                            ValidationReportDocument report = reports.get(urn);
                            if(report != null) {
                                Dialogs.showMessageDialog(report.getDescriptionOfOptions(), "Parameters");
                            }
                        } catch (MalformedURNException e) {
                            e.printStackTrace();
                        }
                    }
                })
        );
    }
}
