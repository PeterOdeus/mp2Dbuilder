package metaprint2d;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

public class FPLevel {
	private static Map<FPLevel, WeakReference<FPLevel>> internMap = new WeakHashMap();
	private byte[] bytes;
	private int hashCode;

	public FPLevel(byte[] bytes) {
		this.bytes = new byte[bytes.length];
		System.arraycopy(bytes, 0, this.bytes, 0, bytes.length);
	}

	public FPLevel(FPLevel level) {
		this.bytes = new byte[level.bytes.length];
		System.arraycopy(level.bytes, 0, this.bytes, 0, this.bytes.length);
		this.hashCode = level.hashCode;
	}

	public FPLevel intern() {
		WeakReference<FPLevel> ref = (WeakReference) internMap.get(this);
		FPLevel ifpl;
		if (ref != null) {
			ifpl = (FPLevel) ref.get();
			if (ifpl != null) {
				return ifpl;
			}
		}
		ifpl = new FPLevel(this);
		internMap.put(this, new WeakReference<FPLevel>(this));
		return this;
	}

	public int hashCode() {
		if (this.hashCode == 0) {
			int h = 0;
			for (int i = 0; i < this.bytes.length; ++i) {
				h = 15 * h + this.bytes[i];
			}
			this.hashCode = h;
		}
		return this.hashCode;
	}

	public boolean equals(Object obj) {
		if ((obj == null) || (!(obj instanceof FPLevel))) {
			return false;
		}
		return Arrays.equals(this.bytes, ((FPLevel) obj).bytes);
	}

	public double calcSoergelDistance(FPLevel that) {
		byte[] xs = this.bytes;
		byte[] ys = that.bytes;

		int length = xs.length;
		if (length != ys.length) {
			throw new IllegalArgumentException(
					"Fingerprints have different lengths (" + xs.length + ", "
							+ ys.length + ")");
		}

		double numerator = 0.0D;
		double denominator = 0.0D;

		for (int i = 0; i < length; ++i) {
			byte x = xs[i];
			byte y = ys[i];
			numerator += x * y;
			denominator += x * x + y * y - (x * y);
		}

		return ((denominator == 0.0D) ? 0.0D : 1.0D - (numerator / denominator));
	}

	public double calcHammingDistance(FPLevel that) {
		byte[] xs = this.bytes;
		byte[] ys = that.bytes;

		int length = xs.length;
		if (length != ys.length) {
			throw new IllegalArgumentException(
					"Fingerprints have different lengths (" + xs.length + ", "
							+ ys.length + ")");
		}

		double dist = 0.0D;

		for (int i = 0; i < length; ++i) {
			byte x = xs[i];
			byte y = ys[i];
			dist += Math.abs(x - y);
		}

		return dist;
	}

	public String toString() {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < this.bytes.length; ++i) {
			text.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
					.charAt(this.bytes[i]));
		}
		return text.toString();
	}

	public String toCompressedString() {
		StringBuilder text = new StringBuilder();
		text
				.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
						.charAt("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
								.length()));
		for (int j = 0; j < this.bytes.length; ++j) {
			byte k = this.bytes[j];
			if (k != 0) {
				text
						.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
								.charAt(j));
				text
						.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
								.charAt(k));
			}
		}
		return text.toString();
	}

	public String to6lvfString() {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < this.bytes.length; ++i) {
			if (i != 0) {
				text.append(' ');
			}
			text.append(this.bytes[i]);
		}
		return text.toString();
	}

	public byte[] getBytes() {
		byte[] copy = new byte[this.bytes.length];
		System.arraycopy(this.bytes, 0, copy, 0, this.bytes.length);
		return copy;
	}

	public static FPLevel parseString(String string) {
		byte[] fp = new byte[33];
		for (int j = 0; j < 33; ++j) {
			char c = string.charAt(j);
			if ((c >= '0') && (c <= '9'))
				fp[j] = (byte) (c - '0');
			else if ((c >= 'A') && (c <= 'Z'))
				fp[j] = (byte) (c - 'A' + 10);
			else {
				throw new IllegalArgumentException("Illegal character: [" + c
						+ "]\n[" + string + "]");
			}
		}
		return new FPLevel(fp);
	}

	public static FPLevel parseCompressedString(String string) {
		byte[] fp = new byte[33];
		for (int i = 0; i < string.length(); i += 2) {
			byte fpix = Fingerprint.parseChar(string.charAt(i));
			byte fpval = Fingerprint.parseChar(string.charAt(i + 1));
			fp[fpix] = fpval;
		}
		return new FPLevel(fp);
	}

	public boolean matches(byte[] bytes) {
		if (bytes == null) {
			return false;
		}
		if (bytes.length != this.bytes.length) {
			return false;
		}
		for (int i = bytes.length - 1; i >= 0; --i) {
			if (bytes[i] != this.bytes[i]) {
				return false;
			}
		}
		return true;
	}

	public int getByte(int index) {
		return this.bytes[index];
	}
}
