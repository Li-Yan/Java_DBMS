/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class Attribute extends Absyn {

	/**
	 * Attribute's name
	 */
	public Symbol.Symbol name;
	
	/**
	 * Attribute's type
	 */
	public Type type;
	
	/**
	 * The attribute is Primary Key or not
	 */
	public Boolean isPrimaryKey;
	
	/**
	 * is NOT NULL or not
	 */
	public Boolean isNotNull;
	/**
	 * Default value;
	 */
	public Const defaultvalue;
	
	/**
	 * eg: Primary Key (a)
	 */
	public Boolean isSetKey;
	
	/**
	 * set key list
	 */
	public IdList id_list;
	
	/**
	 * 
	 */
	public Attribute(int p, Symbol.Symbol n, Type t, Boolean isPK, 
			Boolean isNN, Const v, Boolean isSK, IdList il) {
		pos = p;
		name = n;
		type = t;
		isPrimaryKey = isPK;
		isNotNull = isNN;
		defaultvalue = v;
		isSetKey = isSK;
		id_list = il;
	}

	public String toString() {
		return "Attribute "+name.toString();
	}
}
