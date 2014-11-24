package com.biomatters.plugins.barcoding.validator.research.report.table;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.util.Enumeration;
import java.util.Vector;

public class GroupableTableHeader extends JTableHeader {
    protected Vector columnGroups = null;

    public GroupableTableHeader(JTableHeader originalHeader) {
        super(originalHeader.getColumnModel());
        setUI(new GroupableTableHeaderUI());
        setReorderingAllowed(false);
        setRequestFocusEnabled(false);
        setDefaultRenderer(originalHeader.getDefaultRenderer());
    }

    @SuppressWarnings("unchecked")
    public void addColumnGroup(ColumnGroup g) {
        if (columnGroups == null) {
            columnGroups = new Vector<ColumnGroup>();
        }
        columnGroups.addElement(g);
    }

    /**
     * get the path to specify column in this group
     *
     * @param col the column to be targeted
     * @return {@link java.util.Enumeration}
     */
    public Enumeration getColumnGroups(TableColumn col) {
        if (columnGroups == null) {
            return null;
        }
        Enumeration enum1 = columnGroups.elements();
        while (enum1.hasMoreElements()) {
            ColumnGroup cGroup = (ColumnGroup) enum1.nextElement();
            Vector v_ret = (Vector) cGroup.getColumnGroups(col, new Vector<ColumnGroup>());
            if (v_ret != null) {
                return v_ret.elements();
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public boolean isFocusTraversable() {
        return super.isFocusTraversable() && isRequestFocusEnabled();
    }

    public void setColumnMargin() {
        if (columnGroups == null) {
            return;
        }
        int columnMargin = getColumnModel().getColumnMargin();
        Enumeration enum1 = columnGroups.elements();
        while (enum1.hasMoreElements()) {
            ColumnGroup cGroup = (ColumnGroup) enum1.nextElement();
            cGroup.setColumnMargin(columnMargin);
        }
    }

    public void setReorderingAllowed(boolean b) {
        reorderingAllowed = b;
    }
}