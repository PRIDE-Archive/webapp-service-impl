package uk.ac.ebi.pride.archive.web.service.model.viewer;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;

/**
 * @author florian@ebi.ac.uk
 * @since 0.1.2
 */
public class ModifiedLocationTest {

    private ModifiedLocation[] modArray;

    @Before
    public void setup() {
        modArray = new ModifiedLocation[10];
        modArray[0] = new ModifiedLocation("Phosphorylation", 5, 20.0);
        modArray[1] = new ModifiedLocation("Oxidation", 5, 13.0);
        modArray[2] = new ModifiedLocation("Oxidation", 5, 13.0); // duplicated entry
        modArray[3] = new ModifiedLocation("Oxidation", 6, 13.0);
        modArray[4] = new ModifiedLocation("Oxidation", 1, 13.0);
        modArray[5] = new ModifiedLocation("DiOxidation", 5, 26.0);
        modArray[6] = new ModifiedLocation("DiOxidation", 6, 26.0);
        modArray[7] = new ModifiedLocation("DiOxidation", 8, 26.0);
        modArray[8] = new ModifiedLocation("DiOxidation", 2, 26.0);
        modArray[9] = new ModifiedLocation("DiOxidation", 5, 26.0); // duplicated entry
    }

    @Test
    public void testModifiedLocationEquals() {
        Assert.assertTrue(modArray[1].equals(modArray[1]));
        Assert.assertTrue(modArray[5].equals(modArray[9]));
        Assert.assertTrue(!modArray[2].equals(modArray[5]));
        Assert.assertTrue(!modArray[2].equals(modArray[3]));
    }

    @Test
    public void testModifiedLocationPositionComparator() {
        TreeSet<ModifiedLocation> modSet = new TreeSet<ModifiedLocation>(new ModifiedLocationPositionComparator());
        modSet.addAll(Arrays.asList(modArray));

        Assert.assertTrue(modSet.size() == 8); // due to duplication we only expect 8 entries

//        for (ModifiedLocation ml : modSet) {
//            System.out.println(ml);
//        }

        Iterator<ModifiedLocation> iterator = modSet.iterator();

        Assert.assertEquals(iterator.next(), modArray[4]);
        Assert.assertEquals(iterator.next(), modArray[8]);
        Assert.assertEquals(iterator.next(), modArray[5]);
        Assert.assertEquals(iterator.next(), modArray[1]);
        Assert.assertEquals(iterator.next(), modArray[0]);
        Assert.assertEquals(iterator.next(), modArray[6]);
        Assert.assertEquals(iterator.next(), modArray[3]);
        Assert.assertEquals(iterator.next(), modArray[7]);


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
