package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.SequenceExtractionUtilities;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.validation.assembly.CAP3Options;
import com.biomatters.plugins.barcoding.validator.validation.assembly.CAP3Runner;
import com.biomatters.plugins.barcoding.validator.validation.trimming.ErrorProbabilityOptions;
import com.biomatters.plugins.barcoding.validator.validation.trimming.SequenceTrimmer;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Matthew Cheung
 *         Created on 24/10/14 3:57 PM
 */
public class Pipeline {
    public static void runValidationPipeline(NucleotideSequenceDocument barcode, List<NucleotideGraphSequenceDocument> traces,
                                      ErrorProbabilityOptions trimmingOptions, CAP3Options CAP3Options,
                                      Map<String, ValidationOptions> traceValidationOptions,
                                      Map<String, ValidationOptions> barcodeValidationOptions,
                                      ValidationCallback callback, ProgressListener progressListener) throws DocumentOperationException {
        CompositeProgressListener stepsProgress = new CompositeProgressListener(progressListener, 4);

        stepsProgress.beginSubtask("Trimming Traces");
        List<NucleotideGraphSequenceDocument> trimmedTraces = SequenceTrimmer.trimSequences(traces, trimmingOptions.getErrorProbabilityLimit());


        stepsProgress.beginSubtask("Validating Traces...");
        CompositeProgressListener traceValidationProgress = new CompositeProgressListener(stepsProgress, 3);

        traceValidationProgress.beginSubtask();
        List<ValidationRun> traceValidationResults = runValidationTasks(traceValidationOptions, traceValidationProgress,
                TraceValidation.getTraceValidations(), createTraceValiationRunnerForTraces(trimmedTraces)
        );

        traceValidationProgress.beginSubtask();
        addValidationResultsToCallback(callback, traceValidationResults, traceValidationProgress);

        trimmedTraces = callback.addTrimmedTraces(trimmedTraces, traceValidationProgress);


        stepsProgress.beginSubtask("Assembling...");
        CompositeProgressListener assembleTracesProgress = new CompositeProgressListener(stepsProgress, 3);
        assembleTracesProgress.beginSubtask();
        List<SequenceAlignmentDocument> contigs = assembleTraces(
                trimmedTraces, CAP3Options, barcode.getName(), assembleTracesProgress);
        assembleTracesProgress.beginSubtask();
        if(!contigs.isEmpty()) {
            CompositeProgressListener progressForAddingAssembly = new CompositeProgressListener(assembleTracesProgress, contigs.size());
            for (SequenceAlignmentDocument contig : contigs) {
                progressForAddingAssembly.beginSubtask();
                callback.addAssembly(contig, progressForAddingAssembly);
            }
        }
        assembleTracesProgress.beginSubtask();
        List<NucleotideSequenceDocument> consensusSequences = new ArrayList<NucleotideSequenceDocument>();
        if(!contigs.isEmpty()) {
            CompositeProgressListener progressForEachContig = new CompositeProgressListener(assembleTracesProgress, contigs.size());
            for (SequenceAlignmentDocument contig : contigs) {
                progressForEachContig.beginSubtask();
                NucleotideSequenceDocument consensus = getConsensus(contig);
                callback.addConsensus(consensus, progressForEachContig);
                consensusSequences.add(consensus);
            }
        }

        stepsProgress.beginSubtask("Validating Barcode Sequences...");
        CompositeProgressListener validateBarcodeProgress = new CompositeProgressListener(stepsProgress, 2);
        validateBarcodeProgress.beginSubtask();
        List<ValidationRun> barcodeValidationResults = runValidationTasks(barcodeValidationOptions, validateBarcodeProgress,
                BarcodeValidation.getBarcodeValidations(), createBarcodeValidationRunnerForInput(barcode, consensusSequences)
        );
        validateBarcodeProgress.beginSubtask();
        addValidationResultsToCallback(callback, barcodeValidationResults, validateBarcodeProgress);
    }

    private static ValidationRunner<TraceValidation> createTraceValiationRunnerForTraces(final List<NucleotideGraphSequenceDocument> trimmedTraces) {
        return new ValidationRunner<TraceValidation>() {
            @Override
            ValidationResult run(TraceValidation validation, ValidationOptions options) throws DocumentOperationException {
                return validation.validate(trimmedTraces, options);
            }
        };
    }

    private static ValidationRunner<BarcodeValidation> createBarcodeValidationRunnerForInput(
            final NucleotideSequenceDocument barcode, final List<NucleotideSequenceDocument> consensus) {
        return new ValidationRunner<BarcodeValidation>() {
            @Override
            ValidationResult run(BarcodeValidation validation, ValidationOptions options) throws DocumentOperationException {
                if(consensus.isEmpty()) {
                    return new ValidationResult(false, "Assembly failed.");
                } else if(consensus.size() > 1) {
                    return new ValidationResult(false, "Assembly produced more than one contig.");
                }

                if (validation instanceof BarcodeCompareValidation) {
                    return ((BarcodeCompareValidation) validation).validate(barcode, consensus.get(0), options);
                } else if(validation instanceof SingleBarcodeValidaton) {
                    throw new DocumentOperationException("Single barcode validation not implemented yet");
                } else {
                    throw new DocumentOperationException("Invalid validation options.");
                }
            }
        };
    }

