/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class Function extends Exp {

	/**
	 * Function type according to Parse.sym.*;
	 * 	COUNT:	76
	 * 	SUM:	77
	 * 	AVG:	78
	 * 	MIN:	79
	 * 	MAX:	80
	 */
	public int type;
	
	/**
	 * Boolean, is DISTINCT or not
	 */
	public Boolean isDistinct;
	
	/**
	 * Function exp
	 */
	public Exp exp;
	/**
	 * 
	 */
	public Function(int p, int t, boolean i, Exp e) {
		pos = p;
		type = t;
		isDistinct = i;
		exp =e;
	}

	public String toString() {
		return Parse.Lex.symnames[type]+"("+exp.toString()+")";
	}
}
