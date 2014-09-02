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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static interface HyperlinkHandler {
        public void hyperlinkClicked(String hyperlink);
    }

    private final Map<String, HyperlinkHandler> hyperlinkHandlers = new HashMap<String, HyperlinkHandler>();

    /**
     * Adds an alternative handler for any URL that starts with this protocol followed by a colon
     * @param protocol
     * @param hyperlinkHandler
     */
    public void setProtocolHandler(String protocol, HyperlinkHandler hyperlinkHandler) {
        hyperlinkHandlers.put(protocol, hyperlinkHandler);
    }

    @Override
    public void hyperlinkUpdate(final HyperlinkEvent ev) {
        if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED && !(ev instanceof HTMLFrameHyperlinkEvent)) {
            final String urlString = ev.getDescription();
            if (!hasProtocolHandler(urlString,false)) {
                if (!urlString.toLowerCase().startsWith("urn:")) { // So we can support a mixture of urn hyperlinks and standard hyperlinks
                    super.hyperlinkUpdate(ev);
                    return;
                }
            }
            new Thread("DocumentOpeningHyperlinkListener for "+owner) {
                public void run() {
                    if (hyperlinkClickBeingProcessed.getAndSet(true)) return;
                    try {
                        // we use ev.getDescription() because ev.getURL() sometimes returns null (happens on Windows)
                        if (hasProtocolHandler(urlString,true)) return;
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

    private boolean hasProtocolHandler(String urlString, boolean invokeIt) {
        int colonIndex = urlString.indexOf(':');
        if (colonIndex>=0) {
            String protocol = urlString.substring(0,colonIndex);
            final HyperlinkHandler hyperlinkHandler = hyperlinkHandlers.get(protocol);
            if (hyperlinkHandler!=null) {
                if (invokeIt)
                    hyperlinkHandler.hyperlinkClicked(urlString.substring(colonIndex+1));
                return true;
            }
        }
        return false;
    }
}
