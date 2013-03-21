package Exec_Tree;

import Constant.Const_For_Exec.Exec_Item_ConstType;

public class Exec_Item_Const extends Exec_Item {
	public String value;
	public Exec_Item_ConstType type;
	
	public Exec_Item_Const(String Value, Exec_Item_ConstType Type) {
		value = Value;
		type = Type;
	}
}
