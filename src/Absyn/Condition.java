/**
 * 
 */
package Absyn;


/**
 * @author Kiki
 *
 */
public class Condition extends Absyn {

	/**
	 * Condition type:
	 * 	0:	default type
	 *	1:	exp compare_op exp
	 *			use		exp1, compare_op, exp2
	 *	2:	exp compare_op LPAREN query RPAREN
	 *			use		exp1, compare_op, query1
	 *	3:	LPAREN query RPAREN compare_op exp
	 *			use		query1, compare_op, exp1
	 *	4:	LPAREN query RPAREN compare_op LPAREN query RPAREN
	 *			use		query1, compare_op, query2
	 *	5:	exp compare_op ANY LPAREN query RPAREN
	 *			use		exp1, compare_op, query1
	 *	6:	exp compare_op ALL LPAREN query RPAREN
	 *			use		exp1, compare_op, query1
	 *	7:	exp_list IN LPAREN query RPAREN
	 *			use		exp1, query1
	 *	8:	exp_list NOT IN LPAREN query RPAREN
	 *			use		exp1, query1
	 *	9:	exp IS NULL
	 *			use		exp1
	 *	10:	exp IS NOT NULL
	 *			use		exp1
	 *	11:	exp BETWEEN exp AND exp
	 *			use		exp1, exp2, exp3
	 */
	public int type = 0;
	
	/**
	 * Parse.sym.*;
	 */
	public int compareop;
	public ExpList exp_list;
	public Exp exp1, exp2, exp3;
	public Query query1, query2;

	/**
	 * 
	 */
	public Condition(int p, int t, int o, Exp e1, Exp e2,
			Exp e3, Query q1, Query q2) {
		pos = p;
		type = t;
		compareop = o;
		exp1 = e1;
		exp2 = e2;
		exp3 = e3;
		query1 = q1;
		query2 = q2;
	}
	public Condition(int p, int t, int o, ExpList e1, Exp e2,
			Exp e3, Query q1, Query q2) {
		pos = p;
		type = t;
		compareop = o;
		exp_list = e1;
		exp2 = e2;
		exp3 = e3;
		query1 = q1;
		query2 = q2;
	}
	
	public String toString() {
		String ret = "(";
		switch(type) {
		case 1:
			return exp1.toString() + getOpString() + exp2.toString();
		case 2:
			return exp1.toString() + getOpString() + "(" + query1.toString()+ ")";
		case 3:
			return "("+query1.toString()+")" + getOpString() + exp1.toString();
		case 4:
			return "("+query1.toString()+")" + getOpString() +  "(" + query1.toString()+ ")";
		case 5:
			return exp1.toString() + getOpString() + " ANY (" + query1.toString()+ ")";
		case 6:
			return exp1.toString() + getOpString() + " ALL (" + query1.toString()+ ")";
		case 7:
			return "("+ exp_list.toString() + ") IN ("+ query1.toString() + ")";
		case 8:
			return "("+ exp_list.toString() + ") NOT IN ("+ query1.toString() + ")";
		case 9:
			return exp1.toString() + "IS NULL ";
		case 10:
			return exp1.toString() + "IS NOT NULL ";
		case 11:
			return exp1.toString() + " BETWEEN " + exp2.toString() + " AND " + exp3.toString();
		}
		ret += ")";
		return ret;
	}
	
	public String getOpString() {
		String ret = "";
		switch(compareop) {
		case Parse.sym.EQ:
			return " = ";
		case Parse.sym.NEQ:
			return " <> ";
		case Parse.sym.GT:
			return " > ";
		case Parse.sym.GE:
			return " >= ";
		case Parse.sym.LT:
			return " < ";
		case Parse.sym.LE:
			return " <= ";
		}
		return ret;
	}
}
