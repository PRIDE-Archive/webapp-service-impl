package uk.ac.ebi.pride.archive.web.service.controller.viewer;

import uk.ac.ebi.pride.archive.web.service.model.viewer.ModifiedLocation;
import uk.ac.ebi.pride.archive.web.service.model.viewer.Peptide;
import uk.ac.ebi.pride.archive.web.service.model.viewer.PeptideMatch;
import uk.ac.ebi.pride.archive.web.service.model.viewer.Protein;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Reisinger
 *         Date: 08/05/14
 * @since $version
 */
public class DummyDataCreator {

    public static final String assayAccession = "12345";

    public static final String prot1Accession = "P02768";
    private static final String prot1Sequence =
            "MKWVTFISLLFLFSSAYSRGVFRRDAHKSEVAHRFKDLGEENFKALVLIAFAQYLQQCPF" +
            "EDHVKLVNEVTEFAKTCVADESAENCDKSLHTLFGDKLCTVATLRETYGEMADCCAKQEP" +
            "ERNECFLQHKDDNPNLPRLVRPEVDVMCTAFHDNEETFLKKYLYEIARRHPYFYAPELLF" +
            "FAKRYKAAFTECCQAADKAACLLPKLDELRDEGKASSAKQRLKCASLQKFGERAFKAWAV" +
            "ARLSQRFPKAEFAEVSKLVTDLTKVHTECCHGDLLECADDRADLAKYICENQDSISSKLK" +
            "ECCEKPLLEKSHCIAEVENDEMPADLPSLAADFVESKDVCKNYAEAKDVFLGMFLYEYAR" +
            "RHPDYSVVLLLRLAKTYETTLEKCCAAADPHECYAKVFDEFKPLVEEPQNLIKQNCELFE" +
            "QLGEYKFQNALLVRYTKKVPQVSTPTLVEVSRNLGKVGSKCCKHPEAKRMPCAEDYLSVV" +
            "LNQLCVLHEKTPVSDRVTKCCTESLVNRRPCFSALEVDETYVPKEFNAETFTFHADICTL" +
            "SEKERQIKKQTALVELVKHKPKATKEQLKAVMDDFAAFVEKCCKADDKETCFAEEGKKLV" +
            "AASQAALGL";
    private static final String prot1Desc = "Some default description, ideally containing the gene name....";
    private static final int prot1TaxId = 9606;

    private static final String pep1Sequence = "TCVADESAENCDKSLHTLFGD";
    private static final String pep2Sequence = "DVFLGMFLYEYARRHPDYSVVLLLRLAK";
    private static final String pep3Sequence = "CCAAADNHECYAK";
    private static final String pep4Sequence = "DVFLGMFLYEYARRHPDYSVVLLLRLAK";
    private static final String pep5Sequence = "DVFLGMFLYEYARRHPDYSVVLLLRLAK";
    private static final ModifiedLocation pep1Mod = new ModifiedLocation("OXIDATION", 5);
    private static final ModifiedLocation pep2Mod = new ModifiedLocation("PHOSPHORYLATION", 7);
    private static final int prot1Pep1Pos = 76;
    private static final int prot1Pep2Pos = 348;
    private static final int prot1Pep3Pos = 384;
    private static final int noProtPepPos = -1;

    private static final List<String> tissues1 = new ArrayList<String>(0);
    private static final List<String> tissues2;
    static {
        tissues2 = new ArrayList<String>(2);
        tissues2.add("brain");
        tissues2.add("heart");
    }





    public static Protein createDummyProtein1() {
        Protein protein = new Protein();

        protein.setSequence(prot1Sequence);
        protein.setId(assayAccession + "__" + prot1Accession);
        protein.setAccession(prot1Accession);
        protein.setDescription(prot1Desc);

        List<PeptideMatch> list = new ArrayList<PeptideMatch>(2);
        list.add(createDummyPeptide(prot1Accession, pep1Sequence, prot1Pep1Pos, pep1Mod));
        list.add(createDummyPeptide(prot1Accession, pep2Sequence, prot1Pep2Pos, pep2Mod));

        // create a peptide that does not match the protein sequence
        PeptideMatch pm = createDummyPeptide(prot1Accession, pep3Sequence, prot1Pep3Pos, pep2Mod);
        pm.setUniqueness(-1);
        list.add(pm);
        // create a peptide that does not have a start position (pos = 0)
        pm = createDummyPeptide(prot1Accession, pep3Sequence, 0, pep2Mod);
        pm.setUniqueness(-1);
        list.add(pm);
        // create a peptide that has a negative start position
        pm = createDummyPeptide(prot1Accession, pep4Sequence, noProtPepPos, pep2Mod);
        pm.setUniqueness(-1);
        list.add(pm);
        // create a peptide that has a start position beyond the protein length
        pm = createDummyPeptide(prot1Accession, pep5Sequence, prot1Sequence.length()+100, pep2Mod);
        pm.setUniqueness(-1);
        list.add(pm);


        protein.setPeptides(list);

        List<ModifiedLocation> prot1Modifications = new ArrayList<ModifiedLocation>(2);
        prot1Modifications.add(new ModifiedLocation(pep1Mod.getModification(), prot1Pep1Pos + pep1Mod.getPosition() - 1));
        prot1Modifications.add(new ModifiedLocation(pep2Mod.getModification(), prot1Pep2Pos + pep2Mod.getPosition() - 1));
        protein.setModifiedLocations(prot1Modifications);

        protein.setTaxonID(prot1TaxId);
        protein.setTissues(tissues2);

        return protein;
    }

    public static PeptideMatch createDummyPeptide(String proteinAccession, String sequence, int position, ModifiedLocation mod) {

        PeptideMatch peptide = new PeptideMatch();
        peptide.setId(assayAccession + "__" + proteinAccession + "__" + sequence);
        peptide.setSequence(sequence);
        peptide.setPosition(position);
        peptide.setUniqueness(1);
        peptide.setSymbolic(false);
        peptide.setTaxonID(prot1TaxId);
        peptide.setTissues(tissues1);

        List<String> assays = new ArrayList<String>();
        assays.add(assayAccession);
        peptide.setAssays(assays);

        List<ModifiedLocation> modifiedLocations = new ArrayList<ModifiedLocation>(1);
        modifiedLocations.add(mod);
        peptide.setModifiedLocations(modifiedLocations);

        return peptide;
    }

    public static List<Peptide> createDummyPeptideList() {
        List<Peptide> list = new ArrayList<Peptide>();

        list.add(createDummyPeptide(prot1Accession, pep1Sequence, prot1Pep1Pos, pep1Mod));
        list.add(createDummyPeptide(prot1Accession, pep2Sequence, prot1Pep2Pos, pep2Mod));

        // create peptide that does not match the protein sequence
        PeptideMatch pm = createDummyPeptide(prot1Accession, pep3Sequence, prot1Pep3Pos, pep2Mod);
        pm.setUniqueness(-1);
        list.add(pm);
        // create a peptide that does not have a start position (pos = 0)
        pm = createDummyPeptide(prot1Accession, pep3Sequence, 0, pep2Mod);
        pm.setUniqueness(-1);
        list.add(pm);
        // create a peptide that has a negative start position
        pm = createDummyPeptide(prot1Accession, pep4Sequence, noProtPepPos, pep2Mod);
        pm.setUniqueness(-1);
        list.add(pm);
        // create a peptide that has a start position beyond the protein length
        pm = createDummyPeptide(prot1Accession, pep5Sequence, prot1Sequence.length()+100, pep2Mod);
        pm.setUniqueness(-1);
        list.add(pm);

        return list;
    }

}
