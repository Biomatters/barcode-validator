package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Frank Lee
 *         Created on 22/11/14 9:58 AM
 */
public class SlidingWindowQualityValidationResultFact extends ResultFact {
    public static final String SEQUENCE_COLUMN_NAME                 = "Sequence";
    public static final String PASS_COLUMN_NAME                     = "Pass";
    public static final String TOTAL_NUMBER_OF_WINDOWS_COLUMN_NAME  = "Total number of windows";
    public static final String NUMBER_OF_FAILED_WINDOWS_COLUMN_NAME = "Number of failed windows";
    public static final String RATIO_OF_PASSED_WINDOWS_COLUMN_NAME  = "% of passed windows";
    public static final String NOTES_COLUMN_NAME                    = "Notes";

    private ResultColumn<LinkResultColumn.LinkBox> sequenceColumn;
    private BooleanResultColumn passColumn;
    private IntegerResultColumn totalNumberOfWindowsColumn;
    private IntegerResultColumn numberOfFailedWindowsColumn;
    private DoubleResultColumn ratioOfPassedWindowsColumn;
    private StringResultColumn notesColumn;

    public SlidingWindowQualityValidationResultFact(Element element) throws XMLSerializationException {
        super(element);
    }

    public SlidingWindowQualityValidationResultFact(String factName) {
        this(factName, "-", Collections.<URN>emptyList(), false, 0, 0, "");
    }

    public SlidingWindowQualityValidationResultFact(String factName, String sequenceName, List<URN> sequenceLinks, boolean pass, int totalNumberOfWindows, int numberOfFailedWindows, String notes) {
        super("Sliding Window Quality Validation");

        initColumns(sequenceName, sequenceLinks, pass, totalNumberOfWindows, numberOfFailedWindows, notes);
    }

    @Override
    public List<ResultColumn> getColumns() {
        return Arrays.<ResultColumn>asList(
//                sequenceColumn,
                passColumn,
                totalNumberOfWindowsColumn,
                numberOfFailedWindowsColumn,
                ratioOfPassedWindowsColumn,
                notesColumn
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addColumn(ResultColumn column) {
        String columnName = column.getName();

        if (columnName.equals(SEQUENCE_COLUMN_NAME)) {
            sequenceColumn = column;
        } else if (columnName.equals(RATIO_OF_PASSED_WINDOWS_COLUMN_NAME)) {
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

    @Override
    public void setPass(boolean pass) {
        passColumn.setData(pass);
    }
    @Override
    public boolean getPass() { return passColumn.getData(); }

    public void setSequenceName(String sequenceName) {
        sequenceColumn.getData().setLabel(sequenceName);
    }
    public String getSequenceName() {
        return sequenceColumn.getData().getLabel();
    }

    public void setSequenceLinks(List<URN> sequenceLinks) {
        sequenceColumn.getData().setLinks(sequenceLinks);
    }
    public List<URN> getSequenceLinks() { return sequenceColumn.getData().getLinks(); }

    public void setTotalNumberOfWindows(int totalNumberOfWindows) {
        totalNumberOfWindowsColumn.setData(totalNumberOfWindows);

        ratioOfPassedWindowsColumn.setData((double)(totalNumberOfWindows - numberOfFailedWindowsColumn.getData()) * 100 / totalNumberOfWindows);
    }
    public int getTotalNumberOfWindows() {
        return totalNumberOfWindowsColumn.getData();
    }

    public void setNumberOfFailedWindows(int numberOfFailedWindows) {
        numberOfFailedWindowsColumn.setData(numberOfFailedWindows);

        int totalNumberOfWindows = totalNumberOfWindowsColumn.getData();

        ratioOfPassedWindowsColumn.setData((double)(totalNumberOfWindows - numberOfFailedWindows) * 100 /totalNumberOfWindows);
    }
    public int getNumberOfFailedWindows() { return numberOfFailedWindowsColumn.getData(); }

    public double getRatioOfPassedWindows() {
        return ratioOfPassedWindowsColumn.getData();
    }

    public void setNotes(String notes) {
        notesColumn.setData(notes);
    }
    public String getNotes() { return notesColumn.getData(); }

    private void initColumns(String sequenceName,  List<URN> links, boolean pass, int totalNumberOfWindows, int numberOfFailedWindows, String notes) {
        sequenceColumn = new LinkResultColumn(SEQUENCE_COLUMN_NAME);
        passColumn = new BooleanResultColumn(PASS_COLUMN_NAME);
        totalNumberOfWindowsColumn = new IntegerResultColumn(TOTAL_NUMBER_OF_WINDOWS_COLUMN_NAME);
        numberOfFailedWindowsColumn = new IntegerResultColumn(NUMBER_OF_FAILED_WINDOWS_COLUMN_NAME);
        ratioOfPassedWindowsColumn = new DoubleResultColumn(RATIO_OF_PASSED_WINDOWS_COLUMN_NAME);
        notesColumn = new StringResultColumn(NOTES_COLUMN_NAME);

        numberOfFailedWindowsColumn.setData(0); // To prevent NullPointerException from being thrown in the below
        // call of setTotalNumberOfWindows();

        setSequenceName(sequenceName);
        setSequenceLinks(links);
        setPass(pass);
        setTotalNumberOfWindows(totalNumberOfWindows);
        setNumberOfFailedWindows(numberOfFailedWindows);
        setNotes(notes);
    }
}
