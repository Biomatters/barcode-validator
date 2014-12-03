package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Gen Li
 *         Created on 5/09/14 9:54 AM
 */
public class BOLDTraceListMapper extends BarcodeToTraceMapper {
    private static final String PROCESS_ID_COLUMN_NAME = "PROCESSID";
    private static final String TRACE_FILE_COLUMN_NAME = "TRACEFILE";

    private static final String BOLD_BARCODE_DESCRIPTION_SEPARATOR = "\\|";

    private static final int INDEX_OF_PROCESS_ID_IN_BOLD_BARCODE_DESCRIPTION = 0;

    private String boldTraceInfoFilePath;
    private boolean hasHeaderRow;
    private int userSelectedProcessIdIndex;
    private int userSelectedTracefileIndex;

    public BOLDTraceListMapper(String boldTraceInfoFilePath, boolean hasHeaderRow, int processIdIndex, int traceIndex) {
        setBoldTraceInfoFilePath(boldTraceInfoFilePath);
        this.hasHeaderRow = hasHeaderRow;
        this.userSelectedProcessIdIndex = userSelectedProcessIdIndex;
        this.userSelectedTracefileIndex = userSelectedTracefileIndex;
    }

    /**
     *
     * @param barcodes Barcodes.
     * @param traces Traces.
     * @return Map from the supplied barcodes to the supplied traces.
     * @throws com.biomatters.geneious.publicapi.plugin.DocumentOperationException If an error occurs during the mapping.
     */
    @Override
    public Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> map(Collection<AnnotatedPluginDocument> barcodes, Collection<AnnotatedPluginDocument> traces)
            throws DocumentOperationException {
        if (barcodes == null) {
            throw new IllegalArgumentException("barcodes cannot be null.");
        }

        if (barcodes.contains(null)) {
            throw new IllegalArgumentException("Barcode documents cannot be null.");
        }

        if (traces == null) {
            throw new IllegalArgumentException("traces cannot be null.");
        }

        if (traces.contains(null)) {
            throw new IllegalArgumentException("Trace documents cannot be null.");
        }

        return map(getBarcodesToProcessIDsMap(barcodes), getProcessIDsToTracesMap(traces), traces);
    }

    public void setBoldTraceInfoFilePath(String boldTraceInfoFilePath) {
        this.boldTraceInfoFilePath = boldTraceInfoFilePath;
    }

    public String getBoldTraceInfoFilePath() {
        return boldTraceInfoFilePath;
    }

    /**
     * @param barcodesToProcessIDs Map from barcodes to process IDs.
     * @param processIDsToTraces Map from process IDs to traces.
     * @param allTraces
     * @return Map from the supplied barcodes to the supplied traces.
     * @throws DocumentOperationException If there exists a trace that is not associated with a barcode.
     */
    private Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> map(Map<AnnotatedPluginDocument, String> barcodesToProcessIDs,
                                                                           Multimap<String, AnnotatedPluginDocument> processIDsToTraces,
                                                                           Collection<AnnotatedPluginDocument> allTraces) throws DocumentOperationException {
        Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> barcodesToTraces = ArrayListMultimap.create();

        for (Map.Entry<AnnotatedPluginDocument, String> barcodeToProcessID : barcodesToProcessIDs.entrySet()) {
            barcodesToTraces.putAll(barcodeToProcessID.getKey(), processIDsToTraces.get(barcodeToProcessID.getValue()));
        }

        Collection<AnnotatedPluginDocument> tracesWithoutAnAssociatedBarcode = getTracesWithoutAnAssociatedBarcode(allTraces, barcodesToTraces.values());

        if (!tracesWithoutAnAssociatedBarcode.isEmpty()) {
            throw new DocumentOperationException("Unmapped traces: " + StringUtilities.join(", ", tracesWithoutAnAssociatedBarcode));
        }

        return barcodesToTraces;
    }

    /**
     * Returns a map of trace name to process ID via the trace list file associated with the instance.
     *
     * @return A map of process ids to trace file names via the trace list file associated with the instance.
     * @throws DocumentOperationException
     * @throws IOException
     */
    private Multimap<String, AnnotatedPluginDocument> getProcessIDsToTracesMap(Collection<AnnotatedPluginDocument> traces) throws DocumentOperationException {
        Multimap<String, AnnotatedPluginDocument> processIDsToTraces = ArrayListMultimap.create();

        Multimap<String, AnnotatedPluginDocument> traceNamesToTraces = getTraceNamesToTracesMap(traces);
        List<List<String>> contents = importTraceListFileContent();
        int traceFileRowIndex = hasHeaderRow ? getTraceFileIndex(contents) : traceIndex;
        int processIDRowIndex = hasHeaderRow ? getProcessIDIndex(contents) : processIdIndex;
        int indexOfFirstRowWithContents = hasHeaderRow ? 1 : 0;
        for (int i = indexOfFirstRowWithContents; i < contents.size(); i++) {
            List<String> row = contents.get(i);
            
            throwMappingExceptionIfIndexOutOfBounds(i, processIDRowIndex, row);
            throwMappingExceptionIfIndexOutOfBounds(i, traceFileRowIndex, row);
            
            processIDsToTraces.putAll(row.get(processIDRowIndex), traceNamesToTraces.get(parseTraceFileName(row.get(traceFileRowIndex))));
        }

        return processIDsToTraces;
    }

