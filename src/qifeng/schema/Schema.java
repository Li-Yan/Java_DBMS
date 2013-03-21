package qifeng.schema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import qifeng.schema.Type.Category;
import qifeng.schema.Type.JavaType;

/**
 * The structure of a table. Use {@link SchemaBuilder} to obtain an instance of
 * this class
 * 
 */
public class Schema implements Serializable, Iterable<Schema.Pair> {
	private static final long serialVersionUID = 1196771244973648504L;

	/**
	 * XXX
	 * 
	 * @author qifeng
	 * 
	 */
	public static class Pair implements Serializable {
		private static final long serialVersionUID = -4124528374013945853L;
		public final Symbol s;
		public final Type t;

		public Pair(Symbol s, Type t) {
			this.s = s;
			this.t = t;
		}
	}

	private final Pair[] table;
	private final Symbol primaryKey;
	private transient HashMap<Symbol, Integer> index;
	// if varying char existed, indicated as -1
	private transient int[] offset;

	protected Schema(Pair[] table) {
		this.table = table;
		this.primaryKey = null;
		this.initialize();
	}

	public Schema(Pair[] table, Symbol primaryKey) {
		this.table = table;
		this.primaryKey = primaryKey;
		this.initialize();
	}

	private void initialize() {
		initNameMap();
		initOffsets();
	}

	private void initNameMap() {
		index = new HashMap<Symbol, Integer>();
		for (int i = 0; i < table.length; ++i) {
			index.put(table[i].s, i);
		}
	}

	private void initOffsets() throws Error {
		offset = new int[table.length];
		int off = 0;
		int i;
		L1: for (i = 0; i < table.length; ++i) {
			offset[i] = off;
			Type t = table[i].t;
			switch (t.getCategory()) {
			case DATE:
			case INTEGER:
			case CHARACTER:
				off += t.getLength();
				break;
			case FLOATING_POINT:
				if (t.getLength() > 8)
					off += 4;
				else
					off += t.getLength();
				break;
			case VARY_LENGTH_CHARACTER:
				break L1;
			default:
				throw new Error();
			}
		}
		for (; i < table.length; ++i) {
			offset[i] = -1;
		}
	}

	/**
	 * needed to reconstruct a map
	 * 
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		this.initialize();
	}

	/**
	 * 
	 * @return columns count of this schema
	 */
	public int size() {
		return this.table.length;
	}

	public int getIndex(Symbol s) {
		return this.index.get(s);
	}

	public Type getType(int index) {
		return this.table[index].t;
	}

	public Type getType(Symbol s) {
		return getType(getIndex(s));
	}

	public Symbol getSymbol(int index) {
		return this.table[index].s;
	}

	public Symbol getPrimaryKey() {
		return this.primaryKey;
	}

