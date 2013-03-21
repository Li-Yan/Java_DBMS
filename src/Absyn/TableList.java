/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class TableList{

	public Table head;
	
	public TableList tail;
	/**
	 * 
	 */
	public TableList(Table h, TableList t) {
		head = h;
		tail = t;
	}

	public String toString() {
		String ret = head.toString();
		for(TableList i = tail; i!=null; i = i.tail) {
			ret += ", "+i.head.toString();
		}
		return ret;
	}
}
