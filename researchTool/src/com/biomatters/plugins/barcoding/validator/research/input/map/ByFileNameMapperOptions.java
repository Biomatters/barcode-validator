package com.biomatters.plugins.barcoding.validator.research.input.map;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorPlugin;

/**
 * @author Gen Li
 *         Created on 5/09/14 9:53 AM
 */
public class ByFileNameMapperOptions extends Options {
    private static final String TRACE_PART_NUMBER    = "tracePartNum";
    private static final String SEQUENCE_PART_NUMBER = "seqPartNum";

    private static final String TRACE_SEPARATOR    = "traceSeparator";
    private static final String SEQUENCE_SEPARATOR = "seqSeparator";

    public ByFileNameMapperOptions() {
        super(BarcodeValidatorPlugin.class);

        addTraceMatchingOption();

        addSequenceMatchingOption();
    }

    public int getTraceNamePartNumber() {
        return getPartNumber(TRACE_PART_NUMBER);
    }

    public int getSequenceNamePartNumber() {
        return getPartNumber(SEQUENCE_PART_NUMBER);
    }

    public String getTraceSeparator() {
        return getSeparator(TRACE_SEPARATOR);
    }

    public String getSequenceSeparator() {
        return getSeparator(SEQUENCE_SEPARATOR);
    }

    private void addTraceMatchingOption() {
        beginAlignHorizontally(null, false);
        addCustomOption(new NamePartOption(TRACE_PART_NUMBER, ""));
        addCustomOption(new NameSeparatorOption(TRACE_SEPARATOR, "part of trace name separated by "));
        endAlignHorizontally();
    }

    private void addSequenceMatchingOption() {
        beginAlignHorizontally(null, false);
        addCustomOption(new NamePartOption(SEQUENCE_PART_NUMBER, ""));
        addCustomOption(new NameSeparatorOption(SEQUENCE_SEPARATOR, "part of sequence name separated by "));
        endAlignHorizontally();
    }

    private int getPartNumber(String partNumber) {
        return ((NamePartOption)getOption(partNumber)).getPart();
    }

    private String getSeparator(String separator) {
        return ((NameSeparatorOption)getOption(separator)).getSeparatorString();
    }
}