package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.SystemUtilities;
import org.virion.jam.util.SimpleListener;

import javax.swing.*;

/**
 * @author Amy Wilson
 * @version $Id$
 *          <p/>
 *          Created on 8/12/2008 12:23:10 PM
 */
public class ErrorProbabilityOptions extends Options {
    private final DoubleOption limit;

    public ErrorProbabilityOptions() {
        String errorMethodDescription = "Trim bases up until the point where trimming further bases will only improve the error rate by less than the limit.";
        limit = addDoubleOption("errorLimit", "Error Probability Limit:", 0.05, 0.0, Double.MAX_VALUE);
        limit.setUnits("(decrease to trim more)");
        limit.setIncrement(0.001);
        limit.setDescription(errorMethodDescription);
    }

    public double getErrorProbabilityLimit() {
        return limit.getValue();
    }
}