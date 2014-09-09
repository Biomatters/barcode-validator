package com.biomatters.plugins.barcoding.validator.research.trimming;

import com.biomatters.geneious.publicapi.documents.DocumentField;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 9/09/14 11:03 AM
 */
public class NucleotideSequenceDocumentTrimmer {
    private NucleotideSequenceDocumentTrimmer() {
    }

    public static List<NucleotideSequenceDocument> trim(List<NucleotideSequenceDocument> traces,
                                                        double errorProbabilityLimit)
            throws DocumentOperationException {
        List<NucleotideSequenceDocument> trimmedSequences = new ArrayList<NucleotideSequenceDocument>();
        for (NucleotideSequenceDocument trace : traces) {
            Trimmage trimmage = ErrorProbabilityTrimmer.getTrimmage(trace, TrimmableEnds.Both, errorProbabilityLimit);
            trimmedSequences.add(trimNucleotideSequenceDocument(trace,
                                                                trimmage.trimAtStart - 1,
                                                                trimmage.trimAtEnd - 1));
        }
        return trimmedSequences;
    }

    private static NucleotideSequenceDocument trimNucleotideSequenceDocument(final NucleotideSequenceDocument document,
                                                                             final int from,
                                                                             final int to) {
        return new NucleotideSequenceDocument() {
            private SequenceCharSequence sequence = trimSequence(document.getCharSequence(), from, to);
            private NucleotideSequenceDocument originalDocument = document;

            @Override
            public String getSequenceString() { return sequence.toString(); }
            @Override
            public int getSequenceLength() { return sequence.length(); }
            @Override
            public SequenceCharSequence getCharSequence() { return sequence; }
            @Override
            public List<SequenceAnnotation> getSequenceAnnotations() {
                return originalDocument.getSequenceAnnotations();
            }
            @Override
            public boolean isCircular() { return originalDocument.isCircular(); }
            @Override
            public List<DocumentField> getDisplayableFields() { return originalDocument.getDisplayableFields(); }
            @Override
            public Object getFieldValue(String fieldCodeName) { return originalDocument.getFieldValue(fieldCodeName); }
            @Override
            public String getName() { return originalDocument.getName(); }
            @Override
            public URN getURN() { return originalDocument.getURN(); }
            @Override
            public Date getCreationDate() { return originalDocument.getCreationDate(); }
            @Override
            public String getDescription() { return originalDocument.getDescription(); }
            @Override
            public String toHTML() { return null; }
            @Override
            public Element toXML() {  return null; }
            @Override
            public void fromXML(Element element) throws XMLSerializationException {}
        };
    }

    private static SequenceCharSequence trimSequence(SequenceCharSequence sequence, int from, int to) {
        return sequence.subSequence(from, to);
    }
}