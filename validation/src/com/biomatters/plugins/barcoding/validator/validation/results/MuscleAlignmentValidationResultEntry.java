package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Frank Lee
 *         Created on 11/11/14 5:11 PM
 */
public class MuscleAlignmentValidationResultEntry extends ValidationResultEntry {
    private List<MuscleAlignmentValidationResultFact> facts = new ArrayList<MuscleAlignmentValidationResultFact>();

    @SuppressWarnings("unused")
    public MuscleAlignmentValidationResultEntry(Element element) throws XMLSerializationException {
        super(element);
    }

    public MuscleAlignmentValidationResultEntry() {
        super("Barcode Validation");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List getRow() {
        List ret = new ArrayList();
        for (MuscleAlignmentValidationResultFact fact : facts) {
            for (ResultColumn column : fact.getColumns()) {
                ret.add(column.getDisplayValue());
            }
        }
        return ret;
    }

    @Override
    public List<String> getCol() {
        List<String> ret = new ArrayList<String>();
        for (MuscleAlignmentValidationResultFact fact : facts) {
            for (ResultColumn column : fact.getColumns()) {
                ret.add(column.getName());
            }
        }
        return ret;
    }

    @Override
    public List<ResultColumn> getColumns() {
        List<ResultColumn> ret = new ArrayList<ResultColumn>();
        for (MuscleAlignmentValidationResultFact fact : facts) {
            ret.addAll(fact.getColumns());
        }
        return ret;
    }

    @Override
    public List<ResultFact> getResultFacts() {
        List<ResultFact> ret = new ArrayList<ResultFact>();
        ret.addAll(facts);
        return ret;
    }

    @Override
    public void addResultFact(ResultFact fact) {
        if (fact instanceof MuscleAlignmentValidationResultFact) {
            facts.add((MuscleAlignmentValidationResultFact)fact);
        }
    }

    /**
     * @author Frank Lee
     *         Created on 12/11/14 3:46 PM
     */
    public static class MuscleAlignmentValidationResultFact extends ResultFact {
        public static final String SEQUENCE_COLUMN_NAME = "Sequence";
        public static final String PASS_COLUMN_NAME = "Pass";
        public static final String SIMILARITY_COLUMN_NAME = "Similarity (%)";
        public static final String ALIGNMENT_COLUMN_NAME = "Alignment";
        public static final String NOTES_COLUMN_NAME = "Notes";

        private ResultColumn<LinkResultColumn.LinkBox> sequenceColumn;
        private ResultColumn<Boolean> passColumn;
        private ResultColumn<Double> similarityColumn;
        private ResultColumn<LinkResultColumn.LinkBox> alignmentColumn;
        private ResultColumn<String> notesColumn;

        PluginDocument sequenceReversedDocument;

        public MuscleAlignmentValidationResultFact(String name,
                                                   String sequenceName,
                                                   List<URN> sequenceLinks,
                                                   boolean pass,
                                                   double similarity,
                                                   String alignmentName,
                                                   List<URN> alignmentLinks,
                                                   String notes,
                                                   PluginDocument sequenceReversedDocument) {
            super(name);

            initColumns(sequenceName, sequenceLinks, pass, similarity, alignmentName, alignmentLinks, notes);

            setSequenceReversedDocument(sequenceReversedDocument);
        }

        @Override
        public List<ResultColumn> getColumns() {
            return Arrays.<ResultColumn>asList(
                    sequenceColumn,
                    passColumn,
                    similarityColumn,
                    alignmentColumn,
                    notesColumn
            );
        }

        @Override
        @SuppressWarnings("unchecked")
        public void addColumn(ResultColumn column) {
            String columnName = column.getName();

            if (columnName.equals(SEQUENCE_COLUMN_NAME)) {
                sequenceColumn = column;
            } else if (columnName.equals(PASS_COLUMN_NAME)) {
                similarityColumn = column;
            } else if (columnName.equals(SIMILARITY_COLUMN_NAME)) {
                similarityColumn = column;
            } else if (columnName.equals(ALIGNMENT_COLUMN_NAME)) {
                alignmentColumn = column;
            } else if (columnName.equals(NOTES_COLUMN_NAME)) {
                notesColumn = column;
            }
        }

        @Override
        public void setPass(boolean pass) {
            passColumn.setData(pass);
        }
        @Override
        public boolean getPass() {
            return passColumn.getData();
        }

        public void setSequenceName(String sequenceName) {
            sequenceColumn.getData().setLabel(sequenceName);
        }
        public String getSequenceName() {
            return sequenceColumn.getData().getLabel();
        }

        public void setSequenceLinks(List<URN> sequenceLinks) {
            sequenceColumn.getData().setLinks(sequenceLinks);
        }
        public List<URN> getSequenceLinks() {
            return sequenceColumn.getData().getLinks();
        }

        public void setSimilarity(double similarity) {
            similarityColumn.setData(similarity);
        }
        public double getSimilarity() {
            return similarityColumn.getData();
        }

        public void setAlignmentName(String alignmentName) {
            alignmentColumn.getData().setLabel(alignmentName);
        }
        public String getAlignmentName() {
            return alignmentColumn.getData().getLabel();
        }

        public void setAlignmentLinks(List<URN> alignmentLinks) {
            alignmentColumn.getData().setLinks(alignmentLinks);
        }
        public List<URN> getAlignmentLinks() {
            return alignmentColumn.getData().getLinks();
        }

        public void setNotes(String notes) {
            notesColumn.setData(notes);
        }
        public String getNotes() {
            return notesColumn.getData();
        }

        public void setSequenceReversedDocument(PluginDocument sequenceReversedDocument) {
            this.sequenceReversedDocument = sequenceReversedDocument;
        }
        public PluginDocument getSequenceReversedDocument() {
            return sequenceReversedDocument;
        }

        private void initColumns(String sequenceName, List<URN> sequenceLinks, boolean pass, double similarity, String alignmentName, List<URN> alignmentLinks, String notes) {
            sequenceColumn = new LinkResultColumn(SEQUENCE_COLUMN_NAME);
            passColumn = new BooleanResultColumn(PASS_COLUMN_NAME);
            similarityColumn = new DoubleResultColumn(SIMILARITY_COLUMN_NAME);
            alignmentColumn = new LinkResultColumn(ALIGNMENT_COLUMN_NAME);
            notesColumn = new StringResultColumn(NOTES_COLUMN_NAME);

            setSequenceName(sequenceName);
            setSequenceLinks(sequenceLinks);
            setPass(pass);
            setSimilarity(similarity);
            setAlignmentName(alignmentName);
            setAlignmentLinks(alignmentLinks);
            setNotes(notes);
        }
    }
}