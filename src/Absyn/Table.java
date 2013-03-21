/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class Table extends Absyn {

	public Symbol.Symbol name;
	
	public Symbol.Symbol refname;
	
	/**
	 * SubQuery
	 */
	public Query query;
	
	/**
	 * 
	 */
	public Table(int p, Symbol.Symbol n1, Symbol.Symbol n2, Query q) {
		pos = p;
		name = n1;
		if (n2 == null)
			refname = n1;
		else
			refname = n2;
		query = q;
	}

	public String toString() {
		if (refname == null)
			return name.toString();
		else
			return name.toString() + " AS " + refname.toString();
	}
}
