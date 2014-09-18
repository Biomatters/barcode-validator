package com.biomatters.plugins.barcoding.validator.validation.assembly;

import com.biomatters.geneious.publicapi.documents.DocumentField;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.documents.sequence.*;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

import com.biomatters.geneious.publicapi.plugin.TestGeneious;
import org.jdom.Element;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 28/08/14 3:37 PM
 */
public class AssemblyTest extends Assert {
    @Test
    public void testContigAssembled() throws DocumentOperationException {
        TestGeneious.initialize();
        TestGeneious.initializeAllPlugins();
        final String theSequence = "ACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTG";
        NucleotideSequenceDocument document = new NucleotideSequenceDocument() {
            @Override
            public String getSequenceString() {
                return theSequence;
            }

            @Override
            public int getSequenceLength() {
                return 4;
            }

            @Override
            public SequenceCharSequence getCharSequence() {
                return null;
            }

            @Override
            public List<SequenceAnnotation> getSequenceAnnotations() {
                return new ArrayList<SequenceAnnotation>();
            }

            @Override
            public boolean isCircular() {
                return false;
            }

            @Override
            public List<DocumentField> getDisplayableFields() {
                return null;
            }

            @Override
            public Object getFieldValue(String fieldCodeName) {
                return null;
            }

            @Override
            public String getName() {
                return "testDoc";
            }

            @Override
            public URN getURN() {
                return null;
            }

            @Override
            public Date getCreationDate() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Test document";
            }

            @Override
            public String toHTML() {
                return null;
            }

            @Override
            public Element toXML() {
                return null;
            }

            @Override
            public void fromXML(Element element) throws XMLSerializationException {

            }
        };

        List<NucleotideSequenceDocument> documents = new ArrayList<NucleotideSequenceDocument>();
        documents.add(document);
        documents.add(document);

        List<SequenceAlignmentDocument> result = Cap3AssemblerRunner.assemble(documents, 40, 90);
        assertEquals(1, result.size());
        List<SequenceDocument> sequences = result.get(0).getSequences();
        assertEquals(3, sequences.size());
        for (int i = 1; i < sequences.size(); i++) {
            String withNoGaps = sequences.get(i).getSequenceString().replace("-", "");
            assertEquals(theSequence, withNoGaps);
        }
    }
}
