package org.broadinstitute.hellbender.engine.spark;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.LocationAware;
import htsjdk.tribble.Feature;
import htsjdk.tribble.FeatureCodec;
import htsjdk.tribble.FeatureCodecHeader;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.input.FixedLengthBinaryInputFormat;
import org.disq_bio.disq.impl.file.HadoopFileSystemWrapper;
import org.tukaani.xz.UnsupportedOptionsException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public interface SparkFeatureCodec<F extends Feature & Serializable, H extends Serializable, S> extends FeatureCodec<F, S> {

    @Override
    default Feature decodeLoc(S s) throws IOException {
        return decode(s);
    }

    @Override
    F decode(S s) throws IOException;

    default Feature decodeLoc(H header, S s) throws IOException {
        return decodeLoc(header, s);
    }

    default F decode(H header, S s) throws IOException {
        return decode(s);
    }

    /**
     * This method seems to be used is someplaces for indexing... it smells and seems like
     * API design diarrhea to me... will keep unsupported until there is prove that I need to implement it here
     * and then I do whatever is needed to get it working properly.
     * @param s source.
     * @return never.
     * @throws UnsupportedOptionsException not supported as for now.
     */
    @Override
    default FeatureCodecHeader readHeader(S s) throws IOException {
        throw new UnsupportedOptionsException("");
    }

    /**
     *
     * @param s
     * @return null if this decoder does not deal with headers.
     * @throws IOException
     */
    H decodeHeader(S s) throws IOException;


    @Override
    Class<F> getFeatureType();

    @Override
    default S makeSourceFromStream(final InputStream bufferedInputStream) {
        throw new UnsupportedOperationException();
    }

    /**
     * Seems to me that this method should only be available if S is location aware... I think this and readHeader() : FeatureCodecHaeder
     * should made avaliable if S is location-aware. refactoring needed.
     * Say S is an IndexableSource. the it has a location component to it.
     */
    @Override
    default LocationAware makeIndexableSourceFromStream(InputStream inputStream) {
        throw new UnsupportedOperationException("");
    }

    @Override
    boolean isDone(S s);

    @Override
    default void close(S s) {
        CloserUtil.close(s);
    }

    @Override
    boolean canDecode(String path);

    JavaRDD<F> rdd(final JavaSparkContext ctx, final String path);
}
