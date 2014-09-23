package com.biomatters.plugins.barcoding.validator.validation.trimming;

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
 * Functionality for trimming NucleotideSequenceDocuments. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 9/09/14 11:03 AM
 */
public class SequenceTrimmer {
    private SequenceTrimmer() {
    }

    /**
     * Trims NucleotideSequenceDocuments by removing regions from the ends their sequences.
     *
     * @param documents Documents.
     * @param errorProbabilityLimit Error probability limit.
     * @return Trimmed Documents.
     * @throws DocumentOperationException
     */
    public static List<NucleotideSequenceDocument>
    trimNucleotideSequenceDocuments(List<NucleotideSequenceDocument> documents, double errorProbabilityLimit)
            throws DocumentOperationException {
        List<NucleotideSequenceDocument> trimmedSequences = new ArrayList<NucleotideSequenceDocument>();

        try {
            for (NucleotideSequenceDocument trace : documents) {
                Trimmage trimmage = ErrorProbabilityTrimmer.getTrimmage(trace, TrimmableEnds.Both, errorProbabilityLimit);

                trimmedSequences.add(trimNucleotideSequenceDocument(trace, trimmage));
            }
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not trim NucleotideSequenceDocuments: " + e.getMessage(), e);
        }

        return trimmedSequences;
    }

    /**
     * Trims character sequences by removing regions from its ends.
     *
     * @param sequence Character sequence.
     * @param trimmage Region lengths.
     * @return Trimmed character sequence.
     */
    public static SequenceCharSequence trimCharacterSequence(SequenceCharSequence sequence, Trimmage trimmage) {
        return sequence.subSequence(trimmage.trimAtStart, sequence.length() - trimmage.trimAtEnd);
    }

    /**
     * Trims a single NucleotideSequenceDocument by removing regions from the ends of the its sequence.
     *
     * @param document NucleotideSequenceDocument for trimming.
     * @param trimmage Region lengths.
     * @return Trimmed NucleotideSequenceDocument.
     * @throws DocumentOperationException
     */
    private static NucleotideSequenceDocument trimNucleotideSequenceDocument(NucleotideSequenceDocument document,
                                                                             Trimmage trimmage)
            throws DocumentOperationException {
        try {
            return createNucleotideSequenceDocument(trimCharacterSequence(document.getCharSequence(), trimmage),
                                                                          document.getSequenceAnnotations(),
                                                                          document.isCircular(),
                                                                          document.getDisplayableFields(),
                                                                          document.getName(),
                                                                          document.getURN(),
                                                                          document.getCreationDate(),
                                                                          document.getDescription());
        } catch (IndexOutOfBoundsException e) {
            throw new DocumentOperationException("Could not trim '" + document.getName() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Creates a NucleotideSequenceDocument.
     *
     * @return NucleotideSequenceDocument.
     */
    private static NucleotideSequenceDocument
    createNucleotideSequenceDocument(final SequenceCharSequence sequence,
                                     final List<SequenceAnnotation> sequenceAnnotations,
                                     final boolean isCircular,
                                     final List<DocumentField> displayableFields,
                                     final String name,
                                     final URN urn,
                                     final Date creationDate,
                                     final String description) {
        return new NucleotideSequenceDocument() {
            @Override
            public String getSequenceString() {
                return sequence.toString();
            }

            @Override
            public int getSequenceLength() {
                return sequence.length();
            }

            @Override
            public SequenceCharSequence getCharSequence() {
                return sequence;
            }

            @Override
            public List<SequenceAnnotation> getSequenceAnnotations() {
                return sequenceAnnotations;
            }

            @Override
            public boolean isCircular() {
                return isCircular;
            }

            @Override
            public List<DocumentField> getDisplayableFields() {
                return displayableFields;
            }

            @Override
            public Object getFieldValue(String fieldCodeName) {
                for (DocumentField field : displayableFields)
                    if (field.getCode().equals(fieldCodeName))
                        return field;

                return null;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public URN getURN() {
                return urn;
            }

            @Override
            public Date getCreationDate() {
                return creationDate;
            }

            @Override
            public String getDescription() {
                return description;
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
    }
}