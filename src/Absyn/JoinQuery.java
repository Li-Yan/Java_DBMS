/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class JoinQuery extends Query {

	/**
	 * Join type:
	 * 	1:	NATURAL JOIN
	 * 	2:	FULL JOIN
	 * 	3:	CROSS JOIN
	 * 	4:	NATURAL LEFT JOIN
	 * 	5:	NATURAL RIGHT JOIN
	 */
	public int type;
	public Boolean isDistinct;
	public FieldList field_list;
	public Symbol.Symbol table1;
	public Symbol.Symbol table2;
	public OnPart on;
	
	/**
	 * 
	 */
	public JoinQuery(int p, int t, Boolean isDis, FieldList fl, Symbol.Symbol t1, 
			Symbol.Symbol t2, OnPart o) {
		super(p);
		type = t;
		isDistinct = isDis;
		field_list = fl;
		table1 = t1;
		table2 = t2;
		on = o;
	}

	/**
	 * 	|	SELECT:s distinct_part:d field_list:f FROM ID:i1 NATURAL JOIN ID:i2 on_part:o
	|	SELECT:s distinct_part:d field_list:f FROM ID:i1 FULL JOIN ID:i2 on_part:o
	|	SELECT:s distinct_part:d field_list:f FROM ID:i1 CROSS JOIN ID:i2 on_part:o
	|	SELECT:s distinct_part:d field_list:f FROM ID:i1 NATURAL LEFT JOIN ID:i2 on_part:o
	|	SELECT:s distinct_part:d field_list:f FROM ID:i1 NATURAL RIGHT JOIN ID:i2 on_part:o
	 */
	public String toString() {
		String ret = "SELECT ";
		if (isDistinct)
			ret += "DISTINCT ";
		ret += field_list.toString();
		ret += " FROM " + table1.toString();
		switch(type) {
		case 1:	
			ret += " NATURAL JOIN ";	break;
		case 2:
			ret += " FULL JOIN ";	break;
		case 3:
			ret += " CROSS JOIN ";	break;
		case 4:	
			ret += " NATURAL LEFT JOIN ";	break;
		case 5:
			ret += " NATURAL RIGHT JOIN ";	break;
		}
		ret += table2.toString() + " ";
		ret += on.toString();
		return ret;
	}
}
