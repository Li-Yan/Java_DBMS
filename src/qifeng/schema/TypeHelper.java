package qifeng.schema;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class TypeHelper {
	protected TypeHelper() {
	}

	private static Charset cs = Charset.forName("UTF-8");

	public static byte[] newBytesFromString(String s, int len) {
		if (s == null) return new byte[len];
		ByteBuffer bb = cs.encode(s);
		if (len < 0) len = bb.remaining();
		else if (len < bb.remaining()) {
			bb.limit(bb.position() + len);
		}
		byte[] b = new byte[len];
		bb.get(b, 0, bb.remaining());
		return b;
	}

	public static String newStringFromBytes(byte[] b) {
		if (b == null) return null;
		return cs.decode(ByteBuffer.wrap(b)).toString();
	}

}
