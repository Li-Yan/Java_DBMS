/**
 * 
 */
package Absyn;

import java_cup.internal_error;

/**
 * @author Kiki
 *
 */
public class ExpOp extends Exp {

	/**
	 * Operand type:
	 * 	1:	plus
	 * 	2:	minus
	 * 	3:	times
	 * 	4:	divide
	 * 	5:	OROR || just apply for string conjection.
	 */
	public int type;
	public Exp left;
	public Exp right;
	/**
	 * 
	 */
	public ExpOp(int t, Exp l, Exp r) {
		pos = l.pos;
		type = t;
		left = l;
		right = r;
	}

	public String toString() {
		String ret = "(" + left.toString();
		switch(type) {
		case 1:	ret += "+";		break;
		case 2:	ret += "-";		break;
		case 3:	ret += "*";		break;
		case 4:	ret += "/";		break;
		case 5:	ret += "||";	break;
		}
		ret += right.toString() + ")";
		return ret;
	}
}
