package metaprint2d;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class Fingerprint implements Comparable<Fingerprint> {
	private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static Map<Fingerprint, WeakReference<Fingerprint>> internMap = new WeakHashMap();
	private final FPLevel[] levels;
	private final String rootAtomType;
	private int hashCode;

	public Fingerprint(byte[][] fp) {
		this.levels = new FPLevel[fp.length];
		for (int i = 0; i < fp.length; ++i) {
			this.levels[i] = new FPLevel(fp[i]);
		}
		this.rootAtomType = detectRootAtomType();
	}

	public Fingerprint(FPLevel[] levels) {
		this.levels = new FPLevel[levels.length];
		System.arraycopy(levels, 0, this.levels, 0, levels.length);
		this.rootAtomType = detectRootAtomType();
	}

	public Fingerprint(Fingerprint fp) {
		this.levels = fp.levels;
		this.rootAtomType = fp.rootAtomType;
	}

	private String detectRootAtomType() {
		byte[] bytes = this.levels[0].getBytes();
		for (int i = 0; i < bytes.length; ++i) {
			if (bytes[i] != 0) {
				return ((String) Constants.ATOM_TYPE_LIST.get(i));
			}
		}
		return null;
	}

	public Fingerprint intern() {
		WeakReference<Fingerprint> ref = (WeakReference) internMap.get(this);
		Fingerprint ifp;
		if (ref != null) {
			ifp = (Fingerprint) ref.get();
			if (ifp != null) {
				return ifp;
			}
		}
		ifp = new Fingerprint(this);

		for (int i = 0; i < this.levels.length; ++i) {
			this.levels[i] = this.levels[i].intern();
		}
		internMap.put(this, new WeakReference<Fingerprint>(this));
		return this;
	}

	public byte[][] getBytes() {
		int nlevel = this.levels.length;
		byte[][] bytes = new byte[nlevel][];
		for (int i = 0; i < nlevel; ++i) {
			bytes[i] = this.levels[i].getBytes();
		}
		return bytes;
	}

	public String getRootAtomType() {
		return this.rootAtomType;
	}

	private static byte[][] intsToBytes(int[][] fp) {
		byte[][] bytes = new byte[fp.length][];
		for (int i = 0; i < fp.length; ++i) {
			int[] f = fp[i];
			bytes[i] = new byte[f.length];
			for (int j = 0; j < f.length; ++j) {
				bytes[i][j] = (byte) f[j];
			}
		}
		return bytes;
	}

	public static Fingerprint parseString(String string) {
		byte[][] fp = getBytes(string);
		return new Fingerprint(fp);
	}

	protected static byte[][] getBytes(String string) {
		byte[][] fp = new byte[6][33];
		for (int l = 0; l < 6; ++l) {
			for (int i = 0; i < 33; ++i) {
				char c = string.charAt(l * 33 + i);
				if ((c >= '0') && (c <= '9'))
					fp[l][i] = (byte) (c - '0');
				else if ((c >= 'A') && (c <= 'Z'))
					fp[l][i] = (byte) (c - 'A' + 10);
				else {
					throw new IllegalArgumentException("Illegal character: ["
							+ c + "]\n[" + string + "]");
				}
			}
		}
		return fp;
	}

	public String toString() {
		StringBuilder text = new StringBuilder();
		for (int l = 0; l < this.levels.length; ++l) {
			if (l != 0) {
				text.append(' ');
			}
			FPLevel f = this.levels[l];
			byte[] bytes = f.getBytes();
			for (int i = 0; i < bytes.length; ++i) {
				text.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
						.charAt(bytes[i]));
			}
		}
		return text.toString();
	}

	public String to6lvfString() {
		StringBuilder text = new StringBuilder();
		byte[] bytes;
		for (int l = 0; l < this.levels.length; ++l) {
			bytes = this.levels[l].getBytes();
			for (int i = 0; i < bytes.length; ++i) {
				text.append(bytes[i]);
				text.append(' ');
			}
		}
		return text.toString();
	}

	public static Fingerprint parseFullString(String s) {
		String[] a = s.split(";");
		byte[][] bytes = new byte[6][33];
		bytes[0][((Integer) Constants.ATOM_TYPE_INDEX.get(a[0].toUpperCase()))
				.intValue()] = 1;
		for (int i = 1; i < a.length; ++i) {
			int i0 = a[i].indexOf(45);
			int l = Integer.parseInt(a[i].substring(0, i0));
			int i1 = a[i].indexOf(45, i0 + 1);
			int n = Integer.parseInt(a[i].substring(i0 + 1, i1));
			String x = a[i].substring(i1 + 1);

			int ix = ((Integer) Constants.ATOM_TYPE_INDEX.get(x.toUpperCase()))
					.intValue();
			bytes[l][ix] = (byte) n;
		}
		return new Fingerprint(bytes);
	}

	public String toFullString() {
		byte[] bytes = this.levels[0].getBytes();
		int len = bytes.length;
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < len; ++i) {
			if (bytes[i] == 0) {
				continue;
			}
			text.append((String) Constants.ATOM_TYPE_LIST.get(i));
			text.append(';');
		}
		for (int l = 1; l < this.levels.length; ++l) {
			bytes = this.levels[l].getBytes();
			for (int i = 0; i < len; ++i) {
				int n = bytes[i];
				if (n != 0) {
					text.append(l);
					text.append('-');
					text.append(n);
					text.append('-');
					text.append((String) Constants.ATOM_TYPE_LIST.get(i));
					text.append(';');
				}
			}

		}

		return text.toString();
	}

	public FPLevel getLevel(int i) {
		return this.levels[i];
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		FPLevel[] levels2 = ((Fingerprint) obj).levels;
		if (this.levels.length != levels2.length) {
			return false;
		}
		for (int i = this.levels.length - 1; i >= 0; --i) {
			if (!(this.levels[i].equals(levels2[i]))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		if (this.hashCode == 0) {
			int h = 0;
			for (int i = 0; i < this.levels.length; ++i) {
				h = 63 * h + this.levels[i].hashCode();
			}

			this.hashCode = h;
		}
		return this.hashCode;
	}

	public int compareTo(Fingerprint o) {
		byte[] thisBytes;
		byte[] thatBytes;
		for (int i = 0; i < 6; ++i) {
			thisBytes = this.levels[i].getBytes();
			thatBytes = o.levels[i].getBytes();
			for (int j = 0; j < 33; ++j) {
				byte a = thisBytes[j];
				byte b = thatBytes[j];
				if (a < b) {
					return -1;
				}
				if (a > b) {
					return 1;
				}
			}
		}
		return 0;
	}

	public String toCompressedString() {
		StringBuilder s = new StringBuilder();
		s
				.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
						.charAt(this.levels.length));
		for (int i = 0; i < this.levels.length; ++i) {
			if (i != 0) {
				s.append("_");
			}

			byte[] bs = this.levels[i].getBytes();
			for (int j = 0; j < bs.length; ++j) {
				byte k = bs[j];
				if (k != 0) {
					s
							.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
									.charAt(j));
					s
							.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
									.charAt(k));
				}
			}
		}
		return s.toString();
	}

	public static Fingerprint parseCompressedString(String string) {
		byte[][] fp = getBytesFromCompressedString(string);
		return new Fingerprint(fp);
	}

	protected static byte[][] getBytesFromCompressedString(String string) {
		int nlevels = parseChar(string.charAt(0));
		byte[][] fp = new byte[nlevels][33];
		int ix = 1;
		int len = string.length();

		for (int l = 0; l < nlevels; ++l) {
			while (ix < len) {
				char c;
				if ((c = string.charAt(ix)) == '_')
					break;
				byte fpix = parseChar(c);
				byte fpval = parseChar(string.charAt(ix + 1));
				fp[l][fpix] = fpval;
				ix += 2;
			}
			++ix;
		}
		return fp;
	}

	public static byte parseChar(char c) {
		if ((c >= '0') && (c <= '9')) {
			return (byte) (c - '0');
		}
		if ((c >= 'A') && (c <= 'Z')) {
			return (byte) ('\n' + c - 65);
		}
		if ((c >= 'a') && (c <= 'z')) {
			return (byte) ('$' + c - 97);
		}
		throw new IllegalArgumentException("Illegal character: " + c);
	}

}
