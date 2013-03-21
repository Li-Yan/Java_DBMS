package qifeng.schema;

import java.io.Serializable;

public class Symbol implements Serializable {
	private static final long serialVersionUID = -2925571615554139667L;
	
	private String s;

	public Symbol(String s) {
		this.s = s.intern();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Symbol) {
			return s.equals(((Symbol)obj).toString());
		} else if (obj instanceof String) {
			return s.equals(((String)obj));
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return s.hashCode();
	}

	@Override
	public String toString() {
		return s;
	}
}
