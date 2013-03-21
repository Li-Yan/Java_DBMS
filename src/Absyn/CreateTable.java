/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class CreateTable extends Stm {
	
	/**
	 * Table name
	 */
	public Symbol.Symbol name;
	
	/**
	 * Attribute list
	 */
	public AttributeList attr_list;
	
	/**
	 * Constructor
	 */
	public CreateTable(int p, Symbol.Symbol n, AttributeList al) {
		pos = p;
		name = n;
		attr_list = al;
	}
	
	public String toString() {
		return name.toString();
	}
}
