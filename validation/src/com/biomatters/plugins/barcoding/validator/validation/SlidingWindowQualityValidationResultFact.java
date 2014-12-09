package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.plugins.barcoding.validator.validation.results.*;
import org.jdom.Element;

import java.util.Arrays;
import java.util.List;

/**
 * SlidingWindowQualityValidationResultFact is used to hold validation result for {@link com.biomatters.plugins.barcoding.validator.validation.SlidingWindowQualityValidation}
 * @author Frank Lee
 *         Created on 22/11/14 9:58 AM
 */
public class SlidingWindowQualityValidationResultFact extends ResultFact {
    public static final String PASS_COLUMN_NAME                     = "Pass";
    public static final String TOTAL_NUMBER_OF_WINDOWS_COLUMN_NAME  = "Total number of windows";
    public static final String NUMBER_OF_FAILED_WINDOWS_COLUMN_NAME = "Number of failed windows";
    public static final String RATIO_OF_PASSED_WINDOWS_COLUMN_NAME  = "% of passed windows";
    public static final String NOTES_COLUMN_NAME                    = "Notes";

    private BooleanResultColumn passColumn;
    private IntegerResultColumn totalNumberOfWindowsColumn;
    private IntegerResultColumn numberOfFailedWindowsColumn;
    private DoubleResultColumn ratioOfPassedWindowsColumn;
    private StringResultColumn notesColumn;

    /**
     * used by XMLSerializable
     */
    @SuppressWarnings("unused")
    public SlidingWindowQualityValidationResultFact(Element element) throws XMLSerializationException {
        super(element);
    }

    public SlidingWindowQualityValidationResultFact(boolean pass, int totalNumberOfWindows, int numberOfFailedWindows, String notes) {
        super("Sliding Window Quality Validation");
        initColumns(pass, totalNumberOfWindows, numberOfFailedWindows, notes);
    }

    @Override
    public List<ResultColumn> getColumns() {
        return Arrays.<ResultColumn>asList(
                passColumn,
                totalNumberOfWindowsColumn,
                numberOfFailedWindowsColumn,
                ratioOfPassedWindowsColumn,
                notesColumn
        );
    }

    @Override
    public void addColumn(ResultColumn column) {
        String columnName = column.getName();

        if (columnName.equals(RATIO_OF_PASSED_WINDOWS_COLUMN_NAME)) {
            ratioOfPassedWindowsColumn = (DoubleResultColumn) column;
        } else if (columnName.equals(NUMBER_OF_FAILED_WINDOWS_COLUMN_NAME)) {
            numberOfFailedWindowsColumn = (IntegerResultColumn) column;
        } else if (columnName.equals(PASS_COLUMN_NAME)) {
            passColumn = (BooleanResultColumn) column;
        } else if (columnName.equals(NOTES_COLUMN_NAME)) {
            notesColumn = (StringResultColumn) column;
        } else if (columnName.equals(TOTAL_NUMBER_OF_WINDOWS_COLUMN_NAME)) {
            totalNumberOfWindowsColumn = (IntegerResultColumn) column;
        } else {
            System.out.println("Can not recognize column " + columnName);
        }
    }

    public void setPass(boolean pass) {
        passColumn.setData(pass);
    }

    @Override
    public boolean getPass() {
        return passColumn.getData();
    }

    public void setTotalNumberOfWindows(int totalNumberOfWindows) {
        totalNumberOfWindowsColumn.setData(totalNumberOfWindows);

        ratioOfPassedWindowsColumn.setData((double)(totalNumberOfWindows - numberOfFailedWindowsColumn.getData()) * 100/totalNumberOfWindows);
    }

    public void setNumberOfFailedWindows(int numberOfFailedWindows) {
        numberOfFailedWindowsColumn.setData(numberOfFailedWindows);

        int totalNumberOfWindows = totalNumberOfWindowsColumn.getData();

        ratioOfPassedWindowsColumn.setData((double)(totalNumberOfWindows - numberOfFailedWindows) * 100/totalNumberOfWindows);
    }

    public void setNotes(String notes) {
        notesColumn.setData(notes);
    }

    private void initColumns(boolean pass, int totalNumberOfWindows, int numberOfFailedWindows, String notes) {
        passColumn = new BooleanResultColumn(PASS_COLUMN_NAME);
        totalNumberOfWindowsColumn = new IntegerResultColumn(TOTAL_NUMBER_OF_WINDOWS_COLUMN_NAME);
        numberOfFailedWindowsColumn = new IntegerResultColumn(NUMBER_OF_FAILED_WINDOWS_COLUMN_NAME);
        ratioOfPassedWindowsColumn = new DoubleResultColumn(RATIO_OF_PASSED_WINDOWS_COLUMN_NAME);
        notesColumn = new StringResultColumn(NOTES_COLUMN_NAME);

        numberOfFailedWindowsColumn.setData(0); // To prevent NullPointerException from being thrown in the below

        setPass(pass);
        setTotalNumberOfWindows(totalNumberOfWindows);
        setNumberOfFailedWindows(numberOfFailedWindows);
        setNotes(notes);
    }
}
