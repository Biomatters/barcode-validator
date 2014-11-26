package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

import java.util.List;
import java.util.Map;

/**
 * @author Gen Li
 *         Created on 5/09/14 9:57 AM
 */
public class GenbankXmlMapper extends BarcodesToTracesMapper {
    @Override
    public Map<AnnotatedPluginDocument, List<AnnotatedPluginDocument>> map(List<AnnotatedPluginDocument> traces,
                                                                           List<AnnotatedPluginDocument> barcodeSequences)
            throws DocumentOperationException {
        return null;
    }
}