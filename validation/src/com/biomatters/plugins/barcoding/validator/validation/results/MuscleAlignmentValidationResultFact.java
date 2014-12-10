package com.biomatters.plugins.barcoding.validator.validation.results;

import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.plugins.barcoding.validator.validation.results.*;
import org.jdom.Element;

import java.util.Arrays;
import java.util.List;

/**
 * MuscleAlignmentValidationResultFact is used for holding validation result of {@link com.biomatters.plugins.barcoding.validator.validation.MuscleAlignmentValidation}
 * @author Frank Lee
 *         Created on 22/11/14 9:57 AM
 */
public class MuscleAlignmentValidationResultFact extends ResultFact {
    public static final String PASS_COLUMN_NAME       = "Pass";
    public static final String SIMILARITY_COLUMN_NAME = "Similarity (%)";
    public static final String ALIGNMENT_COLUMN_NAME  = "Alignment";
    public static final String NOTES_COLUMN_NAME      = "Notes";

    private BooleanResultColumn passColumn;
    private DoubleResultColumn similarityColumn;
    private LinkResultColumn alignmentColumn;
    private StringResultColumn notesColumn;

    @SuppressWarnings("unused")
    public MuscleAlignmentValidationResultFact(Element element) throws XMLSerializationException {
        super(element);
    }

    public MuscleAlignmentValidationResultFact(boolean pass, double similarity, String alignmentName, List<URN> alignmentLinks, String notes) {
        super("Barcode Similarity Validation");
        initColumns(pass, similarity, alignmentName, alignmentLinks, notes);
    }

    @Override
    public List<ResultColumn> getColumns() {
        return Arrays.<ResultColumn>asList(
                passColumn,
                similarityColumn,
                alignmentColumn,
                notesColumn
        );
    }

    @Override
    public void addColumn(ResultColumn column) {
        String columnName = column.getName();

        if (columnName.equals(PASS_COLUMN_NAME)) {
            passColumn = (BooleanResultColumn) column;
        } else if (columnName.equals(SIMILARITY_COLUMN_NAME)) {
            similarityColumn = (DoubleResultColumn) column;
        } else if (columnName.equals(ALIGNMENT_COLUMN_NAME)) {
            alignmentColumn = (LinkResultColumn) column;
        } else if (columnName.equals(NOTES_COLUMN_NAME)) {
            notesColumn = (StringResultColumn) column;
        } else {
            System.out.println("Can not recognize column " + columnName);
        }
    }

    public void setPass(boolean pass) {
        passColumn.setData(pass);
    }

    @Override
    public boolean getPass() {
        return passColumn.getData();
    }

    public void setSimilarity(double similarity) {
        similarityColumn.setData(similarity);
    }

    public void setAlignmentName(String alignmentName) {
        alignmentColumn.getData().setLabel(alignmentName);
    }

    public void setAlignmentLinks(List<URN> alignmentLinks) {
        alignmentColumn.getData().setLinks(alignmentLinks);
    }

    public void addAlignmentDocument(PluginDocument pluginDocument) {
        alignmentColumn.getData().addPluginDocument(pluginDocument);
    }

    public void setNotes(String notes) {
        notesColumn.setData(notes);
    }

    private void initColumns(boolean pass, double similarity, String alignmentName, List<URN> alignmentLinks, String notes) {
        passColumn = new BooleanResultColumn(PASS_COLUMN_NAME);
        similarityColumn = new DoubleResultColumn(SIMILARITY_COLUMN_NAME);
        alignmentColumn = new LinkResultColumn(ALIGNMENT_COLUMN_NAME);
        notesColumn = new StringResultColumn(NOTES_COLUMN_NAME);

        setPass(pass);
        setSimilarity(similarity);
        setAlignmentName(alignmentName);
        setAlignmentLinks(alignmentLinks);
        setNotes(notes);
    }
}