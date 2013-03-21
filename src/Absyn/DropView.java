/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class DropView extends Stm {

	/**
	 * Drop view id list
	 */
	public IdList id_list;
	/**
	 * 
	 */
	public DropView(int p, IdList il) {
		pos = p;
		id_list = il;
	}

	public String toString() {
		return id_list.toString();
	}
}
