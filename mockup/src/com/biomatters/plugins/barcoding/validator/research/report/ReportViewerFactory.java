package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.components.GTextPane;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.plugin.DocumentViewerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * @author Matthew Cheung
 *         Created on 2/09/14 12:50 PM
 */
public class ReportViewerFactory extends DocumentViewerFactory {

    public String getName() {
            return "Validation Report (Mockup)";
        }

        public String getDescription() {
            return "Contains a report of the results of an assembly operation";
        }

        public String getHelp() {
            return null;
        }

        public DocumentSelectionSignature[] getSelectionSignatures() {
            return new DocumentSelectionSignature[] {new DocumentSelectionSignature(MockupReport.class, 1, 1)};
        }

        public DocumentViewer createViewer(final AnnotatedPluginDocument[] annotatedDocuments) {
            return new DocumentViewer() {
                public JComponent getComponent() {
                    PluginDocument doc = annotatedDocuments[0].getDocumentOrNull();
                    if(doc == null) {
                        return null;
                    }

                    MockupReport report = (MockupReport)doc;
                    return createComponentToDisplayReport(report, false);
                }
            };
        }

        static JComponent createComponentToDisplayReport(MockupReport report, boolean isForDisplayInDialog) {
            final JTextPane textPane = new GTextPane();
            if (isForDisplayInDialog) {
                textPane.setOpaque(false);
            }
            textPane.setContentType("text/html");
            textPane.setEditable(false);
            final DocumentOpeningHyperlinkListener hyperlinkListener = new DocumentOpeningHyperlinkListener("MockupReportDocumentFactory");
            textPane.addHyperlinkListener(hyperlinkListener);
            textPane.setText(report.toHTML());
            final JScrollPane scroll = new JScrollPane(textPane) {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(super.getPreferredSize().width,super.getPreferredSize().height+30); // increase height by potential horizontal scroll bar height to fix GEN-21094
                }
            };
            scroll.getViewport().setOpaque(false);
            scroll.setOpaque(false);
            scroll.setBorder(null);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    textPane.scrollRectToVisible(new Rectangle(0,0));
                }
            });
            return scroll;
        }
}
