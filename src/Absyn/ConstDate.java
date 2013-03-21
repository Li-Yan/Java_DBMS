/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class ConstDate extends Const {

	public String date;
	public int year;
	public int month;
	public int day;
	/**
	 * @param p
	 * @param t
	 */
	public ConstDate(int p, int t, String s) {
		super(p, t);
		date = s;
		year = new Integer(s.substring(1, 5));
		month = new Integer(s.substring(6, 8));
		day = new Integer(s.substring(9, 11));
	}

	public String toString() {
		return date;
	}
}
