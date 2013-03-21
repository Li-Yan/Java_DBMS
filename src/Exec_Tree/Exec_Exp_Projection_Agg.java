 package Exec_Tree;

import java.util.ArrayList;

public class Exec_Exp_Projection_Agg extends Exec_Exp {
	public ArrayList<Exec_Item> itemList;
	public ArrayList<String> aggOpList;
	public Exec_ItemList group_itemList;
	public ArrayList<String> newNameList;
	public Exec_Exp exp;
	
	public Exec_Exp_Projection_Agg(ArrayList<Exec_Item> iL, ArrayList<String> aL,
			Exec_ItemList gL, ArrayList<String> nL, Exec_Exp Exp) {
		itemList = iL;
		aggOpList = aL;
		group_itemList = gL;
		newNameList = nL;
		exp = Exp;
	}
}
