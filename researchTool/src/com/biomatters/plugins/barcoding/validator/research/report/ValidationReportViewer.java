package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.components.GPanel;
import com.biomatters.geneious.publicapi.components.GTable;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.plugin.ActionProvider;
import com.biomatters.geneious.publicapi.plugin.GeneiousAction;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.GeneralUtilities;
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
import com.biomatters.plugins.barcoding.validator.validation.results.ResultColumn;
import com.biomatters.plugins.barcoding.validator.validation.results.ResultFact;
import com.biomatters.plugins.barcoding.validator.validation.results.ValidationResultEntry;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

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
    JTable table = null;
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

    @SuppressWarnings("unused")
    private static class BarcodeAndStatus {
        private List<URN> docsUrn;
        private boolean passed;
    }

    private static String getLinkForSelectingDocuments(String label, List<URN> documentUrns) {
        List<String> urnStrings = new ArrayList<String>();
        for (URN inputUrn : documentUrns) {
            urnStrings.add(inputUrn.toString());
        }
        return "<a href=\"" + StringUtilities.join(",", urnStrings) + "\">" + label + "</a>";
    }

//    private static Icon TICK_ICON = IconUtilities.getIcons("tick16.png").getIcon16();
//    private static Icon CROSS_ICON = IconUtilities.getIcons("x16.png").getIcon16();

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
        if (table == null)
            table = getTable();

        if (table != null)
            rootPanel.add(new JScrollPane(table));

        return scroll;
    }

    @SuppressWarnings("unchecked")
    public JTable getTable() {
        List<ValidationOutputRecord> records = reportDocument.getRecords();

        List<List<ResultColumn>> columnList = getDataModels(records);

        //get header
        if (columnList.size() == 0) {
            return null;
        }

        List<String> header = new ArrayList<String>();
        List<ResultColumn> resultColumns = columnList.get(0);
        for (ResultColumn column : resultColumns) {
            header.add(column.getName());
        }

        //construct table
        String[] headers = header.toArray(new String[header.size()]);
        Object[][] datas = new Object[columnList.size()][];
        for (int i = 0; i < columnList.size(); i++) {
            datas[i] = new Object[columnList.get(i).size()];
            for (int j = 0; j < columnList.get(i).size(); j++) {
                datas[i][j] = columnList.get(i).get(j).getDisplayValue();
            }
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

        JTable table = new GTable(dm);
        GroupableTableHeader head = new GroupableTableHeader(table.getTableHeader());
        table.setTableHeader(head);
        TableCellRenderer headerRenderer = head.getDefaultRenderer();
        if(headerRenderer instanceof DefaultTableCellRenderer) {
            ((DefaultTableCellRenderer) headerRenderer).setHorizontalAlignment(SwingConstants.CENTER);
        }

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Object source = e.getSource();
                if (source instanceof JTable) {
                    JTable table = (JTable) source;
                    Object cell = table.getValueAt(table.getSelectedRow(), table.getSelectedColumn());
                    if (cell instanceof LinkResultColumn.LinkBox) {
                        ((LinkResultColumn.LinkBox) cell).openLink();
                    }
                }
            }
        });

        //merge header
        TableColumnModel cm = table.getColumnModel();
        int colIndex = 3;   //since we already have 3 other columns
        for (RecordOfValidationResult result : records.get(0).getValidationResults()) {
            ValidationResultEntry entry = result.getEntry();
            ColumnGroup entryGroup = new ColumnGroup(entry.getName(), headerRenderer);
            for (ResultFact fact : entry.getResultFacts()) {
                ColumnGroup factGroup = new ColumnGroup(fact.getFactName(), headerRenderer);
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

    @SuppressWarnings("unchecked")
    private List<List<ResultColumn>> getDataModels(List<ValidationOutputRecord> records) {
        Map<String, List<ResultColumn>> fixedColumns = new HashMap<String, List<ResultColumn>>();
        Map<Class, Map<String, ValidationResultEntry>> entryList = new LinkedHashMap<Class, Map<String, ValidationResultEntry>>();

        int indexInTable = 0;
        for (ValidationOutputRecord record : records) {
            //fixed columns: such as Set Name, #Traces, #Assembled
            indexInTable++;
            List<ResultColumn> fixedCols = new ArrayList();
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

            LinkResultColumn setCol = new LinkResultColumn("Set Name");
            setCol.setData(new LinkResultColumn.LinkBox(label, links));
            fixedCols.add(setCol);

            LinkResultColumn tracesCol = new LinkResultColumn("#Traces");
            tracesCol.setData(new LinkResultColumn.LinkBox("" + record.getTrimmedDocumentUrns().size(), record.getTrimmedDocumentUrns()));
            fixedCols.add(tracesCol);

            LinkResultColumn assemblefCol = new LinkResultColumn("#Assembled");
            links = new ArrayList<URN>();
            links.add(record.getAssemblyUrn());
            assemblefCol.setData(new LinkResultColumn.LinkBox("" + (record.getAssemblyUrn() == null ? 0 : DocumentUtilities.getDocumentByURN(record.getAssemblyUrn()).getReferencedDocuments().size() - 1), links));
            fixedCols.add(assemblefCol);

            fixedColumns.put(label, fixedCols);

            populateLinks(record);
            
            //put entry into catalog
            for (RecordOfValidationResult result : record.getValidationResults()) {
                ValidationResultEntry entry = result.getEntry();
                Map<String, ValidationResultEntry> entryMap = entryList.get(entry.getClass());
                if (entryMap == null) {
                    entryMap = new HashMap<String, ValidationResultEntry>();
                    entryList.put(entry.getClass(), entryMap);
                }

                entryMap.put(label, entry);
            }
        }

        //alignment
        for (Map<String, ValidationResultEntry> next : entryList.values()) {
            align(collection2List(next.values()));
        }

        //assembly
        List<List<ResultColumn>> ret = new ArrayList<List<ResultColumn>>();
        for (Map.Entry<String, List<ResultColumn>> entry : fixedColumns.entrySet()) {
            String key = entry.getKey();
            List<ResultColumn> value1 = entry.getValue();

            for (Map<String, ValidationResultEntry> entryMap : entryList.values()) {
                value1.addAll(entryMap.get(key).getColumns());
            }

            ret.add(value1);
        }

        return ret;
    }

    private void populateLinks(ValidationOutputRecord record) {
        for (RecordOfValidationResult result : record.getValidationResults()) {
            for (ResultColumn column : result.getEntry().getColumns()) {
                if (column instanceof LinkResultColumn) {
                    LinkResultColumn.LinkBox data = ((LinkResultColumn) column).getData();
                    String col1Lable = data.getLable();
                    URN urn = record.getTraceDocumentUrnByName(col1Lable);
                    if (urn != null) {
                        data.addLink(urn);
                        continue;
                    }

                    urn = record.getgetTrimmedDocumentUrnByName(col1Lable);
                    if (urn != null) {
                        data.addLink(urn);
                    }
                }
            }
        }
    }

    private List<ValidationResultEntry> collection2List(Collection<ValidationResultEntry> values) {
        List<ValidationResultEntry> ret = new ArrayList<ValidationResultEntry>();
        for (ValidationResultEntry entry : values) {
            ret.add(entry);
        }

        return ret;
    }

    private void align(List<ValidationResultEntry> values) {
        if (values == null || values.size() == 0) {
            return;
        }

        values.get(0).align(values);
    }

    @Override
    public ActionProvider getActionProvider() {
        return new ActionProvider() {
            @Override
            public List<GeneiousAction> getOtherActions() {
                List<GeneiousAction> ret = new ArrayList<GeneiousAction>();
                ret.add(new ExportReportAction("Export report table to csv file", table));
                return ret;
            }
        };

    }

    protected static class ExportReportAction extends GeneiousAction {

        private JTable table = null;

        public ExportReportAction(String name, JTable table) {
            super(name, null, IconUtilities.getIcons("export16.png"));
            this.table = table;
        }

        public void actionPerformed(ActionEvent e) {
            if (table == null) {
                Dialogs.showMessageDialog("Can not find table");
                return;
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Options options = new Options(ValidationReportViewer.class);
                    Options.FileSelectionOption selectionOption = options.addFileSelectionOption("targetFolder", "File to export", "", new String[0], "Browse...");

                    options.setEnabled(true);
                    Dialogs.DialogOptions dialogOptions = new Dialogs.DialogOptions(Dialogs.OK_CANCEL, "Export report table to csv file");
                    if (Dialogs.CANCEL.equals(Dialogs.showMoreOptionsDialog(dialogOptions, options.getPanel(), options.getAdvancedPanel()))) {
                        return;
                    }

                    File outFile = new File(selectionOption.getValue());
                    BufferedWriter writer = null;
                    try {
                        writer = new BufferedWriter(new FileWriter(outFile));
                        int columnCount = table.getColumnModel().getColumnCount();

                        List<String> values = getHeader();
                        writeRow(writer, values);
                        int rowCount = table.getRowCount();
                        for (int row=0; row < rowCount; row++) {
                            values.clear();
                            for (int column=0; column < columnCount; column++) {
                                Object value = table.getValueAt(row, column);
                                if (value == null) {
                                    values.add("");
                                } else {
                                    values.add(stripHtmlTags(value.toString(), true));
                                }
                            }
                            writeRow(writer, values);
                        }
                    } catch (IOException e1) {
                        Dialogs.showMessageDialog("Failed to export table, since " + e1.getMessage());
                    } finally {
                        GeneralUtilities.attemptClose(writer);
                    }
                }
            });
        }

        protected List<String> getHeader(){
            int columnCount = table.getColumnModel().getColumnCount();
            GroupableTableHeader header = (GroupableTableHeader) table.getTableHeader();

            List<String> values = new LinkedList<String>();
            for (int column = 0; column < columnCount; column++) {
                StringBuilder sb = new StringBuilder();
                Enumeration columnGroups = header.getColumnGroups(table.getColumnModel().getColumn(column));
                while (columnGroups != null && columnGroups.hasMoreElements()) {
                    sb.append(((ColumnGroup) columnGroups.nextElement()).getText()).append(" - ");
                }

                sb.append(table.getColumnModel().getColumn(column).getHeaderValue().toString());
                values.add(sb.toString());
            }

            return values;
        }

        private void writeRow(BufferedWriter writer, List<String> values) throws IOException {
            boolean first = true;
            for (String value : values) {
                if (first) {
                    first = false;
                } else {
                    writer.write(",");
                }
                writer.write(escapeValueForCsv(value));
            }
            writer.newLine();
        }

        private String escapeValueForCsv(String value) {
            value = value.replaceAll("\"", "\"\"");
            if (value.contains("\"") || value.contains(",") || value.contains("\n")) {
                value = "\"" + value + "\"";
            }
            return value;
        }

        private String stripHtmlTags(String string, boolean onlyIfStartsWithHtmlTag) {
            if ((string == null) || "".equals(string) || (onlyIfStartsWithHtmlTag && !string.regionMatches(true, 0, "<html>", 0, 6))) {
                return string;
            }
            string = Pattern.compile("</?[a-zA-Z0-9][^>]*>").matcher(string).replaceAll("");
            string = string.replace("&gt;",">");
            string = string.replace("&lt;","<");
            return string;
        }
    }
}
