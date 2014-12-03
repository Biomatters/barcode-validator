package com.biomatters.plugins.barcoding.validator.validation.pci;

import com.biomatters.plugins.barcoding.validator.validation.results.ResultColumn;
import com.biomatters.plugins.barcoding.validator.validation.results.ResultFact;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthew Cheung
 *         Created on 3/12/14 2:51 PM
 */
public class PciResultFact extends ResultFact {

    public PciResultFact() {
    }

    @Override
    public String getFactName() {
        return "PCI Validation";
    }

    List<ResultColumn> addedColumns = new ArrayList<ResultColumn>();
    @Override
    public List<ResultColumn> getColumns() {
        return addedColumns;
    }

    @Override
    public void addColumn(ResultColumn column) {
        addedColumns.add(column);
    }

    @Override
    public boolean getPass() {
        return true;
    }
}
