/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class ConstString extends Const {

	public String value;
	
	/**
	 * @param p
	 * @param t
	 */
	public ConstString(int p, int t, String v) {
		super(p, t);
		value = v;
	}

	public String toString() {
		return value;
	}
}