	@Override
	public Iterator<Pair> iterator() {
		return new Iterator<Pair>() {
			private int index = 0;
			private int len = Schema.this.table.length;

			@Override
			public boolean hasNext() {
				return index < len;
			}

			@Override
			public Pair next() {
				Pair t = Schema.this.table[index];
				++index;
				return t;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@SuppressWarnings("unused")
	private static Object getValueFromRepresentation(Type t, Object val) {
		switch (t.getJavaType()) {
		case I8:
		case I16:
		case I32:
		case I64:
		case F32:
		case F64:
		case T: {
			Object nul = getNullRepresentation(t);
			return nul.equals(val) ? null : val;
		}
		default:
			return val;
		}
	}

	private static Object getNullRepresentation(Type t) {
		switch (t.getJavaType()) {
		case I8:
			return Byte.MIN_VALUE;
		case I16:
			return Short.MIN_VALUE;
		case I32:
			return Integer.MIN_VALUE;
		case I64:
			return Long.MIN_VALUE;
		case F32:
			return Float.NaN;
		case F64:
			return Double.NaN;
		case C:
			return new byte[t.getLength()];
		case T:
			return new java.sql.Date(Long.MIN_VALUE);
		case V:
			return new String();
		case O:
			return null;
		}
		return null;
	}

	public Object getValue(byte[] record, int i) throws IOException {
		int off = getOffset(record, i);
		Type t = this.table[i].t;
		if (off >= 0) {
			int tmp = t.getLength();
			if (t.getCategory() == Category.FLOATING_POINT && (tmp > 8))
				tmp = 4;
			return constructPrimitive(ByteBuffer.wrap(record, off, tmp),
					t.getJavaType());
		}
		// int e = Arrays.asList(this.offset).indexOf(-1);
		int e;
		for (e = 0; e < this.offset.length; ++e) {
			if (this.offset[e] == -1)
				break;
		}
		e -= 1;
		if (e < 0) {
			e = 0;
			off = 0;
		} else
			off = this.offset[e];
		DataInputStream ois = new DataInputStream(new ByteArrayInputStream(
				record, off, record.length - off));
		try {
			skipValues(ois, e, i);
			return constructValue(ois, t);
		} catch (ClassNotFoundException e1) {
			throw new Error(e1);
		}
	}

	public Object getValue(byte[] record, Symbol name) throws IOException {
		return getValue(record, getIndex(name));
	}

	private int getOffset(byte[] record, int i) {
		if (i >= this.table.length || i < 0) {
			throw new ArrayIndexOutOfBoundsException(i);
		}
		int off = this.offset[i];

		if (off >= record.length)
			throw new ArrayIndexOutOfBoundsException(
					"record too short for index " + i);
		return off;
	}

	private Object constructPrimitive(ByteBuffer bytes, JavaType javaType) {
		ByteBuffer bb = bytes.order(ByteOrder.BIG_ENDIAN);
		switch (javaType) {
		case I8: {
			Byte x = bb.get();
			return x.equals(Byte.MIN_VALUE) ? null : x;
		}
		case I16: {
			Short x = bb.getShort();
			return x.equals(Short.MIN_VALUE) ? null : x;
		}
		case I32: {
			Integer x = bb.getInt();
			return x.equals(Integer.MIN_VALUE) ? null : x;
		}
		case I64: {
			Long x = bb.getLong();
			return x.equals(Long.MIN_VALUE) ? null : x;
		}
		case F32: {
			Float x = bb.getFloat();
			return x.isNaN() ? null : x;
		}
		case F64: {
			Double x = bb.getDouble();
			return x.isNaN() ? null : x;
		}
		case C:
			return Arrays.copyOfRange(bb.array(), bb.position(), bb.limit());
		case T: {
			Long x = bb.getLong();
			return x.equals(Long.MIN_VALUE) ? null : new java.sql.Date(x);
		}
		default:
			throw new Error("not fixed length type cannot be used here");
		}
	}

	private void skipValues(DataInputStream ois, int from, int to)
			throws IOException, ClassNotFoundException {
		while (from < to) {
			switch (this.table[from].t.getCategory()) {
			case VARY_LENGTH_CHARACTER:
				ois.readUTF();
				break;
			default:
				int tmp = this.table[from].t.getLength();
				if (this.table[from].t.getCategory() == Category.FLOATING_POINT
						&& (tmp > 8))
					tmp = 4;
				ois.skipBytes(tmp);
			}
			++from;
		}
	}

	private Object constructValue(DataInputStream ois, Type t)
			throws IOException, ClassNotFoundException {
		switch (t.getCategory()) {
		case VARY_LENGTH_CHARACTER:
			return ois.readUTF();
		default:
		//	int l = t.getLength();
		//	if (t.getCategory() == Type.Category.FLOATING_POINT && 8 < l)
		//		l = 4;
			byte[] buf = new byte[t.getLength()];
			ois.readFully(buf);
			return constructPrimitive(ByteBuffer.wrap(buf), t.getJavaType());
		}
	}

	private Object castForNull(Symbol s, Type t) {
		Object val = null;
		if (t.isPrimaryKey() || t.isNotNull())
			throw new NullPointerException("field " + s + " is "
					+ t.getAttribute());
		else if (t.isDefaultValue()) {
			val = t.getDefaultValue();
		} else {
			val = getNullRepresentation(t);
		}
		return val;
	}

	public byte[] putValue(byte[] record, int i, Object val)
			throws IOException, ClassNotFoundException {
		Type t = this.table[i].t;
		Class<?> jt = Type.primitiveMap.get(t.getJavaType());
		if (val != null && !jt.isAssignableFrom(val.getClass())) {
			if (t.getCategory() == Type.Category.DATE) {
				if (val instanceof Long) {
					val = new java.sql.Date((Long) val);
				} else
					val = java.sql.Date.valueOf(val.toString());
			} else {
				// XXX
				throw new RuntimeException("wrong type: " + val.getClass()
						+ ", except " + t.getJavaType());
			}
		}
		if (val == null) {
			val = castForNull(this.table[i].s, t);
		}
		Object casted = jt.cast(val);

		int off = getOffset(record, i);
		if (off >= 0) {
			int tmp = t.getLength();
			if (t.getCategory() == Category.FLOATING_POINT && (tmp > 8))
				tmp = 4;
			replacePrimitiveInPlace(ByteBuffer.wrap(record, off, tmp),
					t.getJavaType(), casted);
			return record;
		}
		// int e = Arrays.asList(this.offset).indexOf(-1);
		int e;
		for (e = 0; e < this.offset.length; ++e) {
			if (this.offset[e] == -1)
				break;
		}
		e -= 1;
		if (e < 0) {
			e = 0;
			off = 0;
		} else
			off = this.offset[e];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream oos = new DataOutputStream(baos);
		DataInputStream ois = new DataInputStream(new ByteArrayInputStream(
				record, off, record.length - off));
		oos.write(record, 0, off);
		writeValues(oos, ois, e, i);
		replaceValue(oos, ois, t, casted);
		++i;
		writeValues(oos, ois, i, this.table.length);
		oos.close();
		return baos.toByteArray();
	}

	public byte[] putValue(byte[] record, Symbol name, Object val)
			throws IOException, ClassNotFoundException {
		return putValue(record, getIndex(name), val);
	}

	private void replacePrimitiveInPlace(ByteBuffer bytes, JavaType javaType,
			Object val) {
		ByteBuffer bb = bytes.order(ByteOrder.BIG_ENDIAN);
		switch (javaType) {
		case I8:
			bb.put(((Byte) val).byteValue());
			break;
		case I16:
			bb.putShort(((Short) val).shortValue());
			break;
		case I32:
			bb.putInt(((Integer) val).intValue());
			break;
		case I64:
			bb.putLong(((Long) val).longValue());
			break;
		case F32:
			bb.putFloat(((Float) val).floatValue());
			break;
		case F64:
			bb.putDouble(((Double) val).doubleValue());
			break;
		case C:
			byte[] ba = (byte[]) val;
			int r = ba.length < bb.remaining() ? ba.length : bb.remaining();
			bb.put((byte[]) val, 0, r);
			break;
		case T:
			bb.putLong(((java.sql.Date) val).getTime());
			break;
		default:
			new Error("not fixed length type cannot be used here");
		}
	}

	private void writeValues(DataOutputStream oos, DataInputStream ois,
			int from, int to) throws IOException, ClassNotFoundException {
		while (from < to) {
			switch (this.table[from].t.getCategory()) {
			case VARY_LENGTH_CHARACTER:
				oos.writeUTF(ois.readUTF());
				break;
			default:
				int tmp = this.table[from].t.getLength();
				if (this.table[from].t.getCategory() == Category.FLOATING_POINT
						&& (tmp > 8))
					tmp = 4;
				byte[] buf = new byte[tmp];
				ois.readFully(buf);
				oos.write(buf);
			}
			++from;
		}
	}

	private void replaceValue(DataOutputStream oos, DataInputStream ois,
			Type t, Object casted) throws IOException, ClassNotFoundException {
		switch (t.getCategory()) {
		case VARY_LENGTH_CHARACTER:
			ois.readUTF();
			oos.writeUTF((String) casted);
			break;
		default:
			int tmp = t.getLength();
			if (t.getCategory() == Category.FLOATING_POINT && (tmp > 8))
				tmp = 4;
			ois.skipBytes(tmp);
			byte[] buf = new byte[t.getLength()];
			replacePrimitiveInPlace(ByteBuffer.wrap(buf), t.getJavaType(),
					casted);
			oos.write(buf);
		}
	}

	public byte[] newValues(Object... vals) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream oos = new DataOutputStream(baos);
			if (vals == null)
				throw new NullPointerException();
			if (vals.length != this.size()) {
				throw new IllegalArgumentException();
			}
			for (int i = 0; i < this.size(); ++i) {
				Object val = vals[i];
				if (vals[i] == null) {
					val = castForNull(this.table[i].s, this.table[i].t);
				}
				appendValue(oos, this.table[i].t, val);
			}
			oos.close();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	private void appendValue(DataOutputStream oos, Type t, Object val)
			throws IOException {
		// ByteBuffer b =
		switch (t.getJavaType()) {
		case I8:
			oos.writeByte(val == null ? Byte.MIN_VALUE : ((Byte) val)
					.intValue());
			break;
		case I16:
			oos.writeShort(val == null ? Short.MIN_VALUE : ((Short) val)
					.intValue());
			break;
		case I32:
			oos.writeInt(val == null ? Integer.MIN_VALUE : ((Integer) val)
					.intValue());
			break;
		case I64:
			oos.writeLong(val == null ? Long.MIN_VALUE : ((Long) val)
					.longValue());
			break;
		case F32:
			oos.writeFloat(val == null ? Float.NaN : ((Float) val).floatValue());
			break;
		case F64:
			oos.writeDouble(val == null ? Double.NaN : ((Double) val)
					.doubleValue());
			break;
		case C:
			oos.write(val == null ? new byte[t.getLength()] : Arrays.copyOf(
					(byte[]) val, t.getLength()));
			break;
		case T:
			if (val instanceof java.sql.Date) {
				val = ((java.sql.Date) val).getTime();
			}
			oos.writeLong(val == null ? 0L : ((Long) val).longValue());
			break;
		case V:// length is not checked...... XXX
			oos.writeUTF((String) val);
			break;
		}
	}

}