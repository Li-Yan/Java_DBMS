/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class Field extends Absyn {

	public Exp exp;
	
	public Symbol.Symbol refname;
	/**
	 * 
	 */
	public Field(int p, Exp e, Symbol.Symbol r) {
		pos = p;
		exp = e;
		refname = r;
	}

	public String toString() {
		if (refname == null)
			return exp.toString();
		else
			return exp.toString() + " AS " + refname.toString();
	}
}
