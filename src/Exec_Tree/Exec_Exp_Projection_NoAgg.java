package Exec_Tree;

import java.util.ArrayList;

public class Exec_Exp_Projection_NoAgg extends Exec_Exp{
	public ArrayList<Exec_Item> itemList;
	public Exec_ItemList group_itemList;
	public ArrayList<String> newNameList;
	public Exec_Exp exp;
	
	public Exec_Exp_Projection_NoAgg(ArrayList<Exec_Item> il, Exec_ItemList gl,
			ArrayList<String> NN, Exec_Exp Exp) {
		itemList = il;
		group_itemList = gl;
		newNameList = NN;
		exp = Exp;
	}
}