    private static void addValidationResultsToCallback(ValidationCallback callback,
                                               List<ValidationRun> runs,
                                               ProgressListener progressListener) throws DocumentOperationException {
        CompositeProgressListener addTraceValidationResultsProgress = new CompositeProgressListener(progressListener, runs.size());

        for (ValidationRun run : runs) {
            addTraceValidationResultsProgress.beginSubtask();
            callback.addValidationResult(run.options, run.result, addTraceValidationResultsProgress);
        }
    }

    /**
     * Runs a list of {@link com.biomatters.plugins.barcoding.validator.validation.Validation}s using the specified
     * {@link ValidationRunner}
     * and {@link ValidationOptions}
     *
     * @param options Map of {@link ValidationOptions#getIdentifier()} to
     * {@link ValidationOptions} of tasks to run.
     * @param progressListener to report progress to
     * @return a list of {@link ValidationRun}
     * describing each run of the specified {@link Validation}s.
     *
     * @throws com.biomatters.geneious.publicapi.plugin.DocumentOperationException if a problem occurs during validation
     */
    private static <T extends Validation> List<ValidationRun> runValidationTasks(
            Map<String, ValidationOptions> options,
            ProgressListener progressListener,
            List<T> validationTasks,
            ValidationRunner<T> runner)
            throws DocumentOperationException {
        List<ValidationRun> result = new ArrayList<ValidationRun>();
        CompositeProgressListener validationProgress = new CompositeProgressListener(progressListener, validationTasks.size());

        for (T validation : validationTasks) {
            validationProgress.beginSubtask();
            ValidationOptions templateOptionsForValidation = validation.getOptions();
            ValidationOptions optionsToRunWith = options.get(templateOptionsForValidation.getIdentifier());
            if (optionsToRunWith == null) {
                throw new DocumentOperationException("Could not find validation module for identifier: '" +
                        templateOptionsForValidation.getIdentifier() + "'.");
            }
            result.add(new ValidationRun(optionsToRunWith, runner.run(validation, optionsToRunWith)));
        }

        return result;
    }

    private static List<SequenceAlignmentDocument> assembleTraces(List<NucleotideGraphSequenceDocument> traces,
                                                     CAP3Options options,
                                                     String contigName,
                                                     ProgressListener progressListener) throws DocumentOperationException {

        CompositeProgressListener assemblyProgress = new CompositeProgressListener(progressListener, 2);

        assemblyProgress.beginSubtask();
        List<SequenceAlignmentDocument> contigs = CAP3Runner.assemble(traces, options.getExecutablePath(), options.getMinOverlapLength(), options.getMinOverlapIdentity());

        assemblyProgress.beginSubtask();
        for (SequenceAlignmentDocument contig : contigs) {
            DocumentUtilities.getAnnotatedPluginDocumentThatContains(contig).setName(contigName);
            if (contig.canSetSequenceNames()) {
                contig.setSequenceName(0, contigName + " Consensus Sequence", true);
            }
        }

        return contigs;
    }

    private static NucleotideSequenceDocument getConsensus(SequenceAlignmentDocument contig) throws DocumentOperationException {
        SequenceDocument consensus = SequenceExtractionUtilities.removeGaps(
                contig.getSequence(contig.getContigReferenceSequenceIndex())
        );

        if (!(consensus instanceof NucleotideSequenceDocument)) {
            throw new DocumentOperationException(
                    "Assembly produced consensus of unexpected type.\n" +
                    "Expected: " + NucleotideSequenceDocument.class.getSimpleName() + "\n" +
                    "Actual: " + consensus.getClass().getSimpleName() + ".\n\n" +
                    "Please contact support@geneious.com with your input files and options."
            );
        }

        return (NucleotideSequenceDocument)consensus;
    }

    private static abstract class ValidationRunner<T extends Validation> {
        /**
         * Can be used to run a series of {@link Validation}s
         * on the same input data.
         * <br/><br/>
         * <strong>Note</strong>: Implementations are responsible for keeping track of their own input data and how
         * each {@link Validation} is run.
         *
         * @param validation The validation to run.
         * @param options The options to run the validation with.
         * @return a {@link ValidationResult}
         * @throws com.biomatters.geneious.publicapi.plugin.DocumentOperationException
         */
        abstract ValidationResult run(T validation, ValidationOptions options) throws DocumentOperationException;
    }

    private static class ValidationRun {
        private ValidationOptions options;
        private ValidationResult result;

        private ValidationRun(ValidationOptions options, ValidationResult result) {
            this.options = options;
            this.result = result;
        }
    }
}
