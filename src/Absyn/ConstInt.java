/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class ConstInt extends Const {

	public int value;
	/**
	 * @param p
	 * @param t
	 */
	public ConstInt(int p, int t, int v) {
		super(p, t);
		value = v;
	}

	public String toString() {
		return (new Integer(value)).toString();
	}
}
