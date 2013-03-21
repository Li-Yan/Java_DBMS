/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class CharType extends Type {

	/**
	 * Max char lenth
	 */
	public int lenth;
	
	/**
	 * @param p
	 * @param t
	 */
	public CharType(int p, int t, int l) {
		super(p, t);
		lenth = l;
	}

	public String toString() {
		return "CHAR("+(new Integer(lenth)).toString()+")";
	}
}
