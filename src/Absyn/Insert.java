/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class Insert extends Stm {

	/**
	 * Which table to insert into
	 */
	public Symbol.Symbol name;

	/**
	 * Insert Statement type:
	 * 	1:	INSERT INTO ID VALUES values_part
	 * 	2:	INSERT INTO ID query
	 * 	3:	INSERT INTO ID LPAREN id_list RPAREN VALUES values_part
	 * 	4:	INSERT INTO ID LPAREN id_list RPAREN query
	 */
	public int type;
	
	public ValuesPart values;
	
	public IdList attr_list;
	
	public Query query;
	
	/**
	 * 
	 */
	public Insert(int p, Symbol.Symbol n, int t, ValuesPart v, IdList a, Query q) {
		pos = p;
		type = t;
		name = n;
		values = v;
		attr_list = a;
		query = q;
	}

	public String toString() {
		return name.toString();
	}
}
