package com.biomatters.plugins.barcoding.validator.research.report.table;

import com.sun.istack.internal.NotNull;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;

public class ColumnGroup {
    protected TableCellRenderer renderer = null;
    protected Vector<Object> vector = null;
    protected String text = null;
    private int margin = 0;

    /**
     * @param text The name of this group which will show in the merged cell
     * @param renderer the render used to render cell
     */
    public ColumnGroup(String text, @NotNull TableCellRenderer renderer) {
        this.renderer = renderer;
        this.text = text;
        vector = new Vector<Object>();
    }

    /**
     * @param obj such element of this group, it should be {@link com.biomatters.plugins.barcoding.validator.research.report.table.ColumnGroup} or {@link javax.swing.table.TableColumn}
     */
    public void add(Object obj) {
        if (obj == null)
            return;
        vector.addElement(obj);
    }


    /**
     * get the path to specify column
     * @param column the column to be found
     * @param group the {@link java.util.Vector} used to record path
     * @return the {@link java.util.Vector} containing the path, or null if it can not in this group
     */
    public Vector<ColumnGroup> getColumnGroups(TableColumn column, Vector<ColumnGroup> group) {
        group.addElement(this);
        if (vector.contains(column))
            return group;
        Enumeration<Object> enumeration = vector.elements();
        while (enumeration.hasMoreElements()) {
            Object obj = enumeration.nextElement();
            if (obj instanceof ColumnGroup) {
                @SuppressWarnings("unchecked")
                Vector<ColumnGroup> groups = ((ColumnGroup) obj).getColumnGroups(column,
                        (Vector<ColumnGroup>) group.clone());
                if (groups != null) {
                    return groups;
                }
            }
        }
        return null;
    }

    @NotNull
    public TableCellRenderer getHeaderRenderer() {
        return renderer;
    }

    public Object getHeaderValue() {
        return text;
    }

    public int getSize() {
        return vector == null ? 0 : vector.size();
    }

    public Dimension getSize(JTable table) {
        Component comp = renderer.getTableCellRendererComponent(table,
                getHeaderValue(), false, false, -1, -1);
        int height = comp.getPreferredSize().height;
        int width = 0;

        Enumeration<Object> enumeration = vector.elements();
        while (enumeration.hasMoreElements()) {
            Object obj = enumeration.nextElement();
            if (obj instanceof TableColumn) {
                TableColumn aColumn = (TableColumn) obj;
                width += aColumn.getWidth();
                width += margin;
            } else {
                width += ((ColumnGroup) obj).getSize(table).width;
            }
        }
        return new Dimension(width, height);
    }

    public String getText() {
        return text;
    }

    public void setColumnMargin(int margin) {
        this.margin = margin;
        Enumeration<Object> enumeration = vector.elements();
        while (enumeration.hasMoreElements()) {
            Object obj = enumeration.nextElement();
            if (obj instanceof ColumnGroup) {
                ((ColumnGroup) obj).setColumnMargin(margin);
            }
        }
    }

    public void setText(String newText) {
        text = newText;
    }
}
