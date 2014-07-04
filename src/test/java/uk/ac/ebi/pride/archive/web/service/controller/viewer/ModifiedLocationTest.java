package uk.ac.ebi.pride.archive.web.service.controller.viewer;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.archive.web.service.model.viewer.ModifiedLocation;

import java.util.*;

/**
 * @author florian@ebi.ac.uk
 * @since 0.1.2
 */
public class ModifiedLocationTest {

    private ModifiedLocation[] modArray;
    private final int COUNT = 10;

    @Before
    public void setup() {
        modArray = new ModifiedLocation[COUNT];
        modArray[0] = new ModifiedLocation("Phosphorylation", 5);
        modArray[1] = new ModifiedLocation("Oxidation", 5);
        modArray[2] = new ModifiedLocation("Oxidation", 5); // duplicated entry
        modArray[3] = new ModifiedLocation("Oxidation", 6);
        modArray[4] = new ModifiedLocation("Oxidation", 1);
        modArray[5] = new ModifiedLocation("DiOxidation", 5);
        modArray[6] = new ModifiedLocation("DiOxidation", 6);
        modArray[7] = new ModifiedLocation("DiOxidation", 8);
        modArray[8] = new ModifiedLocation("DiOxidation", 2);
        modArray[9] = new ModifiedLocation("DiOxidation", 5); // duplicated entry
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
        TreeSet<ModifiedLocation> modSet = new TreeSet<ModifiedLocation>(new ModifiedLocation.ModifiedLocationPositionComparator());
        modSet.addAll(Arrays.asList(modArray));

        Assert.assertTrue(modSet.size() == 8); // due to duplication we only expect 8 entries

        Iterator<ModifiedLocation> iterator = modSet.descendingIterator();
        Assert.assertEquals(iterator.next(), modArray[7]);
        Assert.assertEquals(iterator.next(), modArray[3]);
        Assert.assertEquals(iterator.next(), modArray[6]);
        Assert.assertEquals(iterator.next(), modArray[0]);
        Assert.assertEquals(iterator.next(), modArray[1]);
        Assert.assertEquals(iterator.next(), modArray[5]);
        Assert.assertEquals(iterator.next(), modArray[8]);
        Assert.assertEquals(iterator.next(), modArray[4]);

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
