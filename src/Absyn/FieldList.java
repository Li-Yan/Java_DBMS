/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class FieldList {

	public Field head;
	
	public FieldList tail;
	/**
	 * 
	 */
	public FieldList(Field h, FieldList t) {
		head = h;
		tail = t;
	}

	public String toString() {
		String ret = "(" + head.toString();
		for(FieldList i = tail; i!=null; i = i.tail) {
			ret += ", "+i.head.toString();
		}
		ret += ")";
		return ret;
	}
}
