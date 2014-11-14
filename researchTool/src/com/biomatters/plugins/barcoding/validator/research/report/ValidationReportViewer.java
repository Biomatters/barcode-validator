package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.components.GPanel;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
import com.biomatters.plugins.barcoding.validator.output.RecordOfValidationResult;
import com.biomatters.plugins.barcoding.validator.output.ValidationOutputRecord;
import com.biomatters.plugins.barcoding.validator.output.ValidationReportDocument;
import com.biomatters.plugins.barcoding.validator.research.report.table.ColumnGroup;
import com.biomatters.plugins.barcoding.validator.research.report.table.GroupableTableHeader;
import com.biomatters.plugins.barcoding.validator.research.report.table.GroupableTableHeaderUI;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.results.LinkResultColumn;
import com.biomatters.plugins.barcoding.validator.validation.results.ResultFact;
import com.biomatters.plugins.barcoding.validator.validation.results.ValidationResultEntry;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.LinkedHashMultimap;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
public class ValidationReportViewer extends HtmlReportDocumentViewer {
    ValidationReportDocument reportDocument;
    public ValidationReportViewer(ValidationReportDocument reportDocument) {
        this.reportDocument = reportDocument;
    }

    @Override
    public String getHtml() {
        return generateHtml(reportDocument);
    }

