package com.biomatters.plugins.barcoding.validator.validation.input.map;

/**
 * @author Gen Li
 *         Created on 5/09/14 9:53 AM
 */
public class FileNameMapperOptions extends BarcodesToTracesMapperOptions {
    private NamePartOption barcodeNamePartOption;
    private NameSeparatorOption barcodeNameSeparatorOption;
    private NamePartOption traceNamePartOption;
    private NameSeparatorOption traceNameSeparatorOption;

    public FileNameMapperOptions(Class cls) {
        super(cls);

        addTraceNameOptions();
        addBarcodeNameOptions();
    }

    public int getBarcodeNamePart() {
        return barcodeNamePartOption.getPart();
    }

    public String getBarcodeNameSeparator() { return barcodeNameSeparatorOption.getSeparatorString() ; }

    public int getTraceNamePart() {
        return traceNamePartOption.getPart();
    }

    public String getTraceNameSeparator() {
        return traceNameSeparatorOption.getSeparatorString();
    }

    private void addBarcodeNameOptions() {
        beginAlignHorizontally(null, false);

        barcodeNamePartOption = addCustomOption(new NamePartOption("barcodeNamePart", ""));
        barcodeNameSeparatorOption = addCustomOption(new NameSeparatorOption("barcodeNameSeparator", "part of barcode name separated by "));

        endAlignHorizontally();
    }

    private void addTraceNameOptions() {
        beginAlignHorizontally(null, false);

        traceNamePartOption = addCustomOption(new NamePartOption("traceNamePart", ""));
        traceNameSeparatorOption = addCustomOption(new NameSeparatorOption("traceNameSeparator", "part of trace name separated by "));

        endAlignHorizontally();
    }
}