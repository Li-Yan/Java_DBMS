/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class Update extends Stm {

	/**
	 * Update statement type:
	 * 	1:	UPDATE ID SET lvalue EQ const where_part
	 * 	2:	UPDATE ID AS ID SET lvalue EQ const where_part
	 */
	public int type;
	
	/**
	 * Table's name
	 */
	public Symbol.Symbol name;
	
	/**
	 * Table's reference name
	 */
	public Symbol.Symbol refname;
	
	public Exp lvalue;
	
	public Const value;
	
	public WherePart where;
	
	/**
	 * {: RESULT = new Update(uleft, 2, sym(i), sym(i2), l, c, w); :}
	 */
	public Update(int p, int t, Symbol.Symbol n, Symbol.Symbol r,
			Exp l, Const c, WherePart w) {
		pos = p;
		name = n;
		refname = r;
		lvalue = l;
		value = c;
		where = w;
	}

	public String toString() {
		return name.toString();
	}
}
