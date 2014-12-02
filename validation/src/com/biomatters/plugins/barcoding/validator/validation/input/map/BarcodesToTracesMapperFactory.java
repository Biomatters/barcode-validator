package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

/**
 * @author Gen Li
 *         Created on 4/09/14 12:58 PM
 */
public class BarcodesToTracesMapperFactory {
    private BarcodesToTracesMapperFactory() {
    }

    public static BarcodeToTraceMapper getBarcodesToTracesMapper(BarcodesToTracesMapperOptions options) throws DocumentOperationException {
        if (BOLDTraceListMapperOptions.class.isAssignableFrom(options.getClass())) {
            BOLDTraceListMapperOptions BOLDTraceListMapperOptions = (BOLDTraceListMapperOptions)options;

            return new BOLDTraceListMapper(BOLDTraceListMapperOptions.getBoldListFilePath());
        } else if (GenbankXmlMapperOptions.class.isAssignableFrom(options.getClass())) {
           GenbankXmlMapperOptions genbankXmlMapperOptions = (GenbankXmlMapperOptions)options;

            return new GenbankXmlMapper(genbankXmlMapperOptions.getBoldListFilePath());
        } else if (FileNameMapperOptions.class.isAssignableFrom(options.getClass())) {
            FileNameMapperOptions fileNameMapperOptions = (FileNameMapperOptions)options;

            return new FileNameMapper(fileNameMapperOptions.getTraceSeparator(),
                                      fileNameMapperOptions.getTraceNamePartNumber(),
                                      fileNameMapperOptions.getSequenceSeparator(),
                                      fileNameMapperOptions.getSequenceNamePartNumber());
        } else {
            throw new IllegalArgumentException("Unrecognized mapper name: " + options.getClass().getSimpleName());
        }
    }
}