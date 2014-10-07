package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.components.GTextPane;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
import com.biomatters.plugins.barcoding.validator.output.RecordOfValidationResult;
import com.biomatters.plugins.barcoding.validator.output.ValidationOutputRecord;
import com.biomatters.plugins.barcoding.validator.output.ValidationReportDocument;
import com.biomatters.plugins.barcoding.validator.validation.BarcodeConsensusValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.SlidingWindowValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.LinkedHashMultimap;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

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

        final DocumentOpeningHyperlinkListener hyperlinkListener = new DocumentOpeningHyperlinkListener("ReportDocumentFactory");
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
        List<ValidationOutputRecord> records = reportDocument.getRecords();
        return getHeaderOfReport(records, reportDocument.getDescriptionOfOptions()) + getResultsTableForReport(records);
    }

    private static String getHeaderOfReport(List<ValidationOutputRecord> records, String descriptionOfOptionUsed) {
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
        headerBuilder.append(descriptionOfOptionUsed);

        headerBuilder.append("<br><br>").append(
                "Ran validations on <strong>").append(records.size()).append("</strong> sets of barcode data:");

        headerBuilder.append("<ul>");
        if(!allFailed) {
            appendHeaderLineItem(headerBuilder, "green", allPassed, recordsThatPassedAll, "passed all validations");
        }
        if(!allPassed) {
            appendHeaderLineItem(headerBuilder, "red", allFailed, recordsThatFailedAtLeastOnce,
                    "failed at least one validations");
        }
        headerBuilder.append("</ul>");

        return headerBuilder.toString();
    }

    private static void appendHeaderLineItem(StringBuilder headerBuilder, String colour, boolean isAll, List<ValidationOutputRecord> records, String whatHappened) {
        List<URN> barcodeUrns = new ArrayList<URN>();
        for (ValidationOutputRecord record : records) {
            barcodeUrns.add(record.getBarcodeSequenceUrn());
        }

        String countString = (isAll ? "All " : "") + records.size();
        headerBuilder.append(
                "<li><font color=\"").append(colour).append("\"><strong>").append(
                countString).append("</strong></font> sets ").append(whatHappened).append(". ").append(
                getLinkForSelectingDocuments("Select all", barcodeUrns));
    }

    private static String getResultsTableForReport(List<ValidationOutputRecord> records) {
        final ArrayListMultimap<String, BarcodeAndStatus> typeToResultsList = ArrayListMultimap.create();
        LinkedHashMultimap<String, String> typeGroupMap = LinkedHashMultimap.create();
        for (ValidationOutputRecord record : records) {
            for (RecordOfValidationResult resultsForBarcode : record.getValidationResults()) {
                ValidationOptions options = resultsForBarcode.getOptions();
                typeToResultsList.put(options.getLabel(), new BarcodeAndStatus(getDocumentUrn(record, options), resultsForBarcode.isPassed()));
                typeGroupMap.put(options.getGroup(), options.getLabel());
            }
        }
        if(typeToResultsList.isEmpty()) {
            return "<font color=\"red\"><strong>WARNING</strong></font>:There were no validation tasks run.";
        }

        List<String> typeListSorted = new ArrayList<String>();
        List<String> groupListSorted = new ArrayList<String>(typeGroupMap.keySet());
        Collections.sort(groupListSorted);

        StringBuilder builder = new StringBuilder("<h2>Results</h3><table border=\"1\">");
        builder.append("<tr><td></td>");
        for (String group : groupListSorted) {
            Set<String> types = typeGroupMap.get(group);
            builder.append("<td colspan=\"").append(types.size()).append("\">").append(group).append("</td>");
            typeListSorted.addAll(types);
        }

        builder.append("<tr><td>Set Name</td>");

        for (String label : typeListSorted) {
            List<BarcodeAndStatus> resultsForType = typeToResultsList.get(label);
            Collection<BarcodeAndStatus> passed = getResultsForStatus(resultsForType, true);
            Collection<BarcodeAndStatus> failed = getResultsForStatus(resultsForType, false);

            builder.append("<td>").append(label).append(" (").append(
                    getStatusLinks(passed, "Passed")).append("/").append(
                    getStatusLinks(failed, "Failed")).append(")</td>");
        }
        builder.append("</tr>");

        for (int i = 0; i < records.size(); i++) {
            ValidationOutputRecord record = records.get(i);
            appendLineForBarcode(builder, i, typeListSorted, record);
        }
        builder.append("</table>");
        return builder.toString();
    }

    private static List<URN> getDocumentUrn(ValidationOutputRecord record, ValidationOptions options) {
        List<URN> ret = new ArrayList<URN>();
        if (options instanceof SlidingWindowValidationOptions)
            ret.addAll(record.getTraceDocumentUrns());
        else if (options instanceof BarcodeConsensusValidationOptions)
            ret.add(record.getBarcodeSequenceUrn());
        else
            throw new IllegalArgumentException("Can not recognize validation options : " + options.getClass());

        return ret;
    }

    private static class BarcodeAndStatus {
        private List<URN> docsUrn;
        private boolean passed;

        private BarcodeAndStatus(List<URN> docsUrn, boolean passed) {
            this.docsUrn = docsUrn;
            this.passed = passed;
        }
    }

    private static void appendLineForBarcode(StringBuilder builder, int indexInTable, List<String> typeListSorted, ValidationOutputRecord record) {
        builder.append("<tr><td>").append(getInputLink(record, indexInTable)).append("</td>");
        Map<String, RecordOfValidationResult> recordsByName = new HashMap<String, RecordOfValidationResult>();
        for (RecordOfValidationResult recordOfValidationResult : record.getValidationResults()) {
           recordsByName.put(recordOfValidationResult.getOptions().getLabel(), recordOfValidationResult);
        }
        for (String typeIdentifier : typeListSorted) {
            RecordOfValidationResult result = recordsByName.get(typeIdentifier);
            builder.append("<td>");
            if(result != null) {
                builder.append(getResultString(result));
            }
            builder.append("</td>");
        }
        builder.append("</tr>");
    }

    private static String getInputLink(ValidationOutputRecord record, int indexInTable) {
        AnnotatedPluginDocument document = DocumentUtilities.getDocumentByURN(record.getBarcodeSequenceUrn());
        String label;
        if(document == null) {
            label = "Set " + (indexInTable+1);
        } else {
            label = document.getName();
        }
        List<URN> inputUrns = new ArrayList<URN>();
        inputUrns.add(record.getBarcodeSequenceUrn());
        for (URN urn : record.getTraceDocumentUrns()) {
            inputUrns.add(urn);
        }

        return getLinkForSelectingDocuments(label, inputUrns);
    }

    private static String getLinkForSelectingDocuments(String label, List<URN> documentUrns) {
        List<String> urnStrings = new ArrayList<String>();
        for (URN inputUrn : documentUrns) {
            urnStrings.add(inputUrn.toString());
        }
        return "<a href=\"" + StringUtilities.join(",", urnStrings) + "\">" + label + "</a>";
    }

    private static String getStatusLinks(Collection<BarcodeAndStatus> passed, String status) {
        if(passed.isEmpty()) {
            return "0 " + status;
        }
        List<URN> urns = new ArrayList<URN>();
        for (BarcodeAndStatus barcodeAndStatus : passed) {
            urns.addAll(barcodeAndStatus.docsUrn);
        }
        return getLinkForSelectingDocuments(passed.size() + " " + status, urns);
    }

    private static Collection<BarcodeAndStatus> getResultsForStatus(Collection<BarcodeAndStatus> results, final boolean passed) {
        return Collections2.filter(results, new Predicate<BarcodeAndStatus>() {
            @Override
            public boolean apply(@Nullable BarcodeAndStatus input) {
                return input != null && input.passed == passed;
            }
        });
    }

    private static Icon TICK_ICON = IconUtilities.getIcons("tick16.png").getIcon16();
    private static Icon CROSS_ICON = IconUtilities.getIcons("x16.png").getIcon16();

    private static String getResultString(RecordOfValidationResult result) {
        String resultString = "<img src=\"" + IconUtilities.createIconUrl(
                result.isPassed() ? TICK_ICON : CROSS_ICON) + "\"></img>";
        resultString += result.getMessage();
        if(!result.getGeneratedDocuments().isEmpty()) {
            resultString += " " + getLinkForSelectingDocuments("Generated Documents", result.getGeneratedDocuments());
        }
        return resultString;
    }
}
