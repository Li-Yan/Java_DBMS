/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class ExpStar extends Exp {

	/**
	 * 
	 */
	public ExpStar(int p) {
		pos = p;
	}

	public String toString() {
		return "*";
	}
}
