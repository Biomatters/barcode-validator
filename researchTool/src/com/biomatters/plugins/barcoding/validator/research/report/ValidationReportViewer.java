package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.components.GTextPane;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
import com.biomatters.plugins.barcoding.validator.output.ValidationOutputRecord;
import com.biomatters.plugins.barcoding.validator.output.ValidationReportDocument;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;

/**
 * Displays a {@link com.biomatters.plugins.barcoding.validator.output.ValidationReportDocument} in a user
 * friendly manner using Java's HTML viewer.
 * <br/>
 * The view is static but does include links to input and output
 * {@link com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument} in Geneious.
 * <br/>
 * There are future plans to expand this to be more dynamic.  Allowing sorting and graphing of data.
 *
 * @author Matthew Cheung
 *         Created on 2/10/14 10:04 AM
 */
public class ValidationReportViewer extends DocumentViewer {
    ValidationReportDocument reportDocument;
    public ValidationReportViewer(ValidationReportDocument reportDocument) {
        this.reportDocument = reportDocument;
    }

    @Override
    public JComponent getComponent() {
        final JTextPane textPane = new GTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);

        final DocumentOpeningHyperlinkListener hyperlinkListener = new DocumentOpeningHyperlinkListener("MockupReportDocumentFactory");
        textPane.addHyperlinkListener(hyperlinkListener);
        textPane.setText(generateHtml(reportDocument));
        final JScrollPane scroll = new JScrollPane(textPane) {
            @Override
            public Dimension getPreferredSize() {
                // increase height by potential horizontal scroll bar height
                return new Dimension(super.getPreferredSize().width,super.getPreferredSize().height+30);
            }
        };
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.setBorder(null);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textPane.scrollRectToVisible(new Rectangle(0,0));
            }
        });
        return scroll;
    }

    private static String generateHtml(ValidationReportDocument reportDocument) {
        return getHeaderOfReport(reportDocument.getRecords());  // todo table of results by validation task
    }

    private static String getHeaderOfReport(List<ValidationOutputRecord> records) {
        List<ValidationOutputRecord> recordsThatPassedAll = new ArrayList<ValidationOutputRecord>();
        List<ValidationOutputRecord> recordsThatFailedAtLeastOnce = new ArrayList<ValidationOutputRecord>();
        for (ValidationOutputRecord record : records) {
            if(record.isAllPassed()) {
                recordsThatPassedAll.add(record);
            } else {
                recordsThatFailedAtLeastOnce.add(record);
            }
        }

        boolean allPassed = recordsThatPassedAll.size() == records.size();
        boolean allFailed = recordsThatFailedAtLeastOnce.size() == records.size();

        StringBuilder headerBuilder = new StringBuilder("<h1>Validation Report</h1>");
        appendOptionsParameters(headerBuilder);

        headerBuilder.append("<br><br><br>").append(
                "Ran validations on <strong>").append(records.size()).append("</strong> sets of barcode data:");

        headerBuilder.append("<ul>");
        if(!allFailed) {
            appendHeaderLineItem(headerBuilder, "green", allPassed, recordsThatPassedAll, "passed all validations");
        }
        if(!allPassed) {
            appendHeaderLineItem(headerBuilder, "red", allFailed, recordsThatFailedAtLeastOnce,
                    "failed at least one validations");
        }

        return headerBuilder.toString();
    }

    private static void appendOptionsParameters(StringBuilder headerBuilder) {
        // todo List the actual options rather than these fake ones
        headerBuilder.append("The following trimming and assembly parameters were used.<br>").append(
                        "Trimming: Max low quality bases=0, Min overlap identity=90%<br>").append(
                        "Assembly: Error Probability Limit=0.05<br>").append(
                        "Consensus Generation: Threshold=Highest Quality, Assign Quality=Total, No Coverage Call=?");
    }

    private static void appendHeaderLineItem(StringBuilder headerBuilder, String colour, boolean isAll, List<ValidationOutputRecord> records, String whatHappened) {
        List<String> barcodeUrnStrings = new ArrayList<String>();
        for (ValidationOutputRecord record : records) {
            URN barcodeUrn = record.getBarcodeSequenceUrn();
            if(barcodeUrn != null) {
                barcodeUrnStrings.add(barcodeUrn.toString());
            }
        }

        String countString = (isAll ? "All " : "") + records.size();
        headerBuilder.append(
                "<li><font color=\"").append(colour).append("\"><strong>").append(
                countString).append("</strong></font> sets ").append(whatHappened).append(". ").append(
                "<a href=\"").append(StringUtilities.join(",", barcodeUrnStrings)).append("\">Select all</a></li>");
    }
}
