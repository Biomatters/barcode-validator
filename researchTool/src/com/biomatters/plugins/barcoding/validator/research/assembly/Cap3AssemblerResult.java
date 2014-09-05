package com.biomatters.plugins.barcoding.validator.research.assembly;

/**
 * Holds paths of {@value Cap3Assembler#CAP3_ASSEMBLER_RESULT_FILE_EXTENSION} and
 * {@value Cap3Assembler#CAP3_ASSEMBLER_UNUSED_READS_FILE_EXTENSION} CAP3 assembly output files.
 */
public class Cap3AssemblerResult {
    private String pathOfResultFile;
    private String pathOfUnusedReadsFile;

    public Cap3AssemblerResult(String pathOfResultFile, String pathOfUnusedReadsFile) {
        this.pathOfResultFile = pathOfResultFile;
        this.pathOfUnusedReadsFile = pathOfUnusedReadsFile;
    }

    public String getPathOfResultFile() {
        return pathOfResultFile;
    }

    public String getPathOfUnusedReadsFile() {
        return pathOfUnusedReadsFile;
    }
}
