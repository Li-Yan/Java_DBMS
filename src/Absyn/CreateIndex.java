/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class CreateIndex extends Stm {

	/**
	 * Index name;
	 */
	public Symbol.Symbol index;
	
	/**
	 * Which table the index on
	 */
	public Symbol.Symbol table;
	
	/**
	 * table's attributes' name
	 */
	public IdList id_list;
	
	/**
	 * 
	 */
	public CreateIndex(int p, Symbol.Symbol i, Symbol.Symbol t, IdList il) {
		pos = p;
		index = i;
		table = t;
		id_list = il;
	}

	public String toString() {
		return "CreateIndex " + index.toString();
	}
}
