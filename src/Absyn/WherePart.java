/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class WherePart extends Absyn {

	public Conditions conditions;
	/**
	 * 
	 */
	public WherePart(int p, Conditions c) {
		pos = p;
		conditions = c;
	}

	public String toString() {
		return " WHERE " + conditions.toString();
	}
}
