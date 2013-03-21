/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class ConstBool extends Const {

	/**
	 * boolean value:
	 * 		0	:	false
	 * 		1	:	true
	 * 		2	:	unknown
	 */
	public int value;
	/**
	 * @param p
	 * @param t
	 */
	public ConstBool(int p, int t, int v) {
		super(p, t);
		value = v;
	}

	public String toString() {
		switch(value) {
		case 0:	return "false";
		case 1:	return "true";
		case 2:	return "unknown";
		}
		return "false";
	}
	
}
