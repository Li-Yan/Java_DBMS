/**
 * 
 */
package Absyn;


/**
 * @author Kiki
 *
 */
public class ExpIdDotId extends Exp {

	public Symbol.Symbol name1;
	
	public Symbol.Symbol name2;
	/**
	 * 
	 */
	public ExpIdDotId(int p, Symbol.Symbol n1, Symbol.Symbol n2) {
		pos = p;
		name1 = n1;
		name2 = n2;
	}

	public String toString() {
		return name1.toString()+"."+name2.toString();
	}
}
