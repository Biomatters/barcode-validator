package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.validation.assembly.CAP3Options;
import com.biomatters.plugins.barcoding.validator.validation.assembly.CAP3Runner;
import com.biomatters.plugins.barcoding.validator.validation.consensus.ConsensusUtilities;
import com.biomatters.plugins.barcoding.validator.validation.trimming.PrimerTrimmingOptions;
import com.biomatters.plugins.barcoding.validator.validation.trimming.SequenceTrimmer;
import com.biomatters.plugins.barcoding.validator.validation.trimming.TrimmingOptions;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Matthew Cheung
 *         Created on 24/10/14 3:57 PM
 */
public class Pipeline {
    public static void runValidationPipeline(NucleotideSequenceDocument barcode,
                                             List<NucleotideGraphSequenceDocument> traces,
                                             TrimmingOptions trimmingOptions,
                                             CAP3Options CAP3Options,
                                             Map<String, ValidationOptions> validationOptions,
                                             ValidationCallback callback,
                                             ProgressListener progressListener) throws DocumentOperationException {
        CompositeProgressListener stepsProgress = new CompositeProgressListener(progressListener, 3);

        stepsProgress.beginSubtask("Trimming traces...");

        CompositeProgressListener trimmingProgress = new CompositeProgressListener(stepsProgress, 2);
        trimmingProgress.beginSubtask();
        List<NucleotideGraphSequenceDocument> trimmedTraces = trimTraces(traces, trimmingOptions, true, trimmingProgress);
        trimmingProgress.beginSubtask();
        trimmedTraces = callback.addTrimmedTraces(trimmedTraces, trimmingProgress);

        stepsProgress.beginSubtask("Assembling traces...");

        CompositeProgressListener assembleTracesProgress = new CompositeProgressListener(stepsProgress, 3);
        assembleTracesProgress.beginSubtask();
        List<SequenceAlignmentDocument> contigs = assembleTraces(trimmedTraces, CAP3Options, barcode.getName(), assembleTracesProgress);
        assembleTracesProgress.beginSubtask();
        if (!contigs.isEmpty()) {
            CompositeProgressListener progressForAddingAssembly = new CompositeProgressListener(assembleTracesProgress, contigs.size());

            for (SequenceAlignmentDocument contig : contigs) {
                progressForAddingAssembly.beginSubtask();

                callback.addAssembly(contig, progressForAddingAssembly);
            }
        }
        assembleTracesProgress.beginSubtask();
        List<NucleotideGraphSequenceDocument> consensusSequences = new ArrayList<NucleotideGraphSequenceDocument>();
        if (!contigs.isEmpty()) {
            CompositeProgressListener progressForEachContig = new CompositeProgressListener(assembleTracesProgress, contigs.size());

            for (SequenceAlignmentDocument contig : contigs) {
                progressForEachContig.beginSubtask();

                CompositeProgressListener progressForEachConsensusStep = new CompositeProgressListener(progressForEachContig, 2);

                progressForEachConsensusStep.beginSubtask();

                NucleotideGraphSequenceDocument consensus = ConsensusUtilities.getConsensus(contig);
                List<NucleotideGraphSequenceDocument> resultConsensusList = trimTraces(Collections.singletonList(consensus), trimmingOptions, true, progressForEachConsensusStep);
                assert resultConsensusList.size() == 1;
                NucleotideGraphSequenceDocument retConsus = resultConsensusList.get(0);

                progressForEachConsensusStep.beginSubtask();

                callback.addConsensus(retConsus, progressForEachConsensusStep);
                consensusSequences.add(retConsus);
            }
        }

        stepsProgress.beginSubtask("Validating trimmed traces and the generated consensus...");

        List<Validation> validations = Validation.getValidations();
        List<SingleSequenceValidation> singleSequenceValidations = new ArrayList<SingleSequenceValidation>();
        List<SequenceCompareValidation> sequenceCompareValidations = new ArrayList<SequenceCompareValidation>();
        for (Validation validation : validations) {
            if (validation instanceof SingleSequenceValidation) {
                singleSequenceValidations.add((SingleSequenceValidation)validation);
            } else if (validation instanceof SequenceCompareValidation) {
                sequenceCompareValidations.add((SequenceCompareValidation)validation);
            } else {
                throw new DocumentOperationException("Unsupported validation procedure: " + validation.getClass().getSimpleName());
            }
        }
        CompositeProgressListener validationProgress = new CompositeProgressListener(stepsProgress, 4);
        validationProgress.beginSubtask("Validating trimmed traces");
        addValidationResultsToCallback(
                callback,
                runValidationTasks(
                        singleSequenceValidations,
                        createSingleSequenceValidationRunner(trimmedTraces),
                        validationOptions,
                        validationProgress
                ),
                validationProgress
        );
        validationProgress.beginSubtask("Validating trimmed traces with barcode");
        addValidationResultsToCallback(
                callback,
                runValidationTasks(
                        sequenceCompareValidations,
                        createSequenceCompareValidationRunner(trimmedTraces, barcode),
                        validationOptions,
                        validationProgress
                ),
                validationProgress
        );
        validationProgress.beginSubtask("Validating the generated consensus");
        addValidationResultsToCallback(
                callback,
                runValidationTasks(
                        singleSequenceValidations,
                        createSingleSequenceValidationRunner(consensusSequences),
                        validationOptions,
                        validationProgress
                ),
                validationProgress
        );
        validationProgress.beginSubtask("Validating the generated consensus with barcode");
        addValidationResultsToCallback(
                callback,
                runValidationTasks(
                    sequenceCompareValidations,
                    createSequenceCompareValidationRunner(consensusSequences, barcode),
                    validationOptions,
                    validationProgress
                ),
                validationProgress
        );
    }

