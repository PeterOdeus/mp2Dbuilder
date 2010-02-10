package metaprint2d.analyzer.data;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import metaprint2d.Fingerprint;

public class AtomData
implements Cloneable
{
	private Fingerprint fingerprint;
	private boolean isReactionCentre;
	private Set<String> typeOfReactionCentres;

	public AtomData(Fingerprint fingerprint, boolean isReactionCentre, Set<String> typeOfReactionCentres)
	{
		if (fingerprint == null) {
			throw new NullPointerException();
		}
		this.fingerprint = fingerprint;
		this.isReactionCentre = isReactionCentre;
		this.typeOfReactionCentres = typeOfReactionCentres;
	}

	public Fingerprint getFingerprint() {
		return this.fingerprint;
	}

	public boolean getIsReactionCentre() {
		return this.isReactionCentre;
	}
	
	public Set<String> getTypeOfReactionCentres() {
		if(typeOfReactionCentres == null){
			return new HashSet<String>(0);
		}
		return typeOfReactionCentres;
	}

	public AtomData clone() {
		try {
			AtomData clone = (AtomData)super.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isReactionCentre() {
		return this.isReactionCentre;
	}
}