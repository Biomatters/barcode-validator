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
    private final Option<String, ? extends JComponent> limitLabel;

    public ErrorProbabilityOptions() {
        String errorMethodDescription = "Trim bases up until the point where trimming further bases will only improve the error rate by less than the limit.";
        limit = addDoubleOption("errorLimit", "Error probability limit:", 0.05, 0.0, Double.MAX_VALUE);
        limit.setUnits("(decrease to trim more)");
        limitLabel = addLabel("");
        limitLabel.setRestoreDefaultApplies(false);
        limitLabel.setRestorePreferenceApplies(false);
        SimpleListener listener = new SimpleListener() {
            public void objectChanged() {
                String fontTag = "<font" + (limit.isEnabled() ? "" : " color=\"gray\"") + (SystemUtilities.isMac() ? " size=-1" : "") + ">";
                String percentage = String.format("%1.1f", limit.getValue() * 100);
                if (percentage.endsWith("0"))
                    percentage = percentage.substring(0, percentage.length() - 2);
                limitLabel.setValue("<html><i>" + fontTag + "Trim regions with more than a " + percentage + "% chance of an error per base&nbsp;</font></i></html>");//&nbsp; stops italics causing last char to get partially cut-off on Windows.
            }
        };
        limit.addChangeListener(listener);
        listener.objectChanged();
        limit.setIncrement(0.001);
        limit.setDescription(errorMethodDescription);
        limitLabel.setDescription(errorMethodDescription);
    }

    public double getErrorProbabilityLimit() {
        return limit.getValue();
    }
}