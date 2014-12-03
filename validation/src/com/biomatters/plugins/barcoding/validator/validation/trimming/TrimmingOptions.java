package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.plugin.Options;

/**
 * @author Gen Li
 *         Created on 21/10/14 3:10 PM
 */
public class TrimmingOptions extends Options {
    private static final String QUALITY_TRIMMING_OPTIONS_NAME = "quality";
    private static final String PRIMER_TRIMMING_OPTIONS_NAME  = "primer";

    private ErrorProbabilityOptions qualityTrimmingOptions = new ErrorProbabilityOptions();
    private PrimerTrimmingOptions primerTrimmingOptions = new PrimerTrimmingOptions(getClass());

    public TrimmingOptions(Class cls) {
        super(cls);

        init();
    }

    public ErrorProbabilityOptions getQualityTrimmingOptions() {
        return qualityTrimmingOptions;
    }

    public PrimerTrimmingOptions getPrimerTrimmingOptions() {
        return primerTrimmingOptions;
    }

    private void init() {
        addChildOptions(QUALITY_TRIMMING_OPTIONS_NAME, "By Quality", "Quality trimming", qualityTrimmingOptions, true);
        addChildOptions(PRIMER_TRIMMING_OPTIONS_NAME, "By Primer", "Primer trimming", primerTrimmingOptions, true);
    }
}