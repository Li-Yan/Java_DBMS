/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class AlterTable extends Stm {

	/**
	 * Table name
	 */
	public Symbol.Symbol name;
	
	/**
	 * Add or Drop
	 * 		1 = add
	 * 		2 = drop
	 */
	public int AddOrDrop;
	
	/**
	 * Add attribute list
	 */
	public AttributeList add_list;
	
	/**
	 * Drop attribute list
	 */
	public IdList drop_list;
	
	/**
	 * 
	 */
	public AlterTable(int p, Symbol.Symbol n, int aod, AttributeList al, IdList dl) {
		pos = p;
		name = n;
		AddOrDrop = aod;
		add_list = al;
		drop_list = dl;
	}

	public String toString() {
		return "AlterTable "+name.toString();
	}
}
