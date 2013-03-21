package Exec_Tree;

public class Exec_Exp_Having extends Exec_Exp {
	public Exec_Condition condition;
	public Exec_ItemList group_itemList;
	public Exec_Exp exp;
	
	public Exec_Exp_Having(Exec_Condition con, Exec_ItemList gL, Exec_Exp Exp) {
		condition = con;
		group_itemList = gL;
		exp = Exp;
	}
}
