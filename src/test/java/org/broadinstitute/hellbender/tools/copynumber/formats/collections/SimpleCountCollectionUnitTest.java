package org.broadinstitute.hellbender.tools.copynumber.formats.collections;

import com.google.common.collect.ImmutableList;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import org.broadinstitute.hellbender.GATKBaseTest;
import org.broadinstitute.hellbender.exceptions.UserException;
import org.broadinstitute.hellbender.tools.copynumber.formats.metadata.SampleLocatableMetadata;
import org.broadinstitute.hellbender.tools.copynumber.formats.metadata.SimpleSampleLocatableMetadata;
import org.broadinstitute.hellbender.tools.copynumber.formats.records.SimpleCount;
import org.broadinstitute.hellbender.utils.SimpleInterval;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public final class SimpleCountCollectionUnitTest extends GATKBaseTest {
    private static final File TEST_SUB_DIR = new File(toolsTestDir, "copynumber/formats/collections");

    private static final File INTEGER_COUNTS_TSV_FILE = new File(TEST_SUB_DIR,"simple-count-collection-integer-counts.tsv");
    private static final File INTEGER_COUNTS_HDF5_FILE = new File(TEST_SUB_DIR,"simple-count-collection-integer-counts.hdf5");
    private static final File INTEGER_COUNTS_TSV_GZ_FILE = new File(TEST_SUB_DIR,"simple-count-collection-integer-counts.tsv.gz");

    private static final String GCS_COLLECTIONS_TEST_SUBDIRECTORY = getGCPTestInputPath() +
            "org/broadinstitute/hellbender/tools/copynumber/formats/collections/";
    private static final String GCS_INTEGER_COUNTS_TSV_PATH = GCS_COLLECTIONS_TEST_SUBDIRECTORY +
            "simple-count-collection-integer-counts.counts.tsv";
    private static final String GCS_INTEGER_COUNTS_TSV_GZ_PATH = GCS_COLLECTIONS_TEST_SUBDIRECTORY +
            "simple-count-collection-integer-counts.counts.tsv.gz";

    private static final File INTEGER_COUNTS_MISSING_HEADER_TSV_FILE = new File(TEST_SUB_DIR,"simple-count-collection-integer-counts-missing-header.tsv");
    private static final File DOUBLE_COUNTS_TSV_FILE = new File(TEST_SUB_DIR, "simple-count-collection-double-counts.tsv");

    private static final SampleLocatableMetadata METADATA_EXPECTED = new SimpleSampleLocatableMetadata(
            "test-sample",
            new SAMSequenceDictionary(Collections.singletonList(
                    new SAMSequenceRecord("20", 200000))));

    private static final List<SimpleInterval> INTERVALS_EXPECTED = Arrays.asList(
            new SimpleInterval("20", 1,10000),
            new SimpleInterval("20", 10001,20000),
            new SimpleInterval("20", 20001, 30000),
            new SimpleInterval("20", 30001, 40000),
            new SimpleInterval("20", 40001, 50000),
            new SimpleInterval("20", 50001, 60000),
            new SimpleInterval("20", 60001, 70000),
            new SimpleInterval("20", 70001, 80000),
            new SimpleInterval("20", 80001, 90000),
            new SimpleInterval("20", 90001, 100000),
            new SimpleInterval("20", 100001, 110000),
            new SimpleInterval("20", 110001, 120000),
            new SimpleInterval("20", 120001, 130000),
            new SimpleInterval("20", 130001, 140000),
            new SimpleInterval("20", 140001, 150000),
            new SimpleInterval("20", 150001, 160000));
    private static final List<Integer> READ_COUNTS_EXPECTED =
            ImmutableList.of(0, 0, 0, 0, 0, 0, 94, 210, 22, 21, 24, 84, 247, 181, 27, 72);

    @DataProvider(name = "simpleCountCollectionReadIntegerCountsTestData")
    public Object[] getSimpleCountCollectionReadTestData() {
        return new Object[] {
                INTEGER_COUNTS_TSV_FILE,
                INTEGER_COUNTS_HDF5_FILE,
                INTEGER_COUNTS_TSV_GZ_FILE
        };
    }

    @Test(dataProvider = "simpleCountCollectionReadIntegerCountsTestData")
    public void testReadIntegerCounts(final File file) {
        final SimpleCountCollection scc = SimpleCountCollection.read(file);

        Assert.assertEquals(scc.getMetadata(), METADATA_EXPECTED);
        Assert.assertEquals(scc.getIntervals(), INTERVALS_EXPECTED);
        Assert.assertEquals(scc.getRecords().stream().map(SimpleCount::getCount).collect(Collectors.toList()), READ_COUNTS_EXPECTED);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testReadIntegerCountsMissingHeader() {
        SimpleCountCollection.read(INTEGER_COUNTS_MISSING_HEADER_TSV_FILE);
    }

    @Test(expectedExceptions = UserException.BadInput.class)
    public void testReadDoubleCounts() {
        SimpleCountCollection.read(DOUBLE_COUNTS_TSV_FILE);
    }

    @DataProvider(name = "simpleCountCollectionReadFromGCSIntegerCountsTestData")
    public Object[] getSimpleCountCollectionReadFromGCSIntegerCountsTestData() {
        return new Object[] {
                GCS_INTEGER_COUNTS_TSV_PATH,
                GCS_INTEGER_COUNTS_TSV_GZ_PATH
        };
    }

    @Test(dataProvider = "simpleCountCollectionReadFromGCSIntegerCountsTestData", groups = "bucket")
    public void testReadFromGCSIntegerCounts(final String path) {
        final SimpleCountCollection scc = SimpleCountCollection.readFromGCS(path);

        Assert.assertEquals(scc.getMetadata(), METADATA_EXPECTED);
        Assert.assertEquals(scc.getIntervals(), INTERVALS_EXPECTED);
        Assert.assertEquals(scc.getRecords().stream().map(SimpleCount::getCount).collect(Collectors.toList()), READ_COUNTS_EXPECTED);
    }

    //TODO: add tests for readSubset and readOverlappingSubsetFromGCS
//    @Test
//    public void testQuery() {
//        LoggingUtils.setLoggingLevel(Log.LogLevel.DEBUG);
//        final String localPath = "/home/slee/working/gatk/test_files/test.counts.tsv.gz";
//        final String bucketPath = "gs://broad-dsde-methods-slee/test.counts.tsv";
//
//        final File file = new File(localPath);
//        final FeatureDataSource<SimpleCount> localSource = new FeatureDataSource<>(localPath);
//        final FeatureDataSource<SimpleCount> bucketSource = new FeatureDataSource<>(bucketPath);
//
//        final SimpleInterval interval = new SimpleInterval("1", 1, 500000);
//
//        final SimpleCountCollection counts = SimpleCountCollection.read(file);
//
//        final BufferedLineReader localReader = new BufferedLineReader(BucketUtils.openFile(localPath));
//        final SAMFileHeader localHeader = new SAMTextHeaderCodec().decode(localReader, localPath);
//
//        final BufferedLineReader bucketReader = new BufferedLineReader(BucketUtils.openFile(bucketPath));
//        final SAMFileHeader bucketHeader = new SAMTextHeaderCodec().decode(bucketReader, bucketPath);
//
//        System.out.println(Stream.of(counts.getMetadata().toHeader().getSAMString(), localHeader.getSAMString(), bucketHeader.getSAMString()).distinct().count() == 1);
//
//        System.out.println(localSource.getHeader().toString());
//        System.out.println(bucketSource.getHeader().toString());
//
//        System.out.println(counts.getOverlapDetector().getOverlaps(interval).stream().sorted(counts.getComparator()).collect(Collectors.toList()));
//        System.out.println(Lists.newArrayList(localSource.query(interval)));
//        System.out.println(Lists.newArrayList(bucketSource.query(interval)));
//    }
}