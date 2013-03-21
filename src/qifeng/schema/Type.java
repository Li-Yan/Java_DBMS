package qifeng.schema;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Type implements Serializable {
	private static final long serialVersionUID = 5285647812258468910L;

	/**
	 * <table>
	 * <tr>
	 * <td>type</td>
	 * <td>valid length</td>
	 * </tr>
	 * <tr>
	 * <td>INTEGER</td>
	 * <td>1, 2, 4, 8</td>
	 * </tr>
	 * <tr>
	 * <td>FLOATING_POINT</td>
	 * <td>4, 8</td>
	 * </tr>
	 * <tr>
	 * <td>CHARACTER</td>
	 * <td>&gt; 0</td>
	 * </tr>
	 * <tr>
	 * <td>VARY_LENGTH_CHARACTER</td>
	 * <td>&gt; 0</td>
	 * </tr>
	 * <tr>
	 * <td>DATE</td>
	 * <td>not used</td>
	 * </tr>
	 * </table>
	 */
	public static enum Category {
		INTEGER, FLOATING_POINT, CHARACTER, VARY_LENGTH_CHARACTER, DATE
	}

	public static class Attribute implements Serializable {
		private static final long serialVersionUID = -1304088671441109405L;

		public static Attribute PRIMARY_KEY = new Attribute("PRIMARY_KEY");
		public static Attribute NOT_NULL = new Attribute("NOT_NULL");

		private String s;

		private Attribute(String s) {
			this.s = s;
		}

		private Attribute(Object def) {
			this("DEFAULT_VALUE");
			this.setDefault(def);
		}

		private Object def;

		private void setDefault(Object def) {
			this.def = def;
		}

		public Object getDefault() {
			if (this == PRIMARY_KEY || this == NOT_NULL)
				throw new IllegalArgumentException();
			return this.def;
		}

		public boolean isPrimaryKey() {
			return this == PRIMARY_KEY;
		}

		public boolean isNotNull() {
			return this == NOT_NULL;
		}

		public boolean isDefaultValue() {
			return this != Attribute.PRIMARY_KEY && this != Attribute.NOT_NULL;
		}

		public String toString() {
			return s + (isDefaultValue() ? (" " + def) : "");
		}
		
		public static Attribute newDefault(Object def) {
			if (def == null)
				throw new NullPointerException();
			return new Attribute(def);
		}
	}

	/**
	 * @see Type#primitiveMap
	 * @author qifeng
	 * 
	 */
	public static enum JavaType {
		I8, I16, I32, I64, F32, F64, C, V, T, O;
	}

	/**
	 * I8 <-> Byte <br>
	 * I16 <-> Short <br>
	 * I32 <-> Integer<br>
	 * I64 <-> Long<br>
	 * F32 <-> Float<br>
	 * F64 <-> Double<br>
	 * C <-> byte[] (constant length characters)<br>
	 * V <-> String (vary length characters)<br>
	 * T <-> java.sql.Date (long is also permitted)<br>
	 * O <-> Object<br>
	 */
	public static Map<JavaType, Class<?>> primitiveMap;
	static {
		HashMap<JavaType, Class<?>> t = new HashMap<JavaType, Class<?>>();
		t.put(JavaType.I8, Byte.class);
		t.put(JavaType.I16, Short.class);
		t.put(JavaType.I32, Integer.class);
		t.put(JavaType.I64, Long.class);
		t.put(JavaType.F32, Float.class);
		t.put(JavaType.F64, Double.class);
		t.put(JavaType.C, byte[].class);
		t.put(JavaType.V, String.class);
		t.put(JavaType.T, java.sql.Date.class);
		t.put(JavaType.O, Object.class);
		primitiveMap = Collections.unmodifiableMap(t);
	}

	/**
	 * type category
	 */
	private Category category;
	/**
	 * length of the type
	 */
	private int length;
	/**
	 * another representation of some `primitive' type
	 */
	private JavaType javaType;

	private Attribute attribute = null;

	public Type(Category category, int length) {
		if (category == Category.DATE)
			length = 8;
		this.category = category;
		this.length = length;
		this.javaType = resolveJavaType();
	}

	public Type(Category category, int length, Attribute attr) {
		if (category == Category.DATE)
			length = 8;
		this.category = category;
		this.length = length;
		this.javaType = resolveJavaType();
		if (attr != null && attr.isDefaultValue()) {
			Class<?> needed = primitiveMap.get(this.javaType);
			Class<?> provided = attr.getDefault().getClass();
			if (!needed.isAssignableFrom(provided)) {
				throw new IllegalArgumentException("cannot assign " + provided
						+ " to " + needed);
			}
		}
		this.attribute = attr;
	}

	/*
	 * protected Type(Category category, int length, JavaType javaType) { if
	 * (category == Category.DATE) length = 8; this.category = category;
	 * this.length = length; this.javaType = javaType; }
	 */

	protected JavaType resolveJavaType() {
		switch (this.category) {
		case INTEGER:
			switch (length) {
			case 1:
				return JavaType.I8;
			case 2:
				return JavaType.I16;
			case 4:
				return JavaType.I32;
			case 8:
				return JavaType.I64;
			default:
				throw new IllegalArgumentException(
						"unsupported integer length: " + length);
			}
		case FLOATING_POINT:
			switch (length) {
			case 4:
				return JavaType.F32;
			case 8:
				return JavaType.F64;
				// case 0:
				// return BigDecimal.class;
			default:
				return JavaType.F32;
			}
		case CHARACTER:
			return JavaType.C; // sorry, cannot specify a class with
			// specific length (but nevertheless, Class#isArray() is simple
		case VARY_LENGTH_CHARACTER:
			return JavaType.V;
		case DATE:
			return JavaType.T;
		default:
			throw new Error();
		}
	}

	public Type.Category getCategory() {
		return category;
	}

	public int getLength() {
		return length;
	}

	public JavaType getJavaType() {
		return javaType;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public boolean isPrimaryKey() {
		if (this.attribute == null)
			return false;
		return this.attribute.isPrimaryKey();
	}

	public boolean isNotNull() {
		if (this.attribute == null)
			return false;
		return this.attribute.isNotNull();
	}

	public boolean isDefaultValue() {
		if (this.attribute == null)
			return false;
		return this.attribute.isDefaultValue();
	}

	public Object getDefaultValue() {
		return isDefaultValue() ? this.attribute.getDefault() : null;
	}
}
