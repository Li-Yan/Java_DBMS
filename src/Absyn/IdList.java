/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class IdList extends Absyn {

	/**
	 * List head
	 */
	public Exp head;
	
	/**
	 * List tail
	 */
	public IdList tail;
	
	/**
	 * 
	 */
	public IdList(int p, Exp h, IdList t) {
		pos = p;
		head = h;
		tail = t;
	}

	public String toString() {
		String ret = head.toString();
		for(IdList i = tail; i!=null; i = i.tail) {
			ret += ", "+i.head.toString();
		}
		return ret;
	}
	
}
