package metaprint2d;

public class FingerprintData extends Fingerprint {
	public static boolean eq = true;
	private final int id;
	private final int rcCount;
	private final int substrateCount;

	public FingerprintData(int id, byte[][] bytes, int rc, int sc) {
		super(bytes);
		this.id = id;
		this.rcCount = rc;
		this.substrateCount = sc;
	}

	public FingerprintData(int id, FPLevel[] levels, int rc, int sc) {
		super(levels);
		this.id = id;
		this.rcCount = rc;
		this.substrateCount = sc;
	}

	public FingerprintData(int id, Fingerprint fp, int rc, int sc) {
		super(fp);
		this.id = id;
		this.rcCount = rc;
		this.substrateCount = sc;
	}

	public FingerprintData(Fingerprint fp, int rc, int sc) {
		super(fp);
		this.id = 0;
		this.rcCount = rc;
		this.substrateCount = sc;
	}

	public int getId() {
		return this.id;
	}

	public int getRcCount() {
		return this.rcCount;
	}

	public int getSubstrateCount() {
		return this.substrateCount;
	}

	public static FingerprintData parseString(String string, int rc, int sc) {
		byte[][] bytes = Fingerprint.getBytes(string);
		Fingerprint fp = new Fingerprint(bytes);
		return new FingerprintData(fp, rc, sc);
	}
}
