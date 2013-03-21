/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class ExpList{

	public Exp head;
	
	public ExpList tail;
	/**
	 * 
	 */
	public ExpList(Exp h, ExpList el) {
		head = h;
		tail = el;
	}

	public String toString() {
		String ret = head.toString();
		for(ExpList i = tail; i!=null; i = i.tail) {
			ret += ", "+i.head.toString();
		}
		return ret;
	}
}
