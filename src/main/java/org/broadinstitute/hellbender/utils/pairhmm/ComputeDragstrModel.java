package org.broadinstitute.hellbender.utils.pairhmm;

import htsjdk.tribble.Feature;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.broadinstitute.hellbender.engine.AlignmentContext;
import org.broadinstitute.hellbender.engine.FeatureContext;
import org.broadinstitute.hellbender.engine.FeatureInput;
import org.broadinstitute.hellbender.engine.ReferenceContext;
import org.broadinstitute.hellbender.engine.spark.LocusWalkerContext;
import org.broadinstitute.hellbender.engine.spark.LocusWalkerSpark;
import org.broadinstitute.hellbender.utils.SimpleInterval;
import org.broadinstitute.hellbender.utils.Utils;
import org.broadinstitute.hellbender.utils.param.ParamUtils;
import org.broadinstitute.hellbender.utils.pileup.ReadPileup;
import org.broadinstitute.hellbender.utils.read.GATKRead;
import org.json4s.FileInput;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ComputeDragstrModel extends FeatureWaølkerSpark {

    @Argument(fullName= "str-file", doc = "file indicating where strs are present and their period and repeat number")
    private FeatureInput<STRInfo> strs;


    @ArgumentCollection
    private final ProcessingArguments processingArguments = new ProcessingArguments();

    @Override
    protected void processAlignments(final JavaRDD<LocusWalkerContext> rdd, JavaSparkContext ctx) {
        rdd.flatMap(processor(processingArguments));
    }

    protected Object processPileup(final LocusWalkerContext context) {
        final AlignmentContext alignment = context.getAlignmentContext();
        final ReadPileup pileup = context.getAlignmentContext().getBasePileup();
        if (qualifies(ReadPileup)) {
            return null;
        }
    }

    @Override
    protected List<SimpleInterval> editIntervals(final List<SimpleInterval> rawIntervals) {
        return rawIntervals;
    }


    private static class Processor implements FlatMapFunction<LocusWalkerContext, ProcessedLocus>, Serializable {

        private static final long serialVersionUID = 1L;

        private final ProcessingArguments arguments;

        private Processor(final ProcessingArguments processingArguments) {
            this.arguments = Objects.requireNonNull(processingArguments);
        }

        @Override
        public Iterator<ProcessedLocus> call(final LocusWalkerContext context) throws Exception {
            if (qualifies(context)) {
                return Collections.singletonList(processLocus(context)).iterator();
            } else {
                return Collections.emptyIterator();
            }
        }

        public boolean qualifies(final LocusWalkerContext context) {
            final ReadPileup pileup = context.getAlignmentContext().getBasePileup();
            if (pileup.isEmpty()) {
                return false;
            } else {
                for (final GATKRead read : pileup.getReads()) {
                    final int mq = read.getMappingQuality();
                    if (mq < arguments.minMQ) {
                       return false;
                    }
                }
                return true;
            }
        }

        public ProcessedLocus processLocus(final LocusWalkerContext context) {
            final ReferenceContext ref = context.getReferenceContext();
            final FeatureContext feature = context.getFeatureContext();
            final FeatureContext
            return null;
        }
    }


    public static final class ProcessingArguments implements Serializable {

        private static long serialVersionUID = 1L;

        @Argument(fullName = "min-mq", optional = true)
        public int minMQ = 20;

        @Argument(fullName = "bq-threshold", optional = true)
        public int bQThreshold = 10;

        @Argument(fullName = "bq-exceptions-allowed", optional = true)
        public int allowedBQExceptions = 2;

        @Argument(fullName = "del-bq", optional = true)
        public int delBQ = 40;

        @Argument(fullName = "padding", optional = true)
        public int padding = 5;
    }

    public static class ProcessedLocus {

    }

    private class STRInfo implements Feature {

        private final String chr;
        private final int start;
        private final int period;
        private final int repeatCount;

        public STRInfo(final String chr, final int start, final int period, final int repeatCount) {
            this.chr = Utils.nonNull(chr);
            this.start = ParamUtils.isPositive(start, "start position");
            this.period = ParamUtils.isPositive(period, "period length");
            this.repeatCount = ParamUtils.isPositive(repeatCount, "repeat count");
        }

        @Override
        public String getContig() {
            return chr;
        }

        @Override
        public int getStart() {
            return start;
        }

        @Override
        public int getEnd() {
            return start + period * repeatCount - 1;
        }
    }
}
