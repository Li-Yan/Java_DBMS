package Exec_Tree;

public class Exec_Condition_IN extends Exec_Condition {
	public Exec_ItemList itemList;
	public Exec_Exp exp;
	
	public Exec_Condition_IN(Exec_ItemList IL, Exec_Exp Exp) {
		itemList = IL;
		exp = Exp;
	}
}
