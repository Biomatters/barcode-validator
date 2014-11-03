package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.components.DefaultHyperlinkListener;
import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.MalformedURNException;
import com.biomatters.geneious.publicapi.documents.URN;

import javax.annotation.Nullable;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A hyperlink listener that can be used with links referencing Geneious documents by their URN.  Delegates to the
 * standard {@link com.biomatters.geneious.publicapi.components.DefaultHyperlinkListener} if the link does not start
 * with "urn:"
 *
 * @author Matthew Cheung
 *         Created on 2/09/14 12:51 PM
 */
public class DocumentOpeningHyperlinkListener extends DefaultHyperlinkListener {
    public static final String URN_PREFIX = "urn:";

    private final AtomicBoolean hyperlinkClickBeingProcessed = new AtomicBoolean(false);
    private final String owner;
    private final Map<String, UrlProcessor> extraProcessors;

    /**
     * @param owner the name of the code that 'owns' this instance.
     *              Used eg as part of the name of the thread that parses the URNs and selects the documents, any RuntimeExceptions that thread may throw
     *                      and for storing the "document(s) may have been modified" don't show again dialog preference
     * @param extraProcessors Extra {@link com.biomatters.plugins.barcoding.validator.research.report.DocumentOpeningHyperlinkListener.UrlProcessor}
     *                        that can be used to add extra functionality to this listener.  Any URLs starting with the
     *                        prefix (case insensitive) will be passed to the processor instead of the default implementation.
     */
    public DocumentOpeningHyperlinkListener(String owner, @Nullable Map<String, UrlProcessor> extraProcessors) {
        this.owner = owner;
        this.extraProcessors = extraProcessors == null ? Collections.<String, UrlProcessor>emptyMap() : extraProcessors;
    }

    public static abstract class UrlProcessor {
        abstract void process(String url);
    }


    @Override
    public void hyperlinkUpdate(final HyperlinkEvent ev) {
        if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED && !(ev instanceof HTMLFrameHyperlinkEvent)) {
            final String urlString = ev.getDescription();
            final List<String> prefixes = new ArrayList<String>(extraProcessors.keySet());
            prefixes.add(URN_PREFIX);

            boolean useSuper = true;
            for (String prefix : prefixes) {
                if(urlString.toLowerCase().startsWith(prefix.toLowerCase())) {
                    useSuper = false;
                }
            }

            if (useSuper) { // So we can support a mixture of urn hyperlinks and standard hyperlinks
                super.hyperlinkUpdate(ev);
                return;
            }

            new Thread("DocumentOpeningHyperlinkListener for " + owner) {
                public void run() {
                    if (hyperlinkClickBeingProcessed.getAndSet(true)) return;
                    try {
                        UrlProcessor processor = null;
                        for (Map.Entry<String, UrlProcessor> entry : extraProcessors.entrySet()) {
                            if(urlString.toLowerCase().startsWith(entry.getKey().toLowerCase())) {
                                processor = entry.getValue();
                            }
                        }
                        if (processor != null) {
                            processor.process(urlString);
                        } else {
                            processURN();
                        }
                    }
                    finally {
                        hyperlinkClickBeingProcessed.set(false);
                    }
                }

                private void processURN() {
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
            }.start();
        }
        else {
            super.hyperlinkUpdate(ev);
        }
    }
}
