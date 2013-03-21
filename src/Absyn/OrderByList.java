/**
 * 
 */
package Absyn;


/**
 * @author Kiki
 *
 */
public class OrderByList{

	public OrderBy head;
	
	public OrderByList tail;
	/**
	 * 
	 */
	public OrderByList(OrderBy h, OrderByList t) {
		head = h;
		tail = t;
	}

	public String toString() {
		String ret = head.toString();
		for(OrderByList i = tail; i!=null; i = i.tail) {
			ret += ", "+i.head.toString();
		}
		return ret;
	}
}