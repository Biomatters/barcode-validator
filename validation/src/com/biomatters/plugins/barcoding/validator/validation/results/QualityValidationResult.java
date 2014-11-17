package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Frank Lee
 *         Created on 11/11/14 5:11 PM
 */
public class QualityValidationResult extends ValidationResultEntry {
    private List<StatsFact> traceFacts = new ArrayList<StatsFact>();

    @SuppressWarnings("unused")
    protected QualityValidationResult(String name) {
        super(name);
    }

    @SuppressWarnings("unused")
    public QualityValidationResult(Element element) throws XMLSerializationException {
        super(element);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List getRow() {
        List ret = new ArrayList();
        for (StatsFact fact : traceFacts) {
            for (ResultColumn column : fact.getColumns()) {
                ret.add(column.getDisplayValue());
            }
        }

        return ret;
    }

    @Override
    public List<String> getCol() {
        List<String> ret = new ArrayList<String>();
        for (StatsFact fact : traceFacts) {
            for (ResultColumn column : fact.getColumns()) {
                ret.add(column.getName());
            }
        }
        return ret;
    }

    @Override
    public List<ResultColumn> getColumns() {
        List<ResultColumn> ret = new ArrayList<ResultColumn>();
        for (StatsFact fact : traceFacts) {
            for (ResultColumn column : fact.getColumns()) {
                ret.add(column);
            }
        }

        return ret;
    }

    public QualityValidationResult() {
        super("Quality Validation");
    }

    @Override
    public List<ResultFact> getResultFacts() {
        List<ResultFact> ret = new ArrayList<ResultFact>();
//        ret.add(consensusFact);
        ret.addAll(traceFacts);
        return ret;
    }

    @Override
    public void addResultFact(ResultFact fact) {
        addStatsFact((StatsFact) fact);
    }

    public void addStatsFact(StatsFact fact) {
        traceFacts.add(fact);
    }

    @Override
    public void align(List<ValidationResultEntry> list) {
        int max = 0;
        for (ValidationResultEntry entry : list) {
            if (!(entry instanceof QualityValidationResult)) continue;
            int size = entry.getResultFacts().size();
            max = max > size ? max : size;
        }

        for (ValidationResultEntry entry : list) {
            if (!(entry instanceof QualityValidationResult)) continue;
            int size = entry.getResultFacts().size();
            for (int i = 0; i < max - size; i++) {
                StatsFact tmp = new StatsFact();
                tmp.setFactName("Trace " + (size + i + 1));
                ((QualityValidationResult)entry).addStatsFact(tmp);
            }
        }
    }

    public static class StatsFact extends ResultFact {
        public static final String TRACE_NAME = "Trace Name";
        public static final String PASS_RATIO = "Pass Ratio%";
        public static final String FAILED_NUM = "Failed Num";
        public static final String STATUS = "Status";

        private LinkResultColumn nameCol;
        private ResultColumn<Double> passRatioCol;
        private ResultColumn<Integer> failNumCol;
        private ResultColumn<Boolean> statusCol;

        public StatsFact() {
            this("Stat");
            setName("-");
            setFailNum(0);
            setStatus(false);
            setPassRatio(0);
        }

        public StatsFact(String name) {
            super(name);
            nameCol = new LinkResultColumn(TRACE_NAME);
            passRatioCol = new DoubleResultColumn(PASS_RATIO);
            failNumCol = new IntegerResultColumn(FAILED_NUM);
            statusCol = new BooleanResultColumn(STATUS);

            setName("None");
            setPassRatio(0);
            setFailNum(-1);
            setStatus(false);
        }

        @Override
        public List<ResultColumn> getColumns() {
            List<ResultColumn> ret = new ArrayList<ResultColumn>();
            ret.add(nameCol);
            ret.add(passRatioCol);
            ret.add(failNumCol);
            ret.add(statusCol);
            return ret;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void addColumns(ResultColumn column) {
            if (TRACE_NAME.equals(column.getName())) {
                nameCol = (LinkResultColumn) column;
            } else if (PASS_RATIO.equals(column.getName())) {
                passRatioCol = column;
            } else if (FAILED_NUM.equals(column.getName())) {
                failNumCol = column;
            } else if (STATUS.equals(column.getName())) {
                statusCol = column;
            }
        }

        public void setName(String name) {
            nameCol.getData().setLable(name);
        }

        public void setLinks(List<URN> links) {
            nameCol.getData().setLink(links);
        }

        public void addLink(URN link) {
            nameCol.getData().addLink(link);
        }

        public void setPassRatio(double ratio) {
            passRatioCol.setData(ratio);
        }

        public void setFailNum(int num) {
            failNumCol.setData(num);
        }

        public void setStatus(boolean status) {
            statusCol.setData(status);
        }

        @SuppressWarnings("unused")
        public double getPassRatio() {
            return passRatioCol.getData();
        }

        @SuppressWarnings("unused")
        public int getFailNum() {
            return failNumCol.getData();
        }

        public boolean getStatus() {
            return statusCol.getData();
        }
    }
}
