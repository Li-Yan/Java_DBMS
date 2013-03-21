package Exec_Tree;

public class Exec_Exp_Selection extends Exec_Exp{
	public Exec_Condition condition;
	public Exec_Exp exp;
	
	public Exec_Exp_Selection(Exec_Condition con, Exec_Exp Exp) {
		condition = con;
		exp = Exp;
	}
	
}
