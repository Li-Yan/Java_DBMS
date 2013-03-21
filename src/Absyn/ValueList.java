/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class ValueList {

	public Const head;
	
	public ValueList tail;
	/**
	 * 
	 */
	public ValueList(Const h, ValueList t) {
		head = h;
		tail = t;
	}

	public String toString() {
		String ret = head.toString();
		for(ValueList i = tail; i!=null; i = i.tail) {
			ret += ", "+i.head.toString();
		}
		return ret;
	}
}
