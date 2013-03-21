package Exec_Tree;

import Constant.Const_For_Exec.Exec_Item_OpType;

public class Exec_Item_Op extends Exec_Item {
	public Exec_Item leftItem;
	public Exec_Item rightItem;
	public Exec_Item_OpType OpType;
	
	public Exec_Item_Op(Exec_Item Left, Exec_Item Right, Exec_Item_OpType Type) {
		leftItem = Left;
		rightItem = Right;
		OpType = Type;
	}

}
