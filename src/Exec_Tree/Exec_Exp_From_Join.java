package Exec_Tree;

public class Exec_Exp_From_Join extends Exec_Exp {
	/**
	 * Join type:
	 * 	1:	NATURAL JOIN
	 * 	2:	FULL JOIN
	 * 	3:	CROSS JOIN
	 * 	4:	NATURAL LEFT JOIN
	 * 	5:	NATURAL RIGHT JOIN
	 */
	public Exec_Exp_Records table1;
	public Exec_Exp_Records table2;
	public int joinType;
	
	public Exec_Exp_From_Join(Exec_Exp_Records T1, Exec_Exp_Records T2, int type) {
		table1 = T1;
		table2 = T2;
		joinType = type;
	}
	
}
