package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.plugin.Icons;
import com.biomatters.geneious.publicapi.plugin.Options;

/**
 * @author Matthew Cheung
 *         Created on 22/07/14 5:27 PM
 */
public class Utilities {


    public static void addQuestionToOptions(Options options, String question) {
        options.addLabelWithIcon("<html><b>Question</b>: " + question + "</html>",
                            new Icons(Dialogs.DialogIcon.QUESTION.getIcon()));
    }

    public static void addNoteToOptions(Options options, String message) {
        options.addLabelWithIcon("<html><b>Note</b>: " + message + "</html>",
                new Icons(Dialogs.DialogIcon.INFORMATION.getIcon()));
    }
}
