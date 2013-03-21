package Exec_Tree;

public class Exec_Exp_Group extends Exec_Exp{
	public Exec_ItemList group_itemList;
	public Exec_Exp exp;
	
	public Exec_Exp_Group(Exec_ItemList IL, Exec_Exp Exp) {
		group_itemList = IL;
		exp = Exp;
	}
}
