package prototyping;

public interface ReactionSmartsDefinitions {

	//Our hydroxylation smarts definition
	public static String HYDROXYLATION_REACTANT_SMARTS="[$([*:1])]";
	public static String HYDROXYLATION_PRODUCT_SMARTS="[*:1][O;H1]";

	//Our dealkylation smarts definition
	public static String N_DEALKYLATION_REACTANT_SMARTS="[$([CH3][NH0;X3:1]([CH3:2]))]";
	public static String N_DEALKYLATION_PRODUCT_SMARTS="[CH3:2][NH:1]";


}
