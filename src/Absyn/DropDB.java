/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class DropDB extends Stm {

	/**
	 * Database's name
	 */
	public Symbol.Symbol name;
	
	/**
	 * 
	 */
	public DropDB(int p, Symbol.Symbol n) {
		pos = p;
		name = n;
	}

	public String toString() {
		return name.toString();
	}
}
