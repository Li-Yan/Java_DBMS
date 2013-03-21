/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class ConstTimestamp extends Const {

	public String timestamp;
	public ConstDate date;
	public ConstTime time;
	/**
	 * @param p
	 * @param t
	 */
	public ConstTimestamp(int p, int t, String s) {
		super(p, t);
		timestamp = s;
		date = new ConstDate(p, Parse.sym.DATEVAL, s.substring(1, 11));
		time = new ConstTime(p, Parse.sym.TIMEVAL, s.substring(12, s.length()-1));
	}

	public String toString() {
		return timestamp;
	}
}
