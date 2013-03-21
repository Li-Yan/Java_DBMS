/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class ConstDouble extends Const {

	public double value;
	/**
	 * @param p
	 * @param t
	 */
	public ConstDouble(int p, int t, double v) {
		super(p, t);
		value = v;
	}

	public String toString() {
		return (new Double(value)).toString();
	}
}
