package com.biomatters.plugins.barcoding.validator.validation.pci;

import com.biomatters.plugins.barcoding.validator.validation.results.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthew Cheung
 *         Created on 3/12/14 2:51 PM
 */
public class PCICalculatorResultFact extends ResultFact {

    private static final String PASS_COLUMN_NAME = "Pass";

    @SuppressWarnings("UnusedDeclaration")
    public PCICalculatorResultFact() {
        // For de-serialization
    }

    public PCICalculatorResultFact(@Nonnull boolean pass, @Nonnull double pDistance, @Nonnull String errorMessage) {
        // ResultColumn crashes serializing with null values :(
        BooleanResultColumn passColumn = new BooleanResultColumn(PASS_COLUMN_NAME);
        passColumn.setData(pass);
        addedColumns.add(passColumn);

        DoubleResultColumn pDistanceColumn = new DoubleResultColumn("P-Distance");
        pDistanceColumn.setData(pDistance);
        addedColumns.add(pDistanceColumn);

        StringResultColumn errorColumn = new StringResultColumn("Errors");
        errorColumn.setData(errorMessage);
        addedColumns.add(errorColumn);
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
        for (ResultColumn resultColumn : getColumns()) {
            if(resultColumn instanceof BooleanResultColumn && resultColumn.getName().equals(PASS_COLUMN_NAME)) {
                return ((BooleanResultColumn)resultColumn).getData();
            }
        }
        return false;
    }
}
