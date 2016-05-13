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

    public static final String ASSAY_ACCESSION = "12345";
    public static final String PROT_1_ACCESSION = "P02768";
    public static final String PROTEIN_1_ID = ASSAY_ACCESSION + "__" + PROT_1_ACCESSION;
    private static final String PROT_1_SEQUENCE =
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
    private static final String PROT_1_DESC = "Some default description, ideally containing the gene name....";
    private static final int PROT_1_TAXID = 9606;

    private static final String PEP_1_SEQUENCE = "TCVADESAENCDKSLHTLFGD";
    private static final String PEP_2_SEQUENCE = "DVFLGMFLYEYARRHPDYSVVLLLRLAK";
    private static final String PEP_3_SEQUENCE = "CCAAADNHECYAK";
    private static final ModifiedLocation PEP_1_MOD = new ModifiedLocation("OXIDATION", 5, 13.0);
    private static final ModifiedLocation PEP_2_MOD = new ModifiedLocation("PHOSPHORYLATION", 7, 20.0);
    private static final int PROT_1_PEP_1_POS = 76;
    private static final int PROT_1_PEP_2_POS = 348;
    private static final int PROT_1_PEP_3_POS = 384;
    private static final int NO_PROT_PEP_POS = -1;

    private static final List<String> TISSUES_1 = new ArrayList<String>(0);
    private static final List<String> TISSUES_2;
    static {
        TISSUES_2 = new ArrayList<String>(2);
        TISSUES_2.add("brain");
        TISSUES_2.add("heart");
    }





    public static Protein createDummyProtein1() {
        Protein protein = new Protein();

        protein.setSequence(PROT_1_SEQUENCE);
        protein.setId(ASSAY_ACCESSION + "__" + PROT_1_ACCESSION);
        protein.setAccession(PROT_1_ACCESSION);
        protein.setDescription(PROT_1_DESC);

        List<PeptideMatch> list = createDummyPeptideList();


        protein.setPeptides(list);

        List<ModifiedLocation> prot1Modifications = new ArrayList<ModifiedLocation>(2);
        prot1Modifications.add(new ModifiedLocation(PEP_1_MOD.getModification(), PROT_1_PEP_1_POS + PEP_1_MOD.getPosition() - 1, PEP_1_MOD.getMass()));
        prot1Modifications.add(new ModifiedLocation(PEP_2_MOD.getModification(), PROT_1_PEP_2_POS + PEP_2_MOD.getPosition() - 1, PEP_2_MOD.getMass()));
        protein.setModifiedLocations(prot1Modifications);

        protein.setTaxonID(PROT_1_TAXID);
        protein.setTissues(TISSUES_2);

        return protein;
    }

    public static <T extends Peptide> List<T> createDummyPeptideList() {
        List<T> list = new ArrayList<T>();

        Peptide peptide = new Peptide();
        list.add((T)createDummyPeptide(PROT_1_ACCESSION, PEP_1_SEQUENCE, PROT_1_PEP_1_POS, PEP_1_MOD));
        list.add((T)createDummyPeptide(PROT_1_ACCESSION, PEP_2_SEQUENCE, PROT_1_PEP_2_POS, PEP_2_MOD));

        // create peptide that does not match the protein sequence
        PeptideMatch pm = createDummyPeptide(PROT_1_ACCESSION, PEP_3_SEQUENCE, PROT_1_PEP_3_POS, PEP_2_MOD);
        pm.setUniqueness(-1);
        list.add((T)pm);
        // create a peptide that does not have a start position (pos = 0)
        pm = createDummyPeptide(PROT_1_ACCESSION, PEP_3_SEQUENCE, 0, PEP_2_MOD);
        pm.setUniqueness(-1);
        list.add((T)pm);
        // create a peptide that has a negative start position
        pm = createDummyPeptide(PROT_1_ACCESSION, PEP_2_SEQUENCE, NO_PROT_PEP_POS, PEP_2_MOD);
        pm.setUniqueness(-1);
        list.add((T)pm);
        // create a peptide that has a start position beyond the protein length
        pm = createDummyPeptide(PROT_1_ACCESSION, PEP_2_SEQUENCE, PROT_1_SEQUENCE.length()+100, PEP_2_MOD);
        pm.setUniqueness(-1);
        list.add((T)pm);

        return list;
    }


    private static PeptideMatch createDummyPeptide(String proteinAccession, String sequence, int position, ModifiedLocation mod) {

        PeptideMatch peptide = new PeptideMatch();
        peptide.setId(ASSAY_ACCESSION + "__" + proteinAccession + "__" + sequence);
        peptide.setSequence(sequence);
        peptide.setPosition(position);
        peptide.setUniqueness(1);
        peptide.setSymbolic(false);
        peptide.setTaxonID(PROT_1_TAXID);
        peptide.setTissues(TISSUES_1);

        List<String> assays = new ArrayList<String>();
        assays.add(ASSAY_ACCESSION);
        peptide.setAssays(assays);

        List<ModifiedLocation> modifiedLocations = new ArrayList<ModifiedLocation>(1);
        modifiedLocations.add(mod);
        peptide.setModifiedLocations(modifiedLocations);

        return peptide;
    }

}
