package metaprint2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
	public static final String DB_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final int NUM_LEVELS = 6;
	public static final int FP_SIZE = 33;
	public static final String DUMMY_ATOM_TYPE = "Du";
	public static final Map<String, Integer> ATOM_TYPE_INDEX;
	public static final List<String> ATOM_TYPE_LIST;
	public static final int H_INDEX;
	private static final String[] SYBYL_ATOMTYPES = { "C.3", "C.2", "C.ar",
			"C.1", "N.3", "N.2", "N.1", "O.3", "O.2", "S.3", "N.ar", "P.3",
			"H", "Br", "Cl", "F", "I", "S.2", "N.pl3", "LP", "Na", "K", "Ca",
			"Li", "Al", "Du", "Si", "N.am", "S.O", "S.O2", "N.4", "O.CO2",
			"C.cat" };

	static {
		Map map = new HashMap();
		for (int i = 0; i < SYBYL_ATOMTYPES.length; ++i) {
			map.put(SYBYL_ATOMTYPES[i].toUpperCase(), Integer.valueOf(i));
		}
		ATOM_TYPE_INDEX = Collections.unmodifiableMap(map);

		List list = new ArrayList(SYBYL_ATOMTYPES.length);
		for (String at : SYBYL_ATOMTYPES) {
			list.add(at);
		}
		ATOM_TYPE_LIST = Collections.unmodifiableList(list);

		H_INDEX = ((Integer) ATOM_TYPE_INDEX.get("H")).intValue();
	}

	public static String[] getSybylAtomTypes() {
		return SYBYL_ATOMTYPES;
	}

}
