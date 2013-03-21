/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class SetQuery extends Query {

	/**
	 * Set operate type:
	 * 	1:	UNION
	 * 	2:	UNION ALL
	 * 	3:	INTERSECT
	 * 	4:	INTERSECT ALL
	 * 	5:	EXCEPT
	 * 	6:	EXCEPT ALL
	 */
	public int type;
	public Query query1, query2;
	/**
	 * 
	 */
	public SetQuery(int p, int t, Query q1, Query q2) {
		super(p);
		type = t;
		query1 = q1;
		query2 = q2;
	}

}