    public static final String OPTION_PREFIX = "option:";
    @Override
    public HyperlinkListener getHyperlinkListener() {
        final Map<String, ValidationOptions> optionsMap = getOptionMap(reportDocument);
        return new DocumentOpeningHyperlinkListener("ReportDocumentFactory",
                Collections.<String, DocumentOpeningHyperlinkListener.UrlProcessor>singletonMap(OPTION_PREFIX,
                new DocumentOpeningHyperlinkListener.UrlProcessor() {
                    @Override
                    void process(String url) {
                        final String optionLable = url.substring(OPTION_PREFIX.length());
                        final ValidationOptions options = optionsMap.get(optionLable);

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                options.setEnabled(false);
                                Dialogs.DialogOptions dialogOptions = new Dialogs.DialogOptions(Dialogs.OK_ONLY, "Options");
                                Dialogs.showMoreOptionsDialog(dialogOptions, options.getPanel(), options.getAdvancedPanel());
                            }
                        });
                    }
                }));
    }

    private static Map<String, ValidationOptions> getOptionMap(ValidationReportDocument reportDocument) {
        Map<String, ValidationOptions> ret = new HashMap<String, ValidationOptions>();
        if (reportDocument == null || reportDocument.getRecords() == null)
            return ret;

        List<ValidationOutputRecord> records = reportDocument.getRecords();
        for (ValidationOutputRecord record : records) {
            for (RecordOfValidationResult result : record.getValidationResults()) {
                ValidationOptions options = result.getOptions();
                ret.put(options.getLabel(), options);
            }
        }

        return ret;
    }

    private static String generateHtml(ValidationReportDocument reportDocument) {
        List<ValidationOutputRecord> records = reportDocument.getRecords();
        return getHeaderOfReport(records, reportDocument.getDescriptionOfOptions());
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
                typeGroupMap.put(options.getGroup().getLabel(), options.getLabel());
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
                    getStatusLinks(failed, "Failed"))
                    .append(")<br/><a href=\"").append(OPTION_PREFIX).append(label).append("\">[Show options]</a>").append("</td>");
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
        switch(options.getGroup()) {
            case TRACE_VALIDATION_GROUP:
                return Collections.unmodifiableList(record.getTraceDocumentUrns());
            case BARCODE_VALIDATION_GROUP:
                return Collections.singletonList(record.getBarcodeSequenceUrn());
            default:
                throw new IllegalArgumentException("Can not recognize validation options : " + options.getClass());
        }
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
        StringBuilder sb = new StringBuilder();
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

        sb.append(getLinkForSelectingDocuments(label, inputUrns));

        List<URN> trimmedDocumentUrns = record.getTrimmedDocumentUrns();
        if (trimmedDocumentUrns != null && trimmedDocumentUrns.size() > 0) {
            int size = trimmedDocumentUrns.size();
            sb.append("\t").append(getLinkForSelectingDocuments("[" + size + (size > 1 ? " Traces]" : " Trace]"), trimmedDocumentUrns));
        }

        List<URN> allLinks = new ArrayList<URN>();
        allLinks.addAll(inputUrns);

        if (trimmedDocumentUrns != null) {
            allLinks.addAll(trimmedDocumentUrns);
        }
        sb.append("\t").append(getLinkForSelectingDocuments("[All]", allLinks));

        return sb.toString();
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

    @Override
    public JComponent getComponent() {
        JComponent textPane = super.getComponent();

        GPanel rootPanel = new GPanel(new BorderLayout());
        final JScrollPane scroll = new JScrollPane(rootPanel) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width,super.getPreferredSize().height+300);
            }
        };
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.setBorder(null);

        rootPanel.add(textPane, BorderLayout.NORTH);
        JTable table = getTable();
        if (table != null)
            rootPanel.add(new JScrollPane(table));

        return scroll;
    }

    public JTable getTable() {
        List<ValidationOutputRecord> records = reportDocument.getRecords();

        //get data
        List<List> rows = new ArrayList<List>();
        int indexInTable = 0;
        for (ValidationOutputRecord record : records) {
            indexInTable++;
            List row = new ArrayList();
            AnnotatedPluginDocument document = DocumentUtilities.getDocumentByURN(record.getBarcodeSequenceUrn());
            String label;
            if(document == null) {
                label = "Set " + indexInTable;
            } else {
                label = document.getName();
            }

            List<URN> links = new ArrayList<URN>();
            links.add(record.getBarcodeSequenceUrn());
            for (URN urn : record.getTraceDocumentUrns()) {
                links.add(urn);
            }
            row.add(new LinkResultColumn.LinkBox(label, links));


            label = "" + record.getTrimmedDocumentUrns().size();
            links = record.getTrimmedDocumentUrns();
            row.add(new LinkResultColumn.LinkBox(label, links));

            label = "" + (record.getAssemblyUrn() == null ? 0 : 1);
            links = new ArrayList<URN>();
            links.add(record.getAssemblyUrn());
            row.add(new LinkResultColumn.LinkBox(label, links));

            for (RecordOfValidationResult result : record.getValidationResults()) {
                ValidationResultEntry entry = result.getEntry();
                List row1 = entry.getRow();
                for (Object col : row1) {
                    if (col instanceof LinkResultColumn.LinkBox) {
                        LinkResultColumn.LinkBox col1 = (LinkResultColumn.LinkBox) col;
                        String col1Lable = col1.getLable();
                        URN urn = record.getTraceDocumentUrnByName(col1Lable);
                        if (urn != null) {
                            col1.addLink(urn);
                            continue;
                        }

                        urn = record.getgetTrimmedDocumentUrnByName(col1Lable);
                        if (urn != null) {
                            col1.addLink(urn);
                            continue;
                        }
                    }
                }
                row.addAll(row1);
            }

            rows.add(row);
        }

        //get header
        List<String> header = new ArrayList<String>();
        if (rows.size() == 0) {
            //todo
        } else {
            header.add("Set Name");
            header.add("# Traces");
            header.add("# Assembled");
            for (RecordOfValidationResult result : records.get(0).getValidationResults()) {
                ValidationResultEntry entry = result.getEntry();
                header.addAll(entry.getCol());
            }
        }

        //construct table
        String[] headers = header.toArray(new String[0]);
        Object[][] datas = new Object[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            datas[i] = rows.get(i).toArray();
        }

        DefaultTableModel dm = new DefaultTableModel(datas, headers) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex >= 0 && columnIndex < getColumnCount() && getValueAt(0, columnIndex) != null) {
                    return getValueAt(0, columnIndex).getClass();
                } else {
                    return Object.class;
                }
            }
        };

        JTable table = new JTable(dm) {
            @Override
            protected JTableHeader createDefaultTableHeader() {

                return new GroupableTableHeader(columnModel);
            }
        };

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Object source = e.getSource();
                if(source instanceof JTable) {
                    JTable table = (JTable) source;
                    Object cell = table.getValueAt(table.getSelectedRow(), table.getSelectedColumn());
                    if (cell instanceof LinkResultColumn.LinkBox) {
                        ((LinkResultColumn.LinkBox)cell).openLink();
                    }
                }
            }
        });

        //merge header
        TableColumnModel cm = table.getColumnModel();
        GroupableTableHeader head = (GroupableTableHeader) table.getTableHeader();
        int colIndex = 3;   //since we already have 3 other columns
        for (RecordOfValidationResult result : records.get(0).getValidationResults()) {
            ValidationResultEntry entry = result.getEntry();
            ColumnGroup entryGroup = new ColumnGroup(entry.getName());
            for (ResultFact fact : entry.getResultFacts()) {
                ColumnGroup factGroup = new ColumnGroup(fact.getFactName());
                for (int i = 0; i < fact.getColumns().size(); i++) {
                    factGroup.add(cm.getColumn(colIndex++));
                }

                entryGroup.add(factGroup);
            }

            head.addColumnGroup(entryGroup);
        }
        table.getTableHeader().setUI(new GroupableTableHeaderUI());
        table.setAutoCreateRowSorter(true);

        //set alignment to center
        DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();
        tcr.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, tcr);

        return table;
    }
}
