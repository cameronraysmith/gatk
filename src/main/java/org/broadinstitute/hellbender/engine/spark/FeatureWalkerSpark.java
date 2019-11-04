package org.broadinstitute.hellbender.engine.spark;

import htsjdk.tribble.Feature;
import htsjdk.tribble.FeatureCodec;
import htsjdk.tribble.FeatureCodecHeader;
import htsjdk.tribble.readers.AsciiLineReader;
import htsjdk.tribble.readers.AsciiLineReaderIterator;
import htsjdk.tribble.readers.LineIterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.broadinstitute.hellbender.utils.SerializableFunction;
import org.broadinstitute.hellbender.utils.codecs.table.TableCodec;
import org.disq_bio.disq.impl.file.HadoopFileSystemWrapper;

import java.util.Iterator;
import java.util.function.Function;

public abstract class FeatureWalkerSpark<F extends Feature> extends GATKSparkTool {

    protected abstract String getDrivingFeaturePath();

    protected abstract SerializableFunction<String, FeatureCodec<F, Iterator<String>>> pathToCodec();

    @Override
    protected void runTool(final JavaSparkContext ctx) {
        TableCodec x;
        final JavaRDD<F> drivingFeatures = getDrivingFeatures(ctx);
        referenceFileName = addReferenceFilesForSpark(ctx, referenceArguments.getReferenceFileName());
        processVariants(getVariants(ctx), ctx);
    }

    protected abstract JavaRDD<F> getDrivingFeatures(final JavaSparkContext ctx) {
        final String path = getDrivingFeaturePath();
        final SerializableFunction<String, FeatureCodec<F, Iterator<String>>> codecConstructor = pathToCodec();
        final FeatureCodec<F, Iterator<String>> codec = codecConstructor.apply(path);
        final Configuration conf = ctx.hadoopConfiguration();
        final FeatureCodecHeader header = codec.readHeader(new AsciiLineReaderIterator(AsciiLineReader.from(new HadoopFileSystemWrapper().open(conf, path))));
        final Broadcast<FeatureCodecHeader> headerBc = ctx.broadcast(header);
        ctx.textFile(path)
                .mapPartitions(it -> {
                    final FeatureCodec<F, Iterator<String>> codec = codecConstructor.apply(path);
                    codec.decode(it);
                });
        ctx.textFile()
        ctx.binaryFiles()
        ctx.newAPIHadoopFile()
        V
    }

}
