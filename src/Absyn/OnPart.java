/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class OnPart extends Absyn {

	public Conditions conditions;
	/**
	 * 
	 */
	public OnPart(int p, Conditions c) {
		pos = p;
		conditions = c;
	}

	public String toString() {
		return "ON " + conditions.toString();
	}
}
