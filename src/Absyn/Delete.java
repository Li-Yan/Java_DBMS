/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class Delete extends Stm {

	/**
	 * Table's name
	 */
	public Symbol.Symbol name;
	
	/**
	 * Conditions
	 */
	public WherePart where;
	/**
	 * 
	 */
	public Delete(int p, Symbol.Symbol n, WherePart w) {
		pos = p;
		name = n;
		where = w;
	}

	public String toString() {
		return name.toString();
	}
}
