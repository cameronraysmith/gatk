package org.broadinstitute.hellbender.engine.spark;

import htsjdk.tribble.Feature;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.input.PortableDataStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class TextSparkFeatureCodec<F extends Feature & Serializable, H extends Serializable, TextIterator > implements SparkFeatureCodec<F, H, S> {
    @Override
    public F decode(Text s) throws IOException {
        return null;
    }

    @Override
    public H decodeHeader(S s) throws IOException {
        return null;
    }

    @Override
    public Class<F> getFeatureType() {
        return null;
    }

    @Override
    public S makeSourceFromStream(InputStream bufferedInputStream) {
        return null;
    }

    @Override
    public boolean isDone(S s) {
        return false;
    }

    @Override
    public boolean canDecode(String path) {
        return false;
    }

    @Override
    public JavaRDD<F> rdd(JavaSparkContext ctx, String path) {
        ctx.textFile(path).
        ctx.binaryRecords()
        PortableDataStream x;
        x.open().
        ctx.binaryFiles()
    }
}
