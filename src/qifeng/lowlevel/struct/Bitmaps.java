package qifeng.lowlevel.struct;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

public class Bitmaps {

	public static boolean used(ByteBuffer b, int index) {
		return (b.get(index / Byte.SIZE) & (1 << (index % Byte.SIZE))) != 0;
	}

	public static void use(ByteBuffer b, int index) {
		b.put(index / Byte.SIZE,
				(byte) (b.get(index / Byte.SIZE) | (1 << (index % Byte.SIZE))));
	}

	public static void unuse(ByteBuffer b, int index) {
		b.put(index / Byte.SIZE,
				(byte) (b.get(index / Byte.SIZE) & ~(1 << (index % Byte.SIZE))));
	}

	public static boolean used(LongBuffer b, int index) {
		return (b.get(index / Long.SIZE) & (1 << (index % Long.SIZE))) != 0;
	}

	public static void use(LongBuffer b, int index) {
		b.put(index / Long.SIZE, b.get(index / Long.SIZE) | (1 << (index % Long.SIZE)));
	}

	public static void unuse(LongBuffer b, int index) {
		b.put(index / Long.SIZE, b.get(index / Long.SIZE) & ~(1 << (index % Long.SIZE)));
	}
}