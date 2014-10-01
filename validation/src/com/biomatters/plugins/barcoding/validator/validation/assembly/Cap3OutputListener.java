package com.biomatters.plugins.barcoding.validator.validation.assembly;

import com.biomatters.geneious.publicapi.utilities.Execution;

/**
 * @author Lars Smits
 * @version $Id$
 *       <p />
 *       Created on 20/12/11 12:53 PM
 */public class Cap3OutputListener extends Execution.OutputListener {
    private StringBuilder stdouts = new StringBuilder();
    private StringBuilder stderrs = new StringBuilder();
    @Override
    public void stdoutWritten(String s) {
        stdouts.append(s).append("\n");
    }

    @Override
    public void stderrWritten(String s) {
        stderrs.append(s).append("\n");
    }

    public String getStdouts() {
        return stdouts.toString();
    }

    public String getStderrs() {
        return stderrs.toString();
    }
}
