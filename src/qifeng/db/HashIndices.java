package qifeng.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import qifeng.schema.Symbol;

/**
 * dummy indices object
 */
public class HashIndices extends
		HashMap<Symbol, ConcurrentHashMap<Object, ArrayList<Integer>>> {
	private static final long serialVersionUID = -2116643287201171450L;

	public HashIndices(String... keys) {
		super();
		for (String s : keys) {
			super.put(new Symbol(s),
					new ConcurrentHashMap<Object, ArrayList<Integer>>());
		}
	}

	public HashIndices(Symbol... keys) {
		super();
		for (Symbol s : keys) {
			super.put(s, new ConcurrentHashMap<Object, ArrayList<Integer>>());
		}
	}

	public Map<Object, ArrayList<Integer>> getIndexOf(Symbol keyname) {
		return super.get(keyname);
	}
	
	public Map<Object, ArrayList<Integer>> getIndexOf(String keyname) {
		return getIndexOf(new Symbol(keyname));
	}

	public ArrayList<Integer> getOffsetsOf(Symbol keyname, Object obj) {
		return super.get(keyname).get(obj);
	}

	public ArrayList<Integer> getOffsetsOf(String keyname, Object obj) {
		return getOffsetsOf(new Symbol(keyname), obj);
	}
	
	public void addAnOffset(Symbol keyname, Object obj, Integer offset) {
		putAnOffset(keyname, obj, offset);
	}
	
	public void addAnOffset(String keyname, Object obj, Integer offset) {
		putAnOffset(new Symbol(keyname), obj, offset);
	}
	
	public void putAnOffset(Symbol keyname, Object obj, Integer offset) {
		ArrayList<Integer> a = this.getOffsetsOf(keyname, obj);
		if (a == null) {
			a = new ArrayList<Integer>();
			super.get(keyname).put(obj, a);
		}
		a.add(offset);
	}

	public void putAnOffset(String keyname, Object obj, Integer offset) {
		putAnOffset(new Symbol(keyname), obj, offset);
	}
	
	public boolean delAnOffset(Symbol keyname, Object obj, Integer offset) {
		ArrayList<Integer> a = this.getOffsetsOf(keyname, obj);
		if (a == null) {
			return false;
		}
		return a.remove(offset);
	}
	
	public boolean delAnOffset(String keyname, Object obj, Integer offset) {
		return delAnOffset(new Symbol(keyname), obj, offset);
	}

	public boolean removeIndex(Symbol keyname) {
		return super.remove(keyname) != null;
	}

	public boolean removeIndex(String keyname) {
		return removeIndex(new Symbol(keyname));
	}
	
	/**
	 * add new __and__ empty indices
	 * 
	 * @param keys
	 */
	public void addIndices(String... keys) {
		for (String ss : keys) {
			Symbol s = new Symbol(ss);
			if (!super.containsKey(s)) {
				super.put(s,
						new ConcurrentHashMap<Object, ArrayList<Integer>>());
			}
		}
	}

	public void addIndices(Symbol... keys) {
		for (Symbol s : keys) {
			if (!super.containsKey(s)) {
				super.put(s,
						new ConcurrentHashMap<Object, ArrayList<Integer>>());
			}
		}
	}
}