/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class UseDB extends Stm {

	/**
	 * Database's name
	 */
	public Symbol.Symbol name;
	
	/**
	 * 
	 */
	public UseDB(int p, Symbol.Symbol n) {
		pos = p;
		name = n;
	}

	public String toString() {
		return name.toString();
	}
}
