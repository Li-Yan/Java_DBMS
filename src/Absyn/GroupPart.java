/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class GroupPart extends Absyn {

	public IdList id_list;
	/**
	 * 
	 */
	public GroupPart(int p, IdList il) {
		pos = p;
		id_list = il;
	}
	
	public String toString() {
		return "GROUP BY " + id_list.toString();
	}
}
