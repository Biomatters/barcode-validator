package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.components.GTextPane;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

/**
 * A viewer that displays some HTML in a scroll pane.
 *
 * @author Matthew Cheung
 *         Created on 4/11/14 11:37 AM
 */
public abstract class HtmlReportDocumentViewer extends DocumentViewer {

    /**
     *
     * @return The HTML to display.  May return null to indicate there is nothing to display, in which case null will
     * be returned from {@link #getComponent()}
     */
    @Nullable
    public abstract String getHtml();

    /**
     *
     * @return a {@link javax.swing.event.HyperlinkListener} to be used with the viewer.
     */
    @Nullable
    public abstract HyperlinkListener getHyperlinkListener();

    @Override
    public JComponent getComponent() {
        String html = getHtml();
        if(html == null || html.isEmpty()) {
            return null;
        }
        final JTextPane textPane = new GTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);

        HyperlinkListener hyperlinkListener = getHyperlinkListener();
        if(hyperlinkListener != null) {
            textPane.addHyperlinkListener(hyperlinkListener);
        }
        textPane.setText(html);
        final JScrollPane scroll = new JScrollPane(textPane) {
            @Override
            public Dimension getPreferredSize() {
                // increase height by potential horizontal scroll bar height
                return new Dimension(super.getPreferredSize().width,super.getPreferredSize().height+30);
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
