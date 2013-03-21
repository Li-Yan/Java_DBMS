/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class OrderPart extends Absyn {

	public OrderByList order_list;
	/**
	 * 
	 */
	public OrderPart(int p, OrderByList o) {
		pos = p;
		order_list = o;
	}

	public String toString() {
		return " ORDER BY " + order_list.toString();
	}
}
