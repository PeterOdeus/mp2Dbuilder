package org.mp2dbuilder.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import metaprint2d.Fingerprint;
import metaprint2d.analyzer.FingerprintGenerator;
import metaprint2d.analyzer.data.AtomData;
import metaprint2d.analyzer.data.Transformation;

import org.mp2dbuilder.mcss.AtomMapperUtil;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

@SuppressWarnings("unused")
public class MetaboliteHandler {

	private static ILoggingTool LOG = LoggingToolFactory
			.createLoggingTool(MetaboliteHandler.class);
	public static final String COMMON_ID_FIELD_NAME = "mcsCommonId";
	public static final String REACTION_CENTRE_FIELD_NAME = "reactionCentre";
	public static final String SMART_HIT_FIELD_NAME = "isSmartsHit";

	@SuppressWarnings("unchecked")
	public Transformation getTransformation(IReactionSet reactionSet)
			throws Exception {
		Transformation t = null;
		try {
			// if (reaction.getReactantCount() != 1) {
			// throw new
			// IllegalArgumentException("Reactant count expected: 1, found: " +
			// reaction.getReactantCount());
			// }
			// if (reaction.getProductCount() != 1) {
			// throw new
			// IllegalArgumentException("Product count expected: 1, found: " +
			// reaction.getProductCount());
			// }

			LOG.info("preparing for transformation");
			Map<String,? extends Object> preparedMap = prepareForTransformation(reactionSet);

			List<AtomData> atomDataList = (List<AtomData>) preparedMap.get("atomDataList");

			t = new Transformation();
			t.setAtomData(atomDataList);

			// ????????? what about this one?
			// t.setMappings(analysis.getMappings());
		} catch (Exception e) {
			LOG.warn("Exception thrown. ignoring this reaction");
		}
		LOG.info("returning transformation");
		return t;
	}

	@SuppressWarnings("unchecked")
	public Map<String,? extends Object> prepareForTransformation(IReactionSet reactionSet)
			throws Exception {

		Map returnMap = new HashMap();

		// Get the reaction and its Sybyl types
		IAtomContainer reactant = (IAtomContainer) reactionSet.getReaction(0)
				.getReactants().getMolecule(0);
		returnMap.put("reactant",reactant);
		SybylAtomTypeMatcher reactantMatcher = SybylAtomTypeMatcher
				.getInstance(reactant.getBuilder());
		IAtomType[] reactantTypes = reactantMatcher
				.findMatchingAtomType(reactant);

		// and generate its fingerprints.
		FingerprintGenerator fpGenerator = new FingerprintGenerator();
		List<Fingerprint> fpList = fpGenerator.generateFingerprints(reactant,
				reactantTypes);

		// Then get the product
		IAtomContainer product = (IAtomContainer) reactionSet.getReaction(0)
				.getProducts().getMolecule(0);
		returnMap.put("product",product);

		/*
		 * Aromaticity needs to be perceived in the same way for both reactant
		 * AND product, otherwise the mcs will be incorrect.
		 */
		SybylAtomTypeMatcher productMatcher = SybylAtomTypeMatcher
				.getInstance(product.getBuilder());
		// we don't care about the types result,just the transformation the
		// product goes through.
		// I.e. CDKHueckelAromaticityDetector
		reactantMatcher.findMatchingAtomType(product);

		// and generate the Maximum Common SubStructure
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(
				reactant, product);
		IAtomContainer mcss = getFirstMCSHavingMostAtoms(mcsList);
		returnMap.put("mcss",mcss);

		// Mark the reactant atoms that are reaction centres
		setReactionCentres(reactant, product, mcss);

		// and generate a list of atoms to be returned
		Set<String> typeOfReactionCentresCandidates = new HashSet<String>();
		typeOfReactionCentresCandidates.add(REACTION_CENTRE_FIELD_NAME);
		List<AtomData> atomDataList = getProcessedAtoms(fpList, reactant,
				typeOfReactionCentresCandidates);
		returnMap.put("atomDataList",atomDataList);

		return returnMap;

	}

