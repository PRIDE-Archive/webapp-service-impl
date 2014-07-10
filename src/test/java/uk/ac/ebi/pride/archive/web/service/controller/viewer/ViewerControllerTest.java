package uk.ac.ebi.pride.archive.web.service.controller.viewer;

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.archive.web.service.model.viewer.ModifiedLocation;
import uk.ac.ebi.pride.archive.web.service.model.viewer.PeptideMatch;
import uk.ac.ebi.pride.archive.web.service.model.viewer.Protein;

import java.util.Iterator;

/**
 * @author Florian Reisinger
 * @since 0.1
 */
public class ViewerControllerTest {


    @Test
    public void testInferProteinModifications() {

        // create modifications for two example peptides (which have the same sequence)
        ModifiedLocation loc1_1 = new ModifiedLocation();
        loc1_1.setPosition(2);
        loc1_1.setModification("Oxidation");
        ModifiedLocation loc1_2 = new ModifiedLocation();
        loc1_2.setPosition(7);
        loc1_2.setModification("Phosphorylation");
        ModifiedLocation loc2_1 = new ModifiedLocation();
        loc2_1.setPosition(4);
        loc2_1.setModification("Oxidation");

        // create the test peptides (same sequence, different mods)
        PeptideMatch peptide1 = new PeptideMatch();
        peptide1.setSequence("PEPTIDE");
        peptide1.getModifiedLocations().add(loc1_1);
        peptide1.getModifiedLocations().add(loc1_2);

        PeptideMatch peptide2 = new PeptideMatch();
        peptide2.setSequence("PEPTIDE");
        peptide2.getModifiedLocations().add(loc2_1);

        // construct the protein record
        Protein protein = new Protein();
        protein.setSequence("MLSRLQELRKEEETLLPEPTIDERLKAALHDQLNRLKVEPEPTIDEELALQSMISSRREGEMLPSQPAPEPSHDMLVHVDNEASINQTALEPEPTIDELSTRSHVQEEEEEEEEEEEDS");
        protein.getPeptides().add(peptide1);
        protein.getPeptides().add(peptide2);

        ViewerControllerImpl.inferProteinModifications(protein);

        // test that the modifications are located at the expected positions
        Iterator<ModifiedLocation> iterator = protein.getModifiedLocations().iterator();
        Assert.assertEquals(18, iterator.next().getPosition());
        Assert.assertEquals(20, iterator.next().getPosition());
        Assert.assertEquals(23, iterator.next().getPosition());
        Assert.assertEquals(41, iterator.next().getPosition());
        Assert.assertEquals(43, iterator.next().getPosition());
        Assert.assertEquals(46, iterator.next().getPosition());
        Assert.assertEquals(93, iterator.next().getPosition());
        Assert.assertEquals(95, iterator.next().getPosition());
        Assert.assertEquals(98, iterator.next().getPosition());
        Assert.assertFalse(iterator.hasNext());

    }


}
