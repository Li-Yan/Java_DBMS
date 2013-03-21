/*
 * @(#)Symbol.java	1.0 2011/01/09
 * Package: Symbol
 * Copyright 2011 Kiki Tiger Compiler, Inc. All rights reserved.
 * @author ÕÅÐù  5080309672 SJTU
 */

package Symbol;

/**
 * Class for a symbol (a encapsulation of string)
 * 
 * @author Kiki
 */
public class Symbol {
	private String name;
	private Symbol(String n) {
		name=n;
	}
	
	private static java.util.Dictionary<String, Symbol> dict = new java.util.Hashtable<String, Symbol>();

	public String toString() {
		return name;
	}

 	/** 
	 * Make return the unique symbol associated with a string.
	 * Repeated calls to <tt>symbol("abc")</tt> will return the same Symbol.
	 */
	public static Symbol symbol(String n) {
		String u = n.intern().toLowerCase();
		Symbol s = dict.get(u);
		if (s==null) {
			s = new Symbol(u);
			dict.put(u,s);
		}
		return s;
	}
}