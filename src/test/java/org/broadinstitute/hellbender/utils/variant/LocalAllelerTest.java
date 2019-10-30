package org.broadinstitute.hellbender.utils.variant;

import com.google.common.primitives.Ints;
import htsjdk.variant.variantcontext.*;
import htsjdk.variant.vcf.VCFConstants;
import org.broadinstitute.hellbender.testutils.BaseTest;
import org.broadinstitute.hellbender.testutils.VariantContextTestUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LocalAllelerTest extends BaseTest {

    @DataProvider
    public Object[][] getTestCasesLAAandLGT(){
        final Allele ALT_AAT = Allele.create("AAT");
        final VariantContextBuilder vcbNoNonRef = getVcBuilder().alleles(Arrays.asList(Allele.REF_A, Allele.ALT_C, Allele.ALT_T, ALT_AAT));
        final VariantContextBuilder vcbWithNonRef = getVcBuilder().alleles(Arrays.asList(Allele.REF_A, Allele.ALT_C, Allele.ALT_T, ALT_AAT, Allele.NON_REF_ALLELE));

        // GT 0/2 LAA [2]   LGT 0/1
        Genotype het0_2 = makeGenotypeWithAlleles(Arrays.asList(Allele.REF_A, Allele.ALT_T));

        // GT 0/0 LAA []    LGT 0/0
        Genotype hom0_0 = makeGenotypeWithAlleles(Arrays.asList(Allele.REF_A, Allele.REF_A));

        // GT 1/2 LAA [1,2] LGT 1/2
        Genotype het1_2 = makeGenotypeWithAlleles(Arrays.asList(Allele.ALT_C, Allele.ALT_T));

        // GT 2/2 LAA [2]   LGT 1/1
        Genotype hom2_2 = makeGenotypeWithAlleles(Arrays.asList(Allele.ALT_T, Allele.ALT_T));

        // GT 1/3 LAA [1,3] LGT 1/2
        Genotype het1_3 = makeGenotypeWithAlleles(Arrays.asList(Allele.ALT_C, ALT_AAT));


        return new Object[][]{
                makeGenotypes(vcbNoNonRef, het0_2, Arrays.asList(2), "0/1"),
                makeGenotypes(vcbNoNonRef, hom0_0, Arrays.asList(), "0/0"),
                makeGenotypes(vcbNoNonRef, het1_2, Arrays.asList(1,2), "1/2"),
                makeGenotypes(vcbNoNonRef, hom2_2, Arrays.asList(2), "1/1"),
                makeGenotypes(vcbNoNonRef, het1_3, Arrays.asList(1,3), "1/2"),

                makeGenotypes(vcbWithNonRef, het0_2, Arrays.asList(2,4), "0/1"),
                makeGenotypes(vcbWithNonRef, hom0_0, Arrays.asList(4), "0/0"),
                makeGenotypes(vcbWithNonRef, het1_2, Arrays.asList(1,2,4), "1/2"),
                makeGenotypes(vcbWithNonRef, hom2_2, Arrays.asList(2,4), "1/1"),
                makeGenotypes(vcbWithNonRef, het1_3, Arrays.asList(1,3,4), "1/2")
        };
    }

    private VariantContextBuilder getVcBuilder() {
        return new VariantContextBuilder("handmade", "chr1", 100, 100, Collections.emptyList());
    }

    private Genotype makeGenotypeWithAlleles(List<Allele> alleles) {
        GenotypeBuilder gb = new GenotypeBuilder("sample");
        return gb.alleles(alleles).make();
    }

    private static Object[] makeGenotypes(VariantContextBuilder vcb, Genotype rootGenotype, List<Integer> LAA, String LGT){
        return makeGenotypes(vcb, rootGenotype, LAA, LGT, Collections.emptyList(), Collections.emptyList());
    }

    private static Object[] makeGenotypes(VariantContextBuilder vcb, Genotype rootGenotype, List<Integer> LAA, String LGT, List<Integer> AD, List<Integer> LAD) {
        GenotypeBuilder gb = new GenotypeBuilder(rootGenotype);
        if( ! AD.isEmpty()){
            gb.AD(Ints.toArray(AD));
        }

        Genotype newRoot = gb.make();

        gb.attribute(LocalAlleler.LAA, LAA)
                .attribute(LocalAlleler.LGT, LGT);

        if(!LAD.isEmpty()){
            gb.attribute(LocalAlleler.LAD, LAD);
        }

        return new Object[]{vcb.genotypes(newRoot).make(), newRoot, gb.make()};
    }

    @Test(dataProvider = "getTestCasesLAAandLGT")
    public void testAddLocalFields(VariantContext vc, Genotype original, Genotype expected) {
        Genotype actual = LocalAlleler.addLocalFields(original, vc);
        VariantContextTestUtils.assertGenotypesAreEqual(actual, expected);
    }

    @DataProvider
    public Object[][] getTestCasesLAAandLGTandAD(){
        final Allele ALT_AAT = Allele.create("AAT");
        final VariantContextBuilder vcbNoNonRef = getVcBuilder().alleles(Arrays.asList(Allele.REF_A, Allele.ALT_C, Allele.ALT_T, ALT_AAT));
        final VariantContextBuilder vcbWithNonRef = getVcBuilder().alleles(Arrays.asList(Allele.REF_A, Allele.ALT_C, Allele.ALT_T, ALT_AAT, Allele.NON_REF_ALLELE));

        // GT 0/2 LAA [2]   LGT 0/1
        Genotype het0_2 = makeGenotypeWithAlleles(Arrays.asList(Allele.REF_A, Allele.ALT_T));

        // GT 0/0 LAA []    LGT 0/0
        Genotype hom0_0 = makeGenotypeWithAlleles(Arrays.asList(Allele.REF_A, Allele.REF_A));

        // GT 1/2 LAA [1,2] LGT 1/2
        Genotype het1_2 = makeGenotypeWithAlleles(Arrays.asList(Allele.ALT_C, Allele.ALT_T));

        // GT 2/2 LAA [2]   LGT 1/1
        Genotype hom2_2 = makeGenotypeWithAlleles(Arrays.asList(Allele.ALT_T, Allele.ALT_T));

        // GT 1/3 LAA [1,3] LGT 1/2
        Genotype het1_3 = makeGenotypeWithAlleles(Arrays.asList(Allele.ALT_C, ALT_AAT));

        // GT 0 LAA [0] LGT 0
        Genotype hap_ref = makeGenotypeWithAlleles(Arrays.asList(Allele.REF_A));

        // GT 3 LAA [1] LGT 1
        Genotype hap_alt = makeGenotypeWithAlleles(Arrays.asList(ALT_AAT));


        return new Object[][]{
                makeGenotypes(vcbNoNonRef, het0_2, Arrays.asList(2), "0/1", Arrays.asList(0,1,2,3), Arrays.asList(0,2)),
                makeGenotypes(vcbNoNonRef, hom0_0, Arrays.asList(), "0/0", Arrays.asList(0,1,2,3), Arrays.asList(0)),
                makeGenotypes(vcbNoNonRef, het1_2, Arrays.asList(1,2), "1/2", Arrays.asList(0,1,2,3), Arrays.asList(0,1,2)),
                makeGenotypes(vcbNoNonRef, hom2_2, Arrays.asList(2), "1/1", Arrays.asList(0,1,2,3), Arrays.asList(0,2)),
                makeGenotypes(vcbNoNonRef, het1_3, Arrays.asList(1,3), "1/2", Arrays.asList(0,1,2,3), Arrays.asList(0,1,3)),
                makeGenotypes(vcbNoNonRef, hap_ref, Arrays.asList(), "0", Arrays.asList(0,1,2,3), Arrays.asList(0)),
                makeGenotypes(vcbNoNonRef, hap_alt, Arrays.asList(3), "1", Arrays.asList(0,1,2,3), Arrays.asList(0,3)),

                makeGenotypes(vcbWithNonRef, het0_2, Arrays.asList(2,4), "0/1", Arrays.asList(0,1,2,3,4), Arrays.asList(0,2,4)),
                makeGenotypes(vcbWithNonRef, hom0_0, Arrays.asList(4), "0/0", Arrays.asList(0,1,2,3,4), Arrays.asList(0,4)),
                makeGenotypes(vcbWithNonRef, het1_2, Arrays.asList(1,2,4), "1/2", Arrays.asList(0,1,2,3,4), Arrays.asList(0,1,2,4)),
                makeGenotypes(vcbWithNonRef, hom2_2, Arrays.asList(2,4), "1/1", Arrays.asList(0,1,2,3,4), Arrays.asList(0,2,4)),
                makeGenotypes(vcbWithNonRef, het1_3, Arrays.asList(1,3,4), "1/2", Arrays.asList(0,1,2,3,4), Arrays.asList(0,1,3,4)),
                makeGenotypes(vcbWithNonRef, hap_ref, Arrays.asList(4), "0", Arrays.asList(0,1,2,3,4), Arrays.asList(0,4)),
                makeGenotypes(vcbWithNonRef, hap_alt, Arrays.asList(3,4), "1", Arrays.asList(0,1,2,3,4), Arrays.asList(0,3,4)),
        };
    }

    @Test(dataProvider = "getTestCasesLAAandLGTandAD")
    public void testADToLAD(VariantContext vc, Genotype original, Genotype expected){
        Genotype actual = LocalAlleler.addLocalFields(original, vc);
        VariantContextTestUtils.assertGenotypesAreEqual(actual, expected);
    }
}