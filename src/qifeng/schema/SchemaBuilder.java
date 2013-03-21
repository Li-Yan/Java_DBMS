package qifeng.schema;

import java.util.ArrayList;

import qifeng.schema.Schema.Pair;

/**
 * this is a helper class for build a schema, it is not thread safe but it is
 * very light weight
 */
public class SchemaBuilder {
	private ArrayList<Type> tys;
	private ArrayList<Symbol> syms;
	private Symbol primaryKey = null;

	public SchemaBuilder() {
		tys = new ArrayList<Type>();
		syms = new ArrayList<Symbol>();
	}

	public SchemaBuilder(Schema old) {
		this();
		for (Schema.Pair p : old) {
			tys.add(p.t);
			syms.add(p.s);
		}
	}

	public boolean exists(String symbol) {
		return exists(new Symbol(symbol));
	}

	public boolean exists(Symbol symbol) {
		return syms.contains(symbol);
	}

	public Type get(String symbol) {
		return get(new Symbol(symbol));
	}
	
	public Type get(Symbol symbol) {
		int x = syms.indexOf(symbol);
		if (x == -1) return null;
		return tys.get(x);
	}
	
	public boolean add(String symbol, Type.Category category, int length) {
		return add(new Symbol(symbol), category, length);
	}

	public boolean add(Symbol symbol, Type.Category category, int length) {
		if (exists(symbol))
			return false;
		Type t = new Type(category, length);
		this.tys.add(t);
		this.syms.add(symbol);
		return true;
	}

	public boolean add(String symbol, Type.Category category, int length,
			Type.Attribute attribute) {
		return add(new Symbol(symbol), category, length, attribute);
	}

	public boolean add(Symbol symbol, Type.Category category, int length,
			Type.Attribute attribute) {
		if (exists(symbol))
			return false;
		if (attribute.isPrimaryKey()) {
			if (this.primaryKey != null)
				throw new IllegalArgumentException("duplicate primary key");
			else 
				this.primaryKey = symbol;
		}
		Type t = new Type(category, length, attribute);
		this.tys.add(t);
		this.syms.add(symbol);
		return true;
	}

	public boolean replace(String symbol, Type.Category category, int length) {
		return replace(new Symbol(symbol), category, length);
	}

	public boolean replace(Symbol symbol, Type.Category category, int length) {
		if (!exists(symbol))
			return false;
		int i = this.syms.indexOf(symbol);
		Type t = new Type(category, length);
		this.tys.set(i, t);
		return true;
	}

	public boolean replace(String symbol, Type.Category category, int length,
			Type.Attribute attr) {
		return replace(new Symbol(symbol), category, length, attr);
	}

	public boolean replace(Symbol symbol, Type.Category category, int length,
			Type.Attribute attr) {
		if (!exists(symbol))
			return false;
		if (attr.isPrimaryKey()) {
			if (this.primaryKey != null && this.primaryKey != symbol)
				throw new IllegalArgumentException("duplicate primary key");
			else 
				this.primaryKey = symbol;
		}
		int i = this.syms.indexOf(symbol);
		Type t = new Type(category, length, attr);
		this.tys.set(i, t);
		return true;
	}

	public boolean remove(String symbol) {
		return remove(new Symbol(symbol));
	}

	public boolean remove(Symbol symbol) {
		int i = this.syms.indexOf(symbol);
		if (i < 0)
			return false;
		this.tys.remove(i);
		this.syms.remove(i);
		return true;
	}

	public Schema newSchema() {
		assert (this.tys.size() == this.syms.size());
		Pair[] ps = new Pair[this.syms.size()];
		for (int i = 0; i < ps.length; ++i) {
			ps[i] = new Pair(this.syms.get(i), this.tys.get(i));
		}
		return new Schema(ps, this.primaryKey);
	}
}
