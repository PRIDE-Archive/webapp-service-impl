package uk.ac.ebi.pride.archive.web.service.model.viewer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * @author florian@ebi.ac.uk
 * @since 0.1.2
 */
public class PeptideMatchTest {

    private PeptideMatch[] pepArray;


    @Before
    public void setup() {
        pepArray = new PeptideMatch[16];
        // different peptides, same location
        pepArray[0] = new PeptideMatch(5, "PEPTIDE");
        pepArray[1] = new PeptideMatch(5, "OTHER");
        pepArray[2] = new PeptideMatch(5, "ANOTHER");
        pepArray[3] = new PeptideMatch(5, "YETANOTHER");
        // duplicated entries on same location
        pepArray[4] = new PeptideMatch(5, "PEPTIDE"); // same as 0
        pepArray[5] = new PeptideMatch(5, "ANOTHER"); // same as 2
        // same peptide, different location
        pepArray[6] = new PeptideMatch(1, "PEPTIDE");
        pepArray[7] = new PeptideMatch(15, "PEPTIDE");
        // peptide not matching the protein sequence (e.g. position < 1)
        pepArray[8] = new PeptideMatch(0, "PEPTIDE");
        pepArray[9] = new PeptideMatch(0, "PEPTIDE"); // same as 8
        // same non-matching peptide, different modifications
        pepArray[10] = new PeptideMatch(0, "PEPTIDE");
        pepArray[10].getModifiedLocations().add(new ModifiedLocation("Oxidation", 3));
        // non matching peptide second case
        pepArray[11] = new PeptideMatch(-1, "PEPTIDE"); // same as 8 & 9
        pepArray[12] = new PeptideMatch(-1, "PEPTIDE"); // same as 10 since same ModifiedLocation
        pepArray[12].getModifiedLocations().add(new ModifiedLocation("Oxidation", 3));
        pepArray[13] = new PeptideMatch(-1, "PEPTIDE"); // new peptide since ModifiedLocation on different position
        pepArray[13].getModifiedLocations().add(new ModifiedLocation("Oxidation", 2));

        pepArray[14] = new PeptideMatch(-1, "PEPTIDE"); // new peptide since ModifiedLocation with different mod
        pepArray[14].getModifiedLocations().add(new ModifiedLocation("DiOxidation", 3));

        pepArray[15] = new PeptideMatch(-1, "PEPTIDE"); // new peptide since different number of ModifiedLocationS
        pepArray[15].getModifiedLocations().add(new ModifiedLocation("DiOxidation", 2));
        pepArray[15].getModifiedLocations().add(new ModifiedLocation("DiOxidation", 3));
    }

    @Test
    public void testPeptideMatchComparator() {


        TreeSet<PeptideMatch> pepSet = new TreeSet<PeptideMatch>(new PeptideMatchPositionComparator());
        pepSet.addAll(Arrays.asList(pepArray));

        Assert.assertTrue(pepSet.size() == 11); // due to 5 duplications we only expect 11 entries

//        for (PeptideMatch pm : pepSet) {
//            System.out.println("Peptide:" + pm.getPosition() + ":" + pm.getSequence());
//            for (ModifiedLocation ml : pm.getModifiedLocations()) {
//                System.out.println("\t" + ml);
//            }
//        }


        Iterator<PeptideMatch> iterator = pepSet.iterator();

        PeptideMatch aux = iterator.next(); // two possibilities 8 or 9
        Assert.assertTrue(aux.equals(pepArray[8]) || aux.equals(pepArray[9]));
        Assert.assertEquals(iterator.next(), pepArray[13]);
        Assert.assertEquals(iterator.next(), pepArray[14]);
        aux = iterator.next(); // two possibilities 10 or 12
        Assert.assertTrue(aux.equals(pepArray[10]) || aux.equals(pepArray[12]));
        Assert.assertEquals(iterator.next(), pepArray[15]);
        Assert.assertEquals(iterator.next(), pepArray[6]);
        Assert.assertEquals(iterator.next(), pepArray[2]);
        Assert.assertEquals(iterator.next(), pepArray[1]);
        Assert.assertEquals(iterator.next(), pepArray[0]);
        Assert.assertEquals(iterator.next(), pepArray[3]);
        Assert.assertEquals(iterator.next(), pepArray[7]);


        // check that there are no further elements
        boolean nextIsException = false;
        try {
            iterator.next();
        } catch (NoSuchElementException e) {
            nextIsException = true;
        }
        Assert.assertTrue(nextIsException);

    }
}
