/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class CreateDB extends Stm {

	/**
	 * Database's name
	 */
	public Symbol.Symbol name;
	
	/**
	 * 
	 */
	public CreateDB(int p, Symbol.Symbol n) {
		pos = p;
		name = n;
	}

	public String toString() {
		return "CreateDB " + name.toString();
	}
}
