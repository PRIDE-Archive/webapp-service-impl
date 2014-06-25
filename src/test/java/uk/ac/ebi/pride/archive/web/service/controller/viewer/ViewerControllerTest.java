package uk.ac.ebi.pride.archive.web.service.controller.viewer;

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.archive.web.service.model.viewer.ModifiedLocation;
import uk.ac.ebi.pride.archive.web.service.model.viewer.PeptideMatch;
import uk.ac.ebi.pride.archive.web.service.model.viewer.Protein;

import java.util.Collections;

/**
 * @author Florian Reisinger
 * @since 0.1
 */
public class ViewerControllerTest {


    private ViewerControllerImpl viewerController = new ViewerControllerImpl();

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

        viewerController.inferProteinModifications(protein);

        // make sure the modifications are sorted according to their position on the protein sequence
        Collections.sort(protein.getModifiedLocations(), new ModifiedLocation.ModifiedLocationPositionComparator());

        // test that the modifications are located at the expected positions
        Assert.assertEquals(18, protein.getModifiedLocations().get(0).getPosition());
        Assert.assertEquals(20, protein.getModifiedLocations().get(1).getPosition());
        Assert.assertEquals(23, protein.getModifiedLocations().get(2).getPosition());
        Assert.assertEquals(41, protein.getModifiedLocations().get(3).getPosition());
        Assert.assertEquals(43, protein.getModifiedLocations().get(4).getPosition());
        Assert.assertEquals(46, protein.getModifiedLocations().get(5).getPosition());
        Assert.assertEquals(93, protein.getModifiedLocations().get(6).getPosition());
        Assert.assertEquals(95, protein.getModifiedLocations().get(7).getPosition());
        Assert.assertEquals(98, protein.getModifiedLocations().get(8).getPosition());

    }


}
