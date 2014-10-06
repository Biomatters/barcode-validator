package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.components.DefaultHyperlinkListener;
import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.MalformedURNException;
import com.biomatters.geneious.publicapi.documents.URN;

import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Matthew Cheung
 *         Created on 2/09/14 12:51 PM
 */
public class DocumentOpeningHyperlinkListener extends DefaultHyperlinkListener {
    private final AtomicBoolean hyperlinkClickBeingProcessed = new AtomicBoolean(false);
    private final String owner;

    /**
     * @param owner the name of the code that 'owns' this instance.
     *              Used eg as part of the name of the thread that parses the URNs and selects the documents, any RuntimeExceptions that thread may throw
     *                      and for storing the "document(s) may have been modified" don't show again dialog preference
     */
    public DocumentOpeningHyperlinkListener(String owner) {
        this.owner = owner;
    }


    @Override
    public void hyperlinkUpdate(final HyperlinkEvent ev) {
        if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED && !(ev instanceof HTMLFrameHyperlinkEvent)) {
            final String urlString = ev.getDescription();
            if (!urlString.toLowerCase().startsWith("urn:")) { // So we can support a mixture of urn hyperlinks and standard hyperlinks
                super.hyperlinkUpdate(ev);
                return;
            }
            new Thread("DocumentOpeningHyperlinkListener for "+owner) {
                public void run() {
                    if (hyperlinkClickBeingProcessed.getAndSet(true)) return;
                    try {
                        boolean someDocsAtDifferentRevisions = false;
                        List<URN> urns = new ArrayList<URN>();
                        for (String urnAndRevisionString : urlString.split(",")) {
                            String[] urnAndRevision = urnAndRevisionString.trim().split("@");
                            URN urn;
                            try {
                                urn = new URN(urnAndRevision[0]);
                            }
                            catch (MalformedURNException ex) {
                                throw new RuntimeException("Malformed URN '"+urnAndRevisionString+"' in String '"+urlString+"'. Owner was "+owner, ex);
                            }
                            urns.add(urn);
                            if (urnAndRevision.length > 1) {
                                int revision = Integer.parseInt(urnAndRevision[1]);
                                AnnotatedPluginDocument annotatedPluginDocument = DocumentUtilities.getDocumentByURN(urn);
                                if (annotatedPluginDocument.getRevisionNumber() != revision) {
                                    someDocsAtDifferentRevisions = true;
                                }
                            }
                        }
                        boolean plural = urns.size() > 1;
                        if (someDocsAtDifferentRevisions) {
                            String title = "Document" + (plural?"s":"") + " Modified";
                            String message = "The document" + (plural?"s":"") + " may have been modified since " + (plural?"they were":"it was") + " referenced";
                            final Object clickedButton = Dialogs.showDialogWithDontShowAgain(
                                    new Dialogs.DialogOptions(Dialogs.OK_CANCEL, title, null, Dialogs.DialogIcon.INFORMATION),
                                    message, "DocumentOpeningHyperlinkListener." + owner, "Don't show this again");
                            if (Dialogs.CANCEL == clickedButton) {
                                return;
                            }
                        }
                        if (!DocumentUtilities.selectDocuments(urns)) {
                            String title = "Document" + (plural?"s":"") +  " not found";
                            String message = "The document" + (plural ? "s" : "") + " could not be found";
                            Dialogs.showMessageDialog(message, title, null, Dialogs.DialogIcon.INFORMATION);
                        }
                    }
                    finally {
                        hyperlinkClickBeingProcessed.set(false);
                    }
                }
            }.start();
        }
        else {
            super.hyperlinkUpdate(ev);
        }
    }
}
