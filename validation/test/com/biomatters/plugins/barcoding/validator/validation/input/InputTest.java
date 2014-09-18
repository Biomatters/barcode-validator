package com.biomatters.plugins.barcoding.validator.research.input;

import com.biomatters.geneious.publicapi.documents.DocumentField;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.validation.input.map.FileNameMapper;
import org.jdom.Element;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * @author Gen Li
 *         Created on 8/09/14 2:36 PM
 */
public class InputTest extends Assert {
    @Test
    public void testSplitByName() throws DocumentOperationException {
        final String separator = "_";
        List<NucleotideSequenceDocument> traces;
        List<NucleotideSequenceDocument> barcodes;

        NucleotideSequenceDocument t1 = new NucleotideSequenceDocument() {
            @Override
            public String getSequenceString() { return null; }
            @Override
            public int getSequenceLength() { return 0; }
            @Override
            public SequenceCharSequence getCharSequence() { return null; }
            @Override
            public List<SequenceAnnotation> getSequenceAnnotations() { return null; }
            @Override
            public boolean isCircular() { return false; }
            @Override
            public List<DocumentField> getDisplayableFields() { return null; }
            @Override
            public Object getFieldValue(String fieldCodeName) { return null; }
            @Override
            public String getName() { return "trace1" + separator + "1"; }
            @Override
            public URN getURN() { return null; }
            @Override
            public Date getCreationDate() { return null; }
            @Override
            public String getDescription() { return null; }
            @Override
            public String toHTML() { return null; }
            @Override
            public Element toXML() { return null; }
            @Override
            public void fromXML(Element element) throws XMLSerializationException { }
        };
        NucleotideSequenceDocument t2 = new NucleotideSequenceDocument() {
            @Override
            public String getSequenceString() { return null; }
            @Override
            public int getSequenceLength() { return 0; }
            @Override
            public SequenceCharSequence getCharSequence() { return null; }
            @Override
            public List<SequenceAnnotation> getSequenceAnnotations() { return null; }
            @Override
            public boolean isCircular() { return false; }
            @Override
            public List<DocumentField> getDisplayableFields() { return null; }
            @Override
            public Object getFieldValue(String fieldCodeName) { return null; }
            @Override
            public String getName() { return "trace2" + separator + "1"; }
            @Override
            public URN getURN() { return null; }
            @Override
            public Date getCreationDate() { return null; }
            @Override
            public String getDescription() { return null; }
            @Override
            public String toHTML() { return null; }
            @Override
            public Element toXML() { return null; }
            @Override
            public void fromXML(Element element) throws XMLSerializationException { }
        };
        NucleotideSequenceDocument t3 = new NucleotideSequenceDocument() {
            @Override
            public String getSequenceString() { return null; }
            @Override
            public int getSequenceLength() { return 0; }
            @Override
            public SequenceCharSequence getCharSequence() { return null; }
            @Override
            public List<SequenceAnnotation> getSequenceAnnotations() { return null; }
            @Override
            public boolean isCircular() { return false; }
            @Override
            public List<DocumentField> getDisplayableFields() { return null; }
            @Override
            public Object getFieldValue(String fieldCodeName) { return null; }
            @Override
            public String getName() { return "trace3" + separator + "1"; }
            @Override
            public URN getURN() { return null; }
            @Override
            public Date getCreationDate() { return null; }
            @Override
            public String getDescription() { return null; }
            @Override
            public String toHTML() { return null; }
            @Override
            public Element toXML() { return null; }
            @Override
            public void fromXML(Element element) throws XMLSerializationException { }
        };
        NucleotideSequenceDocument t4 = new NucleotideSequenceDocument() {
            @Override
            public String getSequenceString() { return null; }
            @Override
            public int getSequenceLength() { return 0; }
            @Override
            public SequenceCharSequence getCharSequence() { return null; }
            @Override
            public List<SequenceAnnotation> getSequenceAnnotations() { return null; }
            @Override
            public boolean isCircular() { return false; }
            @Override
            public List<DocumentField> getDisplayableFields() { return null; }
            @Override
            public Object getFieldValue(String fieldCodeName) { return null; }
            @Override
            public String getName() { return "trace4" + separator + "2"; }
            @Override
            public URN getURN() { return null; }
            @Override
            public Date getCreationDate() { return null; }
            @Override
            public String getDescription() { return null; }
            @Override
            public String toHTML() { return null; }
            @Override
            public Element toXML() { return null; }
            @Override
            public void fromXML(Element element) throws XMLSerializationException { }
        };

        NucleotideSequenceDocument b1 = new NucleotideSequenceDocument() {
            @Override
            public String getSequenceString() { return null; }
            @Override
            public int getSequenceLength() { return 0; }
            @Override
            public SequenceCharSequence getCharSequence() { return null; }
            @Override
            public List<SequenceAnnotation> getSequenceAnnotations() { return null; }
            @Override
            public boolean isCircular() { return false; }
            @Override
            public List<DocumentField> getDisplayableFields() { return null; }
            @Override
            public Object getFieldValue(String fieldCodeName) { return null; }
            @Override
            public String getName() { return "1" + separator + "barcode1"; }
            @Override
            public URN getURN() { return null; }
            @Override
            public Date getCreationDate() { return null; }
            @Override
            public String getDescription() { return null; }
            @Override
            public String toHTML() { return null; }
            @Override
            public Element toXML() { return null; }
            @Override
            public void fromXML(Element element) throws XMLSerializationException { }
        };
        NucleotideSequenceDocument b2 = new NucleotideSequenceDocument() {
            @Override
            public String getSequenceString() { return null; }
            @Override
            public int getSequenceLength() { return 0; }
            @Override
            public SequenceCharSequence getCharSequence() { return null; }
            @Override
            public List<SequenceAnnotation> getSequenceAnnotations() { return null; }
            @Override
            public boolean isCircular() { return false; }
            @Override
            public List<DocumentField> getDisplayableFields() { return null; }
            @Override
            public Object getFieldValue(String fieldCodeName) { return null; }
            @Override
            public String getName() { return "2" + separator + "barcode2"; }
            @Override
            public URN getURN() { return null; }
            @Override
            public Date getCreationDate() { return null; }
            @Override
            public String getDescription() { return null; }
            @Override
            public String toHTML() { return null; }
            @Override
            public Element toXML() { return null; }
            @Override
            public void fromXML(Element element) throws XMLSerializationException { }
        };

        traces = Arrays.asList(t1, t2, t3, t4);
        barcodes = Arrays.asList(b1, b2);

        FileNameMapper mapper = new FileNameMapper("_", 1, "_", 0);

        Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>> mapped = mapper.map(barcodes, traces);

        List<NucleotideSequenceDocument> mappedToB1 = mapped.get(mapped.keySet().toArray()[0]);
        List<NucleotideSequenceDocument> mappedToB2 = mapped.get(mapped.keySet().toArray()[1]);

        assertEquals(2, mapped.keySet().size());

        assertEquals(3, mappedToB1.size());
        assertTrue(mappedToB1.contains(t1));
        assertTrue(mappedToB1.contains(t2));
        assertTrue(mappedToB1.contains(t3));

        assertEquals(1, mappedToB2.size());
        assertTrue(mappedToB2.contains(t4));
    }
}