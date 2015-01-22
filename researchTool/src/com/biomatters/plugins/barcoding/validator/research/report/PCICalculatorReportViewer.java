package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.components.GPanel;
import com.biomatters.geneious.publicapi.components.GTable;
import com.biomatters.geneious.publicapi.components.GTextPane;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.plugin.ActionProvider;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.plugin.GeneiousAction;
import com.biomatters.plugins.barcoding.validator.output.PCICalculatorReportDocument;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Frank Lee
 *         Created on 20/01/15 5:14 PM
 */
public class PCICalculatorReportViewer extends DocumentViewer {
    private PCICalculatorReportDocument document;

    private JTable table = null;

    public PCICalculatorReportViewer(PCICalculatorReportDocument document) {
        this.document = document;
    }

    @Override
    public JComponent getComponent() {
        if (document == null || document.getResult() == null) {
            return null;
        }

        List<Row> rows = new ArrayList<Row>();

        for (Map.Entry<URN, Double> entry : document.getResult().entrySet()) {
            URN urn = entry.getKey();
            Double value = entry.getValue();
            rows.add(new Row(DocumentUtilities.getDocumentByURN(urn).getName(), urn, value));
        }

        if (rows.isEmpty()) {
            return null;
        }

        GPanel rootPanel = new GPanel(new BorderLayout());

        JTextPane textPane = new GTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setText("<h1>PCI score for alignment " + document.getName() + "</h1>");
        rootPanel.add(textPane, BorderLayout.NORTH);

        final RowTableModel model = new RowTableModel(rows);
        table = new GTable(model);
        table.setRowSorter(new TableRowSorter<TableModel>(model));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Object source = e.getSource();
                if (source instanceof JTable) {
                    JTable table = (JTable) source;
                    model.processAction(table.rowAtPoint(e.getPoint()), table.columnAtPoint(e.getPoint()));
                }
            }
        });

        rootPanel.add(new JScrollPane(table));
        return rootPanel;
    }

    private static class Row {
        String name;
        URN urn;
        Double score;

        private Row(String name, URN urn, Double score) {
            this.name = name;
            this.urn = urn;
            this.score = score;
        }
    }

    private static class RowTableModel extends AbstractTableModel {

        private static final SimpleResultColumn NAME_COLUMN = new SimpleResultColumn("Name") {
            @Override
            String getColumnHeader() {
                return "";
            }

            @Override
            Object getValueForRow(Row row) {
                return "<html><a href=\"" + row.urn + "\">" + row.name + "</a></html>";
            }

            @Override
            void processClick(Row row) {
                DocumentUtilities.selectDocument(row.urn);
            }
        };

        private static final SimpleResultColumn SCORE_COLUMN = new SimpleResultColumn("PCI Score") {
            @Override
            Object getValueForRow(Row row) {
                return row.score;
            }
        };

        private List<ResultColumn> columns;
        private List<Row> rows;

        private RowTableModel(List<Row> rows) {
            this.rows = rows;
            columns = new ArrayList<ResultColumn>();
            columns.add(NAME_COLUMN);
            columns.add(SCORE_COLUMN);
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
            if (column < 0 || column >= columns.size()) {
                return "";
            }
            return columns.get(column).getColumnHeader();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex < 0 || columnIndex >= columns.size() || rowIndex < 0 || rowIndex >= rows.size()) {
                return "";
            }

            Row row = rows.get(rowIndex);
            ResultColumn column = columns.get(columnIndex);
            return column.getValueForRow(row);
        }

        public void processAction(int rowIndex, int columnIndex) {
            if (columnIndex < 0 || columnIndex >= columns.size() || rowIndex < 0 || rowIndex >= rows.size()) {
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
        void processClick(Row row) {
        }
    }

    @Override
    public ActionProvider getActionProvider() {
        return new ActionProvider() {
            @Override
            public List<GeneiousAction> getOtherActions() {
                List<GeneiousAction> ret = new ArrayList<GeneiousAction>();
                ret.add(new ValidationReportViewer.ExportReportAction("Export report table to csv file", table) {
                    @Override
                    protected List<String> getHeader() {
                        int columnCount = table.getColumnModel().getColumnCount();

                        List<String> values = new LinkedList<String>();
                        for (int column = 0; column < columnCount; column++) {
                            values.add(table.getColumnModel().getColumn(column).getHeaderValue().toString());
                        }

                        return values;
                    }
                });
                return ret;
            }
        };

    }
}
