package Exec_Tree;

import Constant.Const_For_Exec.Exec_CompareType;

public class Exec_Condition_ALL extends Exec_Condition {
	public Exec_Item item;
	public Exec_Exp exp;
	public Exec_CompareType compareType;
	
	public Exec_Condition_ALL(Exec_Item Item, Exec_Exp Exp, Exec_CompareType Type) {
		item = Item;
		exp = Exp;
		compareType = Type;
	}
}
