/**
 * 
 */
package Absyn;

import Symbol.Symbol;

/**
 * @author Kiki
 *
 */
public class DropIndex extends Stm {

	/**
	 * Drop index name;
	 */
	public Symbol index;
	
	/**
	 * Drop index name;
	 */
	public Symbol table;
	
	/**
	 * 
	 */
	public DropIndex(int p, Symbol i, Symbol t) {
		pos = p;
		index = i;
		table = t;
	}

	public String toString() {
		return "DROP INDEX " + index.toString() + " ON " + table.toString();
	}
}
