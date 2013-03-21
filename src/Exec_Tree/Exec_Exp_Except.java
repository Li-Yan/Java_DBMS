package Exec_Tree;

public class Exec_Exp_Except extends Exec_Exp {
	public Exec_Exp leftExp;
	public Exec_Exp rightExp;
	public boolean isAll;
	
	public Exec_Exp_Except(Exec_Exp Left, Exec_Exp Right, boolean IsALL) {
		leftExp = Left;
		rightExp = Right;
		isAll = IsALL;
	}
}
