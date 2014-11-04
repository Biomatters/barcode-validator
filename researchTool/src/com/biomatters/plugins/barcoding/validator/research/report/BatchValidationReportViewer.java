package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.components.*;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.output.ValidationOutputRecord;
import com.biomatters.plugins.barcoding.validator.output.ValidationReportDocument;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorOperation;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorOptions;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * @author Matthew Cheung
 *         Created on 4/11/14 11:36 AM
 */
public class BatchValidationReportViewer extends DocumentViewer {
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
    public JComponent getComponent() {
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
            BarcodeValidatorOptions optionsUsed = report.getOptionsUsed();
            Map<OptionIdentifier, String> valuesFromOptions = getValuesFromOptions(optionsUsed);
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
            rows.add(new Row(report.getName(), entry.getKey(), count, report.getRecords().size() - count, valuesFromOptions));
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

        GPanel rootPanel = new GPanel(new BorderLayout());

        JTextPane textPane = new GTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setText(getHeaderText(reports.size(), barcodesNotTheSame, numPassedEverything));
        rootPanel.add(textPane, BorderLayout.NORTH);

        boolean missing = false;
        Set<OptionIdentifier> optionIds = new HashSet<OptionIdentifier>();
        for (Row row : rows) {
            if(row.valuesFromOptions == null) {
                missing = true;
            } else {
                optionIds.addAll(row.valuesFromOptions.keySet());
            }
        }
        if(!missing) {
            List<OptionIdentifier> idsOfDifferent = new ArrayList<OptionIdentifier>();
            for (OptionIdentifier optionId : optionIds) {
                Set<String> values = new HashSet<String>();
                for (Row row : rows) {
                    values.add(row.valuesFromOptions.get(optionId));
                }
                if(values.size() > 1) {
                    idsOfDifferent.add(optionId);
                }
            }

            final RowTableModel model = new RowTableModel(idsOfDifferent, rows);
            GTable table = new GTable(model);
            table.setRowSorter(new TableRowSorter<TableModel>(model));

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Object source = e.getSource();
                    if(source instanceof JTable) {
                        JTable table = (JTable) source;
                        model.processAction(table.rowAtPoint(e.getPoint()), table.columnAtPoint(e.getPoint()));
                    }
                }
            });

            rootPanel.add(new JScrollPane(table));
        } else {
            rootPanel.add(new GLabel("No table shown because one of the selected reports was created prior to v0.2"));
        }

        return rootPanel;
    }

    private static Map<OptionIdentifier, String> getValuesFromOptions(Options optionsUsed) {
        if(optionsUsed == null) {
            return null;
        }
        return getValuesFromOptions("", optionsUsed);
    }

    private static Map<OptionIdentifier, String> getValuesFromOptions(String prefix, Options options) {
        Map<OptionIdentifier, String> result = new HashMap<OptionIdentifier, String>();
        for (Options.Option option : options.getOptions()) {
            if(!(option instanceof Options.LabelOption)) {
                result.put(new OptionIdentifier(prefix + option.getName(), option.getLabel()), option.getValueAsString());
            }
        }

        for (Map.Entry<String, Options> entry : options.getChildOptions().entrySet()) {
            result.putAll(getValuesFromOptions(prefix + entry.getKey() + ".", entry.getValue()));
        }

        return result;
    }

    private static class OptionIdentifier {
        private String fullName;
        private String label;

        private OptionIdentifier(String fullName, String label) {
            this.fullName = fullName;
            this.label = label.trim();
            if(label.endsWith(":")) {
                this.label = label.substring(0, label.length()-1);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OptionIdentifier that = (OptionIdentifier) o;

            if (!fullName.equals(that.fullName)) return false;
            if (!label.equals(that.label)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = fullName.hashCode();
            result = 31 * result + label.hashCode();
            return result;
        }
    }

    private static String getHeaderText(int runs, boolean barcodesNotTheSame, int numPassedEverything) {
        return "<h1>Summary of " + runs + " Validation Runs</h1>" +
                (barcodesNotTheSame ? "<font color=\"red\">Warning: Input barcode sequences for selected reports do not match</font><br><br>" : "") +
                numPassedEverything + " out of " + runs + " passed all validations on all data sets.<br><br>";
    }

    private static class Row {
        String name;
        URN urn;
        int numAllPassed;
        int numFailedAtLeastOne;
        Map<OptionIdentifier, String> valuesFromOptions;

        private Row(String name, URN urn, int numAllPassed, int numFailedAtLeastOne, Map<OptionIdentifier, String> valuesFromOptions) {
            this.name = name;
            this.urn = urn;
            this.numAllPassed = numAllPassed;
            this.numFailedAtLeastOne = numFailedAtLeastOne;
            this.valuesFromOptions = valuesFromOptions;
        }
    }

    private static class RowTableModel extends AbstractTableModel {

        private static final SimpleResultColumn NAME_COLUMN = new SimpleResultColumn("Name") {
            @Override
            Object getValueForRow(Row row) {
                String rowName = row.name;
                if (rowName.endsWith(BarcodeValidatorOperation.REPORT_NAME_SUFFIX)) {
                    rowName = rowName.substring(0, rowName.length() - BarcodeValidatorOperation.REPORT_NAME_SUFFIX.length());
                }
                return rowName;
            }
        };


        private static final ResultColumn SELECT_REPORT_COLUMN = new ResultColumn() {
            @Override
            String getColumnHeader() {
                return "";
            }

            @Override
            Object getValueForRow(Row row) {
                return "<html><a href=\"" + row.urn + "\">Select Report</a></html>";
            }

            @Override
            void processClick(Row row) {
                DocumentUtilities.selectDocument(row.urn);
            }
        };
        private static final SimpleResultColumn NUM_FAIL_COLUMN = new SimpleResultColumn("# Failed At Least One") {
            @Override
            Object getValueForRow(Row row) {
                return row.numFailedAtLeastOne;
            }
        };
        private static final SimpleResultColumn NUM_PASS_COLUMN = new SimpleResultColumn("# Passed All") {
            @Override
            Object getValueForRow(Row row) {
                return row.numAllPassed;
            }
        };

        private List<ResultColumn> columns;
        private List<Row> rows;

        private RowTableModel(List<OptionIdentifier> optionsToShow, List<Row> rows) {
            this.rows = rows;
            columns = new ArrayList<ResultColumn>();
            columns.add(NAME_COLUMN);
            columns.add(SELECT_REPORT_COLUMN);
            for (final OptionIdentifier optionIdentifier : optionsToShow) {
                columns.add(new SimpleResultColumn(optionIdentifier.label) {
                    @Override
                    Object getValueForRow(Row row) {
                        return row.valuesFromOptions.get(optionIdentifier);
                    }
                });
            }
            columns.add(NUM_PASS_COLUMN);
            columns.add(NUM_FAIL_COLUMN);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public String getColumnName(int column) {
            if(column < 0 || column >= columns.size()) {
                return "";
            }
            return columns.get(column).getColumnHeader();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(columnIndex < 0 || columnIndex >= columns.size() || rowIndex < 0 || rowIndex >= rows.size()) {
                return "";
            }

            Row row = rows.get(rowIndex);
            ResultColumn column = columns.get(columnIndex);
            return column.getValueForRow(row);
        }

        public void processAction(int rowIndex, int columnIndex) {
            if(columnIndex < 0 || columnIndex >= columns.size() || rowIndex < 0 || rowIndex >= rows.size()) {
                return;
            }

            Row row = rows.get(rowIndex);
            ResultColumn column = columns.get(columnIndex);
            column.processClick(row);
        }
    }

    private static abstract class ResultColumn {
        abstract String getColumnHeader();
        abstract Object getValueForRow(Row row);
        abstract void processClick(Row row);
    }

    private abstract static class SimpleResultColumn extends ResultColumn {

        private String columnHeader;

        private SimpleResultColumn(String columnHeader) {
            this.columnHeader = columnHeader;
        }

        @Override
        String getColumnHeader() {
            return columnHeader;
        }

        @Override
        abstract Object getValueForRow(Row row);

        @Override
        void processClick(Row row) {}
    }
}
