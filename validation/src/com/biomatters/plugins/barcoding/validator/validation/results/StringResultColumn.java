package com.biomatters.plugins.barcoding.validator.validation.results;

/**
 * @author Gen Li
 *         Created on 21/11/14 8:02 AM
 */
public class StringResultColumn extends ResultColumn<String> {
    public StringResultColumn() {
        super("");
    }

    public StringResultColumn(String name) {
        super(name);
    }

    @Override
    protected void setDataFromString(String str) {
        setData(str);
    }
}
