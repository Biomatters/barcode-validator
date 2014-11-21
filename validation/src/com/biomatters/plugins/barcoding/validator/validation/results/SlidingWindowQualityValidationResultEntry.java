package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Frank Lee
 *         Created on 11/11/14 5:11 PM
 */
public class SlidingWindowQualityValidationResultEntry extends ValidationResultEntry {
    private List<SlidingWindowQualityValidationResultFact> facts = new ArrayList<SlidingWindowQualityValidationResultFact>();

    @SuppressWarnings("unused")
    protected SlidingWindowQualityValidationResultEntry(String name) {
        super(name);
    }

    @SuppressWarnings("unused")
    public SlidingWindowQualityValidationResultEntry(Element element) throws XMLSerializationException {
        super(element);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List getRow() {
        List ret = new ArrayList();
        for (SlidingWindowQualityValidationResultFact fact : facts) {
            for (ResultColumn column : fact.getColumns()) {
                ret.add(column.getDisplayValue());
            }
        }
        return ret;
    }

    @Override
    public List<String> getCol() {
        List<String> ret = new ArrayList<String>();
        for (SlidingWindowQualityValidationResultFact fact : facts) {
            for (ResultColumn column : fact.getColumns()) {
                ret.add(column.getName());
            }
        }
        return ret;
    }

    @Override
    public List<ResultColumn> getColumns() {
        List<ResultColumn> ret = new ArrayList<ResultColumn>();
        for (SlidingWindowQualityValidationResultFact fact : facts) {
            ret.addAll(fact.getColumns());
        }
        return ret;
    }

    public SlidingWindowQualityValidationResultEntry() {
        super("Quality Validation (Sliding Window)");
    }

    @Override
    public List<ResultFact> getResultFacts() {
        List<ResultFact> ret = new ArrayList<ResultFact>();
        ret.addAll(facts);
        return ret;
    }

    @Override
    public void addResultFact(ResultFact fact) {
        if (fact instanceof SlidingWindowQualityValidationResultFact) {
            addStatsFact((SlidingWindowQualityValidationResultFact)fact);
        }
    }

    public void addStatsFact(SlidingWindowQualityValidationResultFact fact) {
        facts.add(fact);
    }

    @Override
    public void align(List<ValidationResultEntry> list) {
        int max = 0;
        for (ValidationResultEntry entry : list) {
            if (!(entry instanceof SlidingWindowQualityValidationResultEntry)) continue;
            int size = entry.getResultFacts().size();
            max = max > size ? max : size;
        }

        for (ValidationResultEntry entry : list) {
            if (!(entry instanceof SlidingWindowQualityValidationResultEntry)) continue;
            int size = entry.getResultFacts().size();
            for (int i = 0; i < max - size; i++) {
                SlidingWindowQualityValidationResultFact tmp = new SlidingWindowQualityValidationResultFact("tmp");
                tmp.setFactName("Trace " + (size + i + 1));
                ((SlidingWindowQualityValidationResultEntry)entry).addStatsFact(tmp);
            }
        }
    }

    public static class SlidingWindowQualityValidationResultFact extends ResultFact {
        public static final String SEQUENCE_COLUMN_NAME                 = "Sequence";
        public static final String PASS_COLUMN_NAME                     = "Pass";
        public static final String TOTAL_NUMBER_OF_WINDOWS_COLUMN_NAME  = "Total number of windows";
        public static final String NUMBER_OF_FAILED_WINDOWS_COLUMN_NAME = "Number of failed windows";
        public static final String RATIO_OF_PASSED_WINDOWS_COLUMN_NAME  = "% of passed windows";
        public static final String NOTES_COLUMN_NAME                    = "Notes";

        private ResultColumn<LinkResultColumn.LinkBox> sequenceColumn;
        private ResultColumn<Boolean> passColumn;
        private ResultColumn<Integer> totalNumberOfWindowsColumn;
        private ResultColumn<Integer> numberOfFailedWindowsColumn;
        private ResultColumn<Double> ratioOfPassedWindowsColumn;
        private ResultColumn<String> notesColumn;

        public SlidingWindowQualityValidationResultFact(String factName) {
            this(factName, "-", Collections.<URN>emptyList(), false, 0, 0, "");
        }

        public SlidingWindowQualityValidationResultFact(String factName, String sequenceName, List<URN> sequenceLinks, boolean pass, int totalNumberOfWindows, int numberOfFailedWindows, String notes) {
            super(factName);

            initColumns(sequenceName, sequenceLinks, pass, totalNumberOfWindows, numberOfFailedWindows, notes);
        }

        @Override
        public List<ResultColumn> getColumns() {
            return Arrays.<ResultColumn>asList(
                    sequenceColumn,
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
                ratioOfPassedWindowsColumn = column;
            } else if (columnName.equals(NUMBER_OF_FAILED_WINDOWS_COLUMN_NAME)) {
                numberOfFailedWindowsColumn = column;
            } else if (columnName.equals(PASS_COLUMN_NAME)) {
                passColumn = column;
            } else if (columnName.equals(NOTES_COLUMN_NAME)) {
                notesColumn = column;
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

            ratioOfPassedWindowsColumn.setData((double)(totalNumberOfWindows - numberOfFailedWindowsColumn.getData())/totalNumberOfWindows);
        }
        public int getTotalNumberOfWindows() {
            return totalNumberOfWindowsColumn.getData();
        }

        public void setNumberOfFailedWindows(int numberOfFailedWindows) {
            numberOfFailedWindowsColumn.setData(numberOfFailedWindows);

            int totalNumberOfWindows = totalNumberOfWindowsColumn.getData();

            ratioOfPassedWindowsColumn.setData((double)(totalNumberOfWindows - numberOfFailedWindows)/totalNumberOfWindows);
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
            passColumn = new BooleanResultColumn(RESULT_COLUMN);
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
}