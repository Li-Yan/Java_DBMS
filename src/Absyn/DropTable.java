/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class DropTable extends Stm {

	/**
	 * Table name
	 */
	public IdList id_list;
	
	/**
	 * 
	 */
	public DropTable(int p, IdList i) {
		pos = p;
		id_list = i;
	}

	public String toString() {
		return id_list.toString();
	}
}
