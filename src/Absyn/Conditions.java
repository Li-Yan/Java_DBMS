/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class Conditions {

	public Conditions left;
	
	/**
	 * Binary operand
	 * 0: Single Condition
	 * 1: and
	 * 2: or
	 * 3: not	use left
	 */
	public int op;
	
	public Conditions right;
	
	public Condition condition;
	/**
	 * 
	 */
	public Conditions(Condition c, Conditions l, int o, Conditions r) {
		condition = c;
		left = l;
		op = o;
		right = r;
	}

	public String toString() {
		String ret = " ";
		switch(op) {
		case 0:
			ret += condition.toString();
			break;
		case 1:
			ret += "("+left.toString()+") AND ("+right.toString()+") ";
			break;
		case 2:
			ret += "("+left.toString()+") OR ("+right.toString()+") ";
			break;
		case 3:
			ret += " NOT ("+left.toString()+")";
			break;
		}
		return ret;
	}
}