    /**
     * Returns the contents of the trace info file associated with the instance.
     *
     * @return Contents of the trace info file as a List of List of Strings where the sub Lists represent rows and the
     *         Strings represent cells.
     */
    private List<List<String>> importTraceListFileContent() throws DocumentOperationException {
        if (boldTraceInfoFilePath == null) {
            throw new IllegalStateException("boldTraceInfoFilePath cannot be null.");
        }

        File file = new File(boldTraceInfoFilePath);

        if (!file.isFile()) {
            throw new DocumentOperationException(boldTraceInfoFilePath + " does not point to a file.");
        }

        List<List<String>> result = new ArrayList<List<String>>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null && !line.isEmpty()) {
                result.add(Arrays.asList(line.split("\t")));

                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new DocumentOperationException("Error importing data from");
        }

        validateTraceList(result);

        return result;
    }

    /**
     * Returns a map from the process IDs of the supplied barcodes to the supplied barcodes.
     *
     * @param barcodes The supplied barcodes.
     * @return Map from the process ids of the supplied barcodes to the supplied barcodes.
     */
    private static Map<AnnotatedPluginDocument, String> getBarcodesToProcessIDsMap(Collection<AnnotatedPluginDocument> barcodes) throws DocumentOperationException {
        Map<AnnotatedPluginDocument, String> barcodesToProcessIDs = new HashMap<AnnotatedPluginDocument, String>();

        for (AnnotatedPluginDocument barcode : barcodes) {
            barcodesToProcessIDs.put(barcode, barcode.getName().split(BOLD_BARCODE_DESCRIPTION_SEPARATOR)[INDEX_OF_PROCESS_ID_IN_BOLD_BARCODE_DESCRIPTION]);
        }

        return barcodesToProcessIDs;
    }

    private static Multimap<String, AnnotatedPluginDocument> getTraceNamesToTracesMap(Collection<AnnotatedPluginDocument> traces) throws DocumentOperationException {
        Multimap<String, AnnotatedPluginDocument> traceNamesToTraces = ArrayListMultimap.create();

        for (AnnotatedPluginDocument trace : traces) {
            traceNamesToTraces.put(trace.getName(), trace);
        }

        return traceNamesToTraces;
    }

    /**
     * Validates the number of rows of a trace list being greater than or equal to 2 and the equality of the lengths of
     * each and every pair of rows.
     *
     * @param contents Trace list contents.
     * @throws DocumentOperationException if
     */
    private static void validateTraceList(List<List<String>> contents) throws DocumentOperationException {
        if (contents.size() < 2) {
            throw new DocumentOperationException("Trace lists cannot have less than 2 rows.");
        }

        if (!validateTraceListRowsAreAllOfEqualLength(contents)) {
            throw new DocumentOperationException("Rows of a trace list must be of equal length.");
        }
    }

    /**
     * @param contents Trace list contents.
     * @return True if the the lengths of each and every row of the trace list are equal, and false if not.
     */
    private static boolean validateTraceListRowsAreAllOfEqualLength(List<List<String>> contents) {
        for (int i = 0; i < contents.size() - 1; i++) {
            if (contents.get(i).size() != contents.get(i + 1).size()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parses the name of a trace file from a trace list file to the actual name of the trace file.  Traces from BOLD
     * can be named NNNN/traceName+dddddd.ab1 rather than just traceName
     *
     * @param traceFileName The name of a trace file from a trace list file.
     * @return The actual name of the trace file.
     */
    private static String parseTraceFileName(String traceFileName) {
        traceFileName = traceFileName.substring(traceFileName.lastIndexOf("/") + 1);

        int indexOfPlus = traceFileName.indexOf("+");
        return traceFileName.substring(0, indexOfPlus) + traceFileName.substring(traceFileName.indexOf(".", indexOfPlus + 1));
    }

    /**
     * Returns the index of the process ID column of a trace list file.
     *
     * @param contents Trace list contents.
     * @return Index of the process ID column.
     * @throws DocumentOperationException If no or more than 1 process ID column was found.
     */
    private static int getProcessIDIndex(List<List<String>> contents) throws DocumentOperationException {
        return getRowIndex(contents, PROCESS_ID_COLUMN_NAME);
    }

    /**
     * Returns the index of the trace file column of a trace list file.
     *
     * @param contents Trace list contents.
     * @return Index of the trace file column.
     * @throws DocumentOperationException If no or more than 1 trace file column was found.
     */
    private static int getTraceFileIndex(List<List<String>> contents) throws DocumentOperationException {
        return getRowIndex(contents, TRACE_FILE_COLUMN_NAME);
    }

    /**
     * @param contents Trace list contents.
     * @return Index of the specified column.
     * @throws DocumentOperationException If no or more than 1 of the specified column was found.
     */
    private static int getRowIndex(List<List<String>> contents, String columnName) throws DocumentOperationException {
        List<String> header = contents.get(0);
        int index = header.indexOf(columnName);

        if (index == -1) {
            throw new DocumentOperationException("\"" + columnName + "\" column was not found.");
        }

        if (index != header.size() && header.subList(index + 1, header.size()).indexOf(columnName) != -1) {
            throw new DocumentOperationException("More than 1 '" + columnName + "' column was found.");
        }

        return index;
    }
    
    private static void throwMappingExceptionIfIndexOutOfBounds(int lineIndex, int index, Collection<String> row) throws DocumentOperationException {
        if(index >= row.size()) {
            throw new DocumentOperationException("Line " + (lineIndex+1) + ": did not have " +
                        NamePartOption.getLabelForPartNumber(index) + " element.");
        }
    }
}