    private static ValidationRunner<SingleSequenceValidation> createSingleSequenceValidationRunner(final List<NucleotideGraphSequenceDocument> sequences) {
        return new ValidationRunner<SingleSequenceValidation>() {
            @Override
            List<ValidationResult> run(SingleSequenceValidation validation, ValidationOptions options) throws DocumentOperationException {
                List<ValidationResult> validationResults = new ArrayList<ValidationResult>();
                for (NucleotideGraphSequenceDocument sequence : sequences) {
                    validationResults.add(validation.validate(sequence, options));
                }

                return validationResults;
            }
        };
    }

    private static ValidationRunner<SequenceCompareValidation> createSequenceCompareValidationRunner(final List<NucleotideGraphSequenceDocument> sequences, final NucleotideSequenceDocument referenceSequence) {
        return new ValidationRunner<SequenceCompareValidation>() {
            @Override
            List<ValidationResult> run(SequenceCompareValidation validation, ValidationOptions options) throws DocumentOperationException {
                List<ValidationResult> validationResults = new ArrayList<ValidationResult>();

                for (NucleotideSequenceDocument sequence : sequences) {
                     validationResults.add(validation.validate(sequence, referenceSequence, options));
                }

                return validationResults;
            }
        };
    }

    private static void addValidationResultsToCallback(ValidationCallback callback, List<ValidationRun> runs, ProgressListener progressListener) throws DocumentOperationException {
        CompositeProgressListener addTraceValidationResultsProgress = new CompositeProgressListener(progressListener, runs.size());

        for (ValidationRun run : runs) {
            addTraceValidationResultsProgress.beginSubtask();

            callback.addValidationResults(run.options, run.results, addTraceValidationResultsProgress);
        }
    }

    /**
     * Runs a list of {@link com.biomatters.plugins.barcoding.validator.validation.Validation}s using the specified
     * {@link ValidationRunner}
     * and {@link ValidationOptions}
     *
     * @param options Map of {@link ValidationOptions#getIdentifier()} to {@link ValidationOptions} of tasks to run.
     * @param progressListener to report progress to
     * @return {@link ValidationRun}s that describe the runs of the supplied {@link Validation}s.
     *
     * @throws com.biomatters.geneious.publicapi.plugin.DocumentOperationException if a problem occurs during validation
     */
    private static <T extends Validation> List<ValidationRun> runValidationTasks(List<T> validationTasks,
                                                                                 ValidationRunner<T> runner,
                                                                                 Map<String, ValidationOptions> options,
                                                                                 ProgressListener progressListener) throws DocumentOperationException {
        List<ValidationRun> result = new ArrayList<ValidationRun>();
        CompositeProgressListener validationProgress = new CompositeProgressListener(progressListener, validationTasks.size());

        for (T validation : validationTasks) {
            validationProgress.beginSubtask();

            ValidationOptions templateOptionsForValidation = validation.getOptions();

            ValidationOptions optionsToRunWith = options.get(templateOptionsForValidation.getIdentifier());

            if (optionsToRunWith == null) {
                throw new DocumentOperationException("Could not find validation module for identifier: '" + templateOptionsForValidation.getIdentifier() + "'.");
            }

            result.add(new ValidationRun(optionsToRunWith, runner.run(validation, optionsToRunWith)));
        }

        return result;
    }

    private static List<SequenceAlignmentDocument> assembleTraces(List<NucleotideGraphSequenceDocument> traces,
                                                                  CAP3Options options,
                                                                  String contigName,
                                                                  ProgressListener progressListener) throws DocumentOperationException {
        return CAP3Runner.assemble(traces, options.getExecutablePath(), options.getMinOverlapLength(), options.getMinOverlapIdentity(), contigName, progressListener);
    }

    private static List<NucleotideGraphSequenceDocument> trimTraces(List<NucleotideGraphSequenceDocument> traces,
                                                                    TrimmingOptions options,
                                                                    boolean trimByAddingAnnotation,
                                                                    ProgressListener progressListener) throws DocumentOperationException {
        List<NucleotideGraphSequenceDocument> trimmedTraces = new ArrayList<NucleotideGraphSequenceDocument>();
        PrimerTrimmingOptions primerTrimmingOptions = options.getPrimerTrimmingOptions();
        CompositeProgressListener trimmingProgress = new CompositeProgressListener(progressListener, traces.size());

        for (NucleotideGraphSequenceDocument trace : traces) {
            trimmingProgress.beginSubtask("Trimming " + trace.getName());

            trimmedTraces.add(SequenceTrimmer.trimSequenceByQualityAndPrimers(
                    trace,
                    options.getQualityTrimmingOptions().getErrorProbabilityLimit(),
                    primerTrimmingOptions.getPrimers(),
                    (float)primerTrimmingOptions.getGapOptionPenalty(),
                    (float)primerTrimmingOptions.getGapExtensionPenalty(),
                    primerTrimmingOptions.getScores(),
                    primerTrimmingOptions.getMaximumMismatches(),
                    primerTrimmingOptions.getMinimumMatchLength(),
                    trimByAddingAnnotation)
            );
        }

        return trimmedTraces;
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
        abstract List<ValidationResult> run(T validation, ValidationOptions options) throws DocumentOperationException;
    }

    private static class ValidationRun {
        private ValidationOptions options;
        private List<ValidationResult> results;

        private ValidationRun(ValidationOptions options, List<ValidationResult> results) {
            this.options = options;
            this.results = results;
        }
    }
}