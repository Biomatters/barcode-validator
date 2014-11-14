package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.URN;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Frank Lee
 *         Created on 12/11/14 3:46 PM
 */
public class StatusFact extends ResultFact {
    public static final String DOCUMENT = "Document";
    public static final String IDENTITY = "Identity%";
    public static final String STATUS = "Status";

    private LinkResultColumn nameCol;
    private ResultColumn<Double> identityCol;
    private ResultColumn<Boolean> statusCol;

    public StatusFact(String name) {
        super(name);
        nameCol = new LinkResultColumn(DOCUMENT);
        identityCol = new DoubleResultColumn(IDENTITY);
        statusCol = new BooleanResultColumn(STATUS);

        setName("None");
        setStatus(false);
        setIdentity(0);
    }

    public StatusFact() {
        this("Consensus");
    }

    @Override
    public List<ResultColumn> getColumns() {
        List<ResultColumn> ret = new ArrayList<ResultColumn>();
//        ret.add(nameCol);
        ret.add(identityCol);
        ret.add(statusCol);
        return ret;
    }

    @Override
    public void addColumns(ResultColumn column) {
        if (DOCUMENT.equals(column.getName())) {
            nameCol = (LinkResultColumn) column;
        } else
        if (IDENTITY.equals(column.getName())) {
            identityCol = column;
        } else if (STATUS.equals(column.getName())) {
            statusCol = column;
        }
    }

    public void setName(String s) {
        nameCol.getData().setLable(s);
    }

    public void setLink(List<URN> links) {
        nameCol.getData().setLink(links);
    }

    public void addLink(URN link) {
        nameCol.getData().addLink(link);
    }

    public void setIdentity(double ratio) {
        identityCol.setData(ratio);
    }

    public void setStatus(boolean status) {
        statusCol.setData(status);
    }
}