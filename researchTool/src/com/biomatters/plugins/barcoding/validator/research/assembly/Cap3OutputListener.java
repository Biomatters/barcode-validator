package com.biomatters.plugins.barcoding.validator.research.assembly;

import com.biomatters.geneious.publicapi.utilities.Execution;

/**
 * @author Lars Smits
 * @version $Id$
 *       <p />
 *       Created on 20/12/11 12:53 PM
 */public class Cap3OutputListener extends Execution.OutputListener {

    @Override
    public void stdoutWritten(String s) {
//        System.out.println(s);
    }

    @Override
    public void stderrWritten(String s) {
        System.err.println(s);
    }
}
