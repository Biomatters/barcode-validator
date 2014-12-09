package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.components.*;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.plugin.ActionProvider;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
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
 * The view is sortable and includes links to input and output
 * {@link com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument} in Geneious.
 *
 * @author Matthew Cheung
 *         Created on 2/10/14 10:04 AM
 */
public class ValidationReportViewer extends DocumentViewer {
    ValidationReportDocument reportDocument;
    JTable table = null;
    public ValidationReportViewer(ValidationReportDocument reportDocument) {
        this.reportDocument = reportDocument;
    }

    public String getHtml() {
        return generateHtml(reportDocument);
    }

    public static final String OPTION_PREFIX = "option:";

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
            if(record.isAllPassedForConsensus()) {
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

    private static String getLinkForSelectingDocuments(String label, List<URN> documentUrns) {
        List<String> urnStrings = new ArrayList<String>();
        for (URN inputUrn : documentUrns) {
            urnStrings.add(inputUrn.toString());
        }
        return "<a href=\"" + StringUtilities.join(",", urnStrings) + "\">" + label + "</a>";
    }

    private JTextPane getTextPane() {
        String html = getHtml();
        if(html == null || html.isEmpty()) {
            return null;
        }
        final JTextPane textPane = new GTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);

        HyperlinkListener hyperlinkListener = getHyperlinkListener();
        if(hyperlinkListener != null) {
            textPane.addHyperlinkListener(hyperlinkListener);
        }
        textPane.setText(html);
        return textPane;
    }

