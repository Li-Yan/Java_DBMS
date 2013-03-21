/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class ConstTime extends Const {

	public String time;
	public int hour;
	public int minute;
	public double second;
	/**
	 * @param p
	 * @param t
	 */
	public ConstTime(int p, int t, String s) {
		super(p, t);
		time = s;
		hour = new Integer(s.substring(1, 3));
		minute = new Integer(s.substring(4, 6));
		second = new Double(s.substring(7, s.length()-1));
	}

	public String toString() {
		return time;
	}
}
