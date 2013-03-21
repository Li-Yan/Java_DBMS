package Exec_Tree;

import java.util.ArrayList;

public class Exec_Exp_OrderBy extends Exec_Exp {
	/**
	 * Whether or not is Descend
	 * 
	 * IsDown == true -> down
	 * IsDown == false -> up
	 */
	
	public ArrayList<Exec_Item> itemList;
	public ArrayList<Boolean> isDownList;
	public Exec_Exp exp;
	
	public Exec_Exp_OrderBy(ArrayList<Exec_Item> il, ArrayList<Boolean> mode, Exec_Exp Exp) {
		itemList = il;
		isDownList = mode;
		exp = Exp;
	}
}
