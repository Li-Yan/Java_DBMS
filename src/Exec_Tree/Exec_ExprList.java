package Exec_Tree;

public class Exec_ExprList {
	public Exec_Expr head;
	public Exec_ExprList tail;
	
	public Exec_ExprList() {
		head = null;
		tail = null;
	}
	
	public Exec_ExprList(Exec_Expr Head, Exec_ExprList Tail) {
		head = Head;
		tail = Tail;
	}
	
	public void Add_Expr(Exec_Expr expr) {
		if (head == null) {
			head = expr;
		}
		else {
			Exec_ExprList newExecExprList = new Exec_ExprList(expr, null);
			this.tail = newExecExprList;
		}
	}
}