    @Override
    public JComponent getComponent() {
        JComponent textPane = getTextPane();

        final GPanel rootPanel = new GPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension defaultPrefSize = super.getPreferredSize();
                // Add 500 px to the preferred height so users can scroll past the table
                return new Dimension(defaultPrefSize.width, defaultPrefSize.height + 500);
            }
        };

        rootPanel.add(textPane, BorderLayout.NORTH);
        if (table == null) {
        	table = getTable();
        }

        if (table != null) {
            final JScrollPane tableScrollPane = new JScrollPane(table);

            // Set the scroll pane's preferred size to the same as the table so scroll bars are never needed
            tableScrollPane.getViewport().setPreferredSize(table.getPreferredSize());
            table.addComponentListener(new ComponentListener() {
                @Override
                public void componentResized(ComponentEvent e) {
                    tableScrollPane.getViewport().setPreferredSize(table.getPreferredSize());
                    rootPanel.validate();
                }

                @Override
                public void componentMoved(ComponentEvent e) {

                }

                @Override
                public void componentShown(ComponentEvent e) {

                }

                @Override
                public void componentHidden(ComponentEvent e) {

                }
            });

            rootPanel.add(tableScrollPane, BorderLayout.CENTER);
        }

        return rootPanel;
    }

    public JTable getTable() {
        List<ValidationOutputRecord> records = reportDocument.getRecords();

        final ValidationReportTableModel tableModel = new ValidationReportTableModel(records, reportDocument.getPciValues());
        final JTable table = new GTable(tableModel, new DefaultTableColumnModel(){
            @Override
            public void moveColumn(int columnIndex, int newIndex) {
                if (tableModel.getOptionAt(columnIndex) == tableModel.getOptionAt(newIndex)) {
                    super.moveColumn(columnIndex, newIndex);
                }
            }
        });
        table.setAutoCreateColumnsFromModel(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setDefaultRenderer(ResultColumn.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return super.getTableCellRendererComponent(table, ((ResultColumn) value).getDisplayValue(), isSelected, hasFocus, row, column);
            }
        });


        TableRowSorter<ValidationReportTableModel> sorter = new TableRowSorter<ValidationReportTableModel>(tableModel);
        for (int i = 0; i < table.getColumnCount(); i++) {
            Class<?> columnClass = table.getColumnClass(i);
            if(CellValue.class.isAssignableFrom(columnClass)) {
                sorter.setComparator(i, getCellValueComparator(CellValue.class, sorter));
            }
        }
        table.setRowSorter(sorter);

        GroupableTableHeader head = new GroupableTableHeader(table.getTableHeader());
        table.setTableHeader(head);
        TableCellRenderer headerRenderer = head.getDefaultRenderer();
        if(headerRenderer instanceof DefaultTableCellRenderer) {
            ((DefaultTableCellRenderer) headerRenderer).setHorizontalAlignment(SwingConstants.CENTER);
        }

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                Object source = e.getSource();
                if (source instanceof JTable) {
                    JTable table = (JTable) source;
                    Object cell = table.getValueAt(table.getSelectedRow(), table.getSelectedColumn());
                    if (cell instanceof CellValue) {
                        ResultColumn value = ((CellValue) cell).value;
                        if(value instanceof LinkResultColumn) {
                            ((LinkResultColumn)value).getData().openLink();
                        }
                    }
                }
            }
        });

        mergeHeader(table, records);
        final GroupableTableHeaderUI groupableTableHeaderUI = new GroupableTableHeaderUI();
        table.getTableHeader().setUI(groupableTableHeaderUI);
        table.setAutoCreateRowSorter(false);

        MouseListener[] mouseListeners = head.getMouseListeners();
        MouseInputListener originalHeaderMouseListener = null;
        for (final MouseListener mouseListener : mouseListeners) {
            if(mouseListener instanceof BasicTableHeaderUI.MouseInputHandler) {
                originalHeaderMouseListener = (BasicTableHeaderUI.MouseInputHandler)mouseListener;
            }
        }
        if(originalHeaderMouseListener != null) {
            head.removeMouseListener(originalHeaderMouseListener);
            MouseAdapter wrapper = wrapMouseListenerForHeader(tableModel, table, groupableTableHeaderUI, originalHeaderMouseListener);
            head.addMouseListener(wrapper);
        }

        table.getTableHeader().setReorderingAllowed(true);
        return table;
    }

    private MouseInputAdapter wrapMouseListenerForHeader(final ValidationReportTableModel tableModel, final JTable table, final GroupableTableHeaderUI groupableTableHeaderUI, final MouseInputListener originalHeaderMouseListener) {
        return new MouseInputAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int column = table.columnAtPoint(e.getPoint());
                    if (column >= tableModel.getFixedColumnLength() && e.getY() <= groupableTableHeaderUI.getHeaderHeight() / 2) {
                        tableModel.displayOptions(column);
                    } else {
                        originalHeaderMouseListener.mouseClicked(e);
                    }
                }

            @Override
            public void mousePressed(MouseEvent e) {
                originalHeaderMouseListener.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                originalHeaderMouseListener.mouseReleased(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                originalHeaderMouseListener.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                originalHeaderMouseListener.mouseExited(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                originalHeaderMouseListener.mouseDragged(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                originalHeaderMouseListener.mouseMoved(e);
            }
        };
    }

    private static <T extends Comparable<T>> Comparator getCellValueComparator(
            // There is a warning that we don't use the class.  But we need this parameter at compile time to type the generic
            @SuppressWarnings("UnusedParameters") Class<T> columnClass, final RowSorter rowSorter) {
        return new Comparator<CellValue<T>>() {
            @Override
            public int compare(CellValue<T> o1, CellValue<T> o2) {
                int value = o1.compareTo(o2);

                if(o1.consensusValue == o2.consensusValue) {
                    // If the values are from the same set we must keep the order.  So we need to check direction.
                    //noinspection unchecked
                    List<RowSorter.SortKey> sortKeys = rowSorter.getSortKeys();
                    if (!sortKeys.isEmpty()) {
                        RowSorter.SortKey sortKey = sortKeys.get(0);
                        SortOrder sortOrder = sortKey.getSortOrder();
                        return (sortOrder == SortOrder.ASCENDING ? 1 : -1) * value;
                    }
                }

                return value;
            }
        };
    }

    private void mergeHeader(JTable table, List<ValidationOutputRecord> records) {
        assert records != null && records.size() > 0;
        ValidationOutputRecord record = records.get(0);
        TableColumnModel cm = table.getColumnModel();
        GroupableTableHeader head = (GroupableTableHeader) table.getTableHeader();
        TableCellRenderer headerRenderer = head.getDefaultRenderer();
        ValidationReportTableModel model = (ValidationReportTableModel) table.getModel();

        int colIndex = record.getFixedColumns(record.getTraceDocumentUrns().get(0), null).size();
        Map<Class, Map<URN, RecordOfValidationResult>> validationResultsMap = record.getValidationResultsMap();

        for (Map.Entry<Class, Map<URN, RecordOfValidationResult>> classMapEntry : validationResultsMap.entrySet()) {
            Map<URN, RecordOfValidationResult> entry = classMapEntry.getValue();
            assert entry.size() > 0;

            int failCount = 0;
            int successCount = 0;

            for (Map.Entry<URN, RecordOfValidationResult> resultEntry : model.getResultsAt(colIndex).entrySet()) {
                if (resultEntry.getValue().isPassed()) {
                    successCount++;
                } else {
                    failCount++;
                }
            }

            ResultFact fact = entry.values().iterator().next().getFact();
            String headText = fact.getFactName() + "<br>(" + successCount + " Passed/" + failCount + " Failed)";
            ColumnGroup factGroup = new ColumnGroup(headText, headerRenderer);
            for (int i = 0; i < fact.getColumns().size(); i++) {
                factGroup.add(cm.getColumn(colIndex++));
            }

            head.addColumnGroup(factGroup);

        }
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
                    Options.FileSelectionOption selectionOption = options.addFileSelectionOption("targetFolder", "Export Folder:", "", new String[0], "Browse...");
                    selectionOption.setSelectionType(JFileChooser.DIRECTORIES_ONLY);
                    Options.StringOption filenameOption = options.addStringOption("filename", "Filename:", "report.csv", "Name of CSV file");

                    options.setEnabled(true);
                    if (!Dialogs.showOptionsDialog(options, "Export report table to csv file", false)) {
                        return;
                    }

                    File outFile = new File(selectionOption.getValue(), filenameOption.getValue());
                    if(outFile.exists()) {
                        if (!Dialogs.showYesNoDialog(outFile.getName() + " already exists, do you want to replace it?",
                                "Replace File?", null, Dialogs.DialogIcon.QUESTION)) {
                            return;
                        }
                    }
                    BufferedWriter writer = null;
                    try {
                        writer = new BufferedWriter(new FileWriter(outFile));
                        int columnCount = table.getColumnModel().getColumnCount();

                        List<String> values = getHeader();
                        writeRow(writer, values);
                        int rowCount = table.getRowCount();
                        for (int row = 0; row < rowCount; row++) {
                            values.clear();
                            for (int column=0; column < columnCount; column++) {
                                Object value = table.getValueAt(row, column);
                                if(value instanceof CellValue) {
                                    value = ((CellValue)value).value.getDisplayValue();
                                }
                                if (value == null) {
                                    values.add("");
                                } else {
                                    values.add(stripHtmlTags(value.toString(), true));
                                }
                            }
                            writeRow(writer, values);
                        }

                        Dialogs.showMessageDialog("Table exported to " + outFile.getAbsolutePath() + " successfully.");
                    } catch (IOException e1) {
                        Dialogs.showMessageDialog("Failed to export table: " + e1.getMessage());
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

    /**
     * A value in the table model that is aware of it's index and the value associated with the consensus
     * @param <T> Type of the data contained in a cell.  Corresponds to the {@link com.biomatters.plugins.barcoding.validator.validation.results.ResultColumn} type.
     */
    private static class CellValue<T extends Comparable<T>> implements Comparable<CellValue<T>> {

        private ResultColumn<T> consensusValue;
        private Integer index;
        private ResultColumn<T> value;

        public CellValue(ResultColumn<T> consensusValue, ResultColumn<T> value, Integer index) {
            this.consensusValue = consensusValue;
            this.value = value;
            this.index = index;
        }

        @Override
        public String toString() {
            return value.getDisplayValue().toString();
        }

        @Override
        public int compareTo(CellValue<T> o) {
            int consensusCompareResult = consensusValue.getData().compareTo(o.consensusValue.getData());
            if(consensusCompareResult == 0) {
                return index.compareTo(o.index);
            } else {
                return consensusCompareResult;
            }
        }
    }

    public static class ValidationReportTableModel extends AbstractTableModel {
        private List<ValidationOutputRecord> records;
        private List<List<CellValue<?>>> data;

        public ValidationReportTableModel(List<ValidationOutputRecord> records, Map<URN, Double> pciScores) {
            this.records = new ArrayList<ValidationOutputRecord>();
            for (ValidationOutputRecord record : records) {
                this.records.add(record);
            }
            data = createTableData(records, pciScores);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return CellValue.class;
        }


        private static List<List<CellValue<?>>> createTableData(List<ValidationOutputRecord> records, Map<URN, Double> pciValues) {
            List<List<CellValue<?>>> data = new ArrayList<List<CellValue<?>>>();
            for (ValidationOutputRecord record : records) {
                List<ResultColumn> firstRow = null;
                for (List<ResultColumn> resultColumns : record.exportTable(pciValues)) {
                    if(firstRow == null) {
                        firstRow = resultColumns;
                    }
                    List<CellValue<?>> values = new ArrayList<CellValue<?>>();
                    for(int columnIndex=0; columnIndex<resultColumns.size(); columnIndex++) {
                        CellValue<?> cellValue = getCellValue(firstRow.get(columnIndex), resultColumns.get(columnIndex), columnIndex);
                        values.add(cellValue);
                    }
                    data.add(values);
                }
            }
            return data;
        }

        private static <T extends Comparable<T>> CellValue getCellValue(ResultColumn<T> firstRowValue, ResultColumn currentRowValue, int columnIndex) {
            // Have to cast because getClass() actually returns Class<? extends ResultColumn> due to type erasure :(
            //noinspection unchecked
            Class<ResultColumn<T>> firstRowClass = (Class<ResultColumn<T>>) firstRowValue.getClass();
            return new CellValue<T>(firstRowClass.cast(firstRowValue), firstRowClass.cast(currentRowValue), columnIndex);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            if (data.size() == 0) {
                return 0;
            }

            return data.get(0).size();
        }

        @Override
        public CellValue getValueAt(int rowIndex, int columnIndex) {
            return data.get(rowIndex).get(columnIndex);
        }

        @Override
        public String getColumnName(int column) {
            if (data.size() == 0) {
                return super.getColumnName(column);
            }

            return data.get(0).get(column).value.getName();
        }

        public void displayOptions(int columnIndex) {
            final ValidationOptions options = getOptionAt(columnIndex);
            if (options == null) {
                return;
            }

            final List<URN> failedResults = new ArrayList<URN>();
            final List<URN> passedResults = new ArrayList<URN>();
            for (Map.Entry<URN, RecordOfValidationResult> resultEntry : getResultsAt(columnIndex).entrySet()) {
                if (resultEntry.getValue().isPassed()) {
                    passedResults.add(resultEntry.getKey());
                } else {
                    failedResults.add(resultEntry.getKey());
                }
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    options.setEnabled(false);
                    Dialogs.DialogAction failButton = new Dialogs.DialogAction("View Failed") {
                        @Override
                        public boolean performed(GDialog dialog) {
                            DocumentUtilities.selectDocuments(failedResults);
                            return true;
                        }
                    };

                    Dialogs.DialogAction passButton = new Dialogs.DialogAction("View Passed") {
                        @Override
                        public boolean performed(GDialog dialog) {
                            DocumentUtilities.selectDocuments(passedResults);
                            return true;
                        }
                    };
                    Dialogs.DialogOptions dialogOptions = new Dialogs.DialogOptions(new Dialogs.DialogAction[]{
                            Dialogs.OK, passButton, failButton}, "Validation Options");
                    dialogOptions.setCancelButton(Dialogs.OK);
                    Dialogs.showMoreOptionsDialog(dialogOptions, options.getPanel(), options.getAdvancedPanel());
                }
            });
        }

        public ValidationOptions getOptionAt(int columnIndex) {
            assert records != null && records.size() > 0;
            Map<Integer, ValidationOptions> colunmOptionsMap = records.get(0).getColunmOptionsMap(false);
            assert colunmOptionsMap != null;

            return colunmOptionsMap.get(columnIndex);
        }

        public Map<URN, RecordOfValidationResult> getResultsAt(int columnIndex) {
            assert records != null && records.size() > 0;

            Map<URN, RecordOfValidationResult> ret = new HashMap<URN, RecordOfValidationResult>();
            Class aClass = records.get(0).getColunmClassMap(true).get(columnIndex);

            for (ValidationOutputRecord record : records) {
                ret.putAll(record.getValidationResultsMap().get(aClass));
            }

            return ret;
        }

        public int getFixedColumnLength() {
            assert records != null && records.size() > 0;
            ValidationOutputRecord record = records.get(0);
            return record.getFixedColumns(record.getOneURN(), null).size();
        }
    }
}
