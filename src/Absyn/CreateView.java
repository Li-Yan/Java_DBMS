/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class CreateView extends Stm {

	public Symbol.Symbol name;
	public IdList id_list;
	public Query query;
	/**
	 * 
	 */
	public CreateView(int p, Symbol.Symbol n, IdList il, Query q) {
		pos = p;
		name = n;
		id_list = il;
		query = q;
	}
	
	public String toString() {
		return name.toString();
	}
}
