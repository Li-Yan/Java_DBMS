/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class DecimalType extends Type {

	public int lenth1;
	public int lenth2;
	/**
	 * @param p
	 * @param t
	 */
	public DecimalType(int p, int t, int l1, int l2) {
		super(p, t);
		lenth1 = l1;
		lenth2 = l2;
	}

	public String toString() {
		return "DECIMAL(" + (new Integer(lenth1)).toString() + ", " + 
		(new Integer(lenth2)).toString() + ")";
	}
}
