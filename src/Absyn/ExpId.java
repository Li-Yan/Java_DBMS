/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class ExpId extends Exp {

	/**
	 * Id's name
	 */
	public Symbol.Symbol name;
	
	/**
	 * 
	 */
	public ExpId(int p, Symbol.Symbol n) {
		pos = p;
		name = n;
	}

	public String toString() {
		return name.toString();
	}
	
}