	protected List<AtomData> getProcessedAtoms(List<Fingerprint> fpList,
			IAtomContainer reactant, Set<String> typeOfReactionCentresCandidates) {
		List<AtomData> atomDataList = new ArrayList<AtomData>();
		int i = 0;
		Boolean isReactionCentre = null;
		Boolean tempIsReactionCentre = null;
		AtomData atomData = null;
		Set<String> typeOfReactionCentres = null;
		for (Fingerprint fp : fpList) {
			IAtom atom = reactant.getAtom(i);
			isReactionCentre = null;
			for (String reactionCentreCandidate : typeOfReactionCentresCandidates) {
				tempIsReactionCentre = (Boolean) atom
						.getProperty(reactionCentreCandidate);
				if (tempIsReactionCentre != null
						&& tempIsReactionCentre.equals(Boolean.TRUE)) {
					isReactionCentre = Boolean.TRUE;
					if (typeOfReactionCentres == null) {
						typeOfReactionCentres = new HashSet<String>();
					}
					typeOfReactionCentres.add(reactionCentreCandidate);
				}
			}
			if (isReactionCentre == null) {
				isReactionCentre = Boolean.FALSE;
			}
			atomData = new AtomData(fp, isReactionCentre.booleanValue(),
					typeOfReactionCentres);
			atomDataList.add(atomData);
			i++;
		}
		return atomDataList;
	}

	public void setReactionCentres(IAtomContainer reactant,
			IAtomContainer product, IAtomContainer mcs) throws Exception {
		// Make sure that all structures have there atom IDs in common
		AtomMapperUtil util = new AtomMapperUtil();
		util.setCommonIds(COMMON_ID_FIELD_NAME, mcs, reactant, product);

		setRemovedAtomsReactionCentres(reactant);

		setAddedAtomsReactionCentres(reactant, product);
	}

	private void setAddedAtomsReactionCentres(IAtomContainer reactant,
			IAtomContainer product) {
		// get all product atoms that have null ids
		List<IAtom> nullIdAtoms = getNullIdAtoms(product);
		// for each product null atom X: if X is connected to "id atoms"
		// (Plural),
		// then the reactant atoms having those ids are reaction centres.
		List<IAtom> connectedAtoms = null;
		String currentId = null;
		Set<String> idSet = new HashSet<String>();
		for (IAtom atom : nullIdAtoms) {
			connectedAtoms = product.getConnectedAtomsList(atom);
			for (IAtom connectedAtom : connectedAtoms) {
				currentId = (String) connectedAtom
						.getProperty(COMMON_ID_FIELD_NAME);
				if (currentId != null) {
					idSet.add(currentId);
				}
			}
		}
		for (String id : idSet) {
			for (IAtom atom : reactant.atoms()) {
				currentId = (String) atom.getProperty(COMMON_ID_FIELD_NAME);
				if (id.equals(currentId)) {
					atom.setProperty(REACTION_CENTRE_FIELD_NAME, new Boolean(
							true));
					break;
				}
			}
		}
	}

	private void setRemovedAtomsReactionCentres(IAtomContainer reactant) {

		// get all reactant atoms that have null ids
		List<IAtom> nullIdAtoms = getNullIdAtoms(reactant);

		// for each reactant null atom X: if X is connected to an "id atom",
		// then X is a reaction centre.
		List<IAtom> connectedAtoms = null;
		for (IAtom atom : nullIdAtoms) {
			connectedAtoms = reactant.getConnectedAtomsList(atom);
			for (IAtom connectedAtom : connectedAtoms) {
				if (connectedAtom.getProperty(COMMON_ID_FIELD_NAME) != null) {
					connectedAtom.setProperty(REACTION_CENTRE_FIELD_NAME,
							new Boolean(true));
				}
			}
		}
	}

	private List<IAtom> getNullIdAtoms(IAtomContainer atomContainer) {
		List<IAtom> nullIdAtoms = new ArrayList<IAtom>();
		String currentId = null;
		for (IAtom atom : atomContainer.atoms()) {
			currentId = (String) atom.getProperty(COMMON_ID_FIELD_NAME);
			if (currentId == null) {
				nullIdAtoms.add(atom);
			}
		}
		return nullIdAtoms;
	}

	public IAtomContainer getFirstMCSHavingMostAtoms(
			List<IAtomContainer> mcsList) {
		IAtomContainer chosenAtomContainer = null;
		int maxCount = -1;
		for (IAtomContainer atoms : mcsList) {
			if (atoms.getAtomCount() > maxCount) {
				maxCount = atoms.getAtomCount();
				if (chosenAtomContainer == null) {
					LOG.info("Choosing MCS having " + atoms.getAtomCount()
							+ ".");
				} else {
					LOG.info("No, wait... Choosing MCS having "
							+ atoms.getAtomCount() + " instead.");
				}
				chosenAtomContainer = atoms;
			}
		}
		return chosenAtomContainer;
	}

}
