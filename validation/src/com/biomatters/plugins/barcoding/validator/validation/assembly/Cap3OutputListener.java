package com.biomatters.plugins.barcoding.validator.validation.assembly;

import com.biomatters.geneious.publicapi.utilities.Execution;

/**
 * @author Lars Smits
 *       <p />
 *       Created on 20/12/11 12:53 PM
 */public class Cap3OutputListener extends Execution.OutputListener {
    private StringBuilder stdoutOutput = new StringBuilder();
    private StringBuilder stderrOutput = new StringBuilder();

    @Override
    public void stdoutWritten(String s) {
        stdoutOutput.append(s).append("\n");
    }

    @Override
    public void stderrWritten(String s) {
        stderrOutput.append(s).append("\n");
    }

    public String getStdoutOutput() {
        return stdoutOutput.toString();
    }

    public String getStderrOutput() {
        return stderrOutput.toString();
    }
}
