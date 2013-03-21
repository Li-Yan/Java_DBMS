package Exec_Tree;

import Constant.Const_For_Exec.Exec_CompareType;

public class Exec_Condition_Compare extends Exec_Condition {
	/*
	 * compareFormation:
	 * 
	 * 1: exp compare_op exp
	 * 2: exp compare_op LPAREN query RPAREN
	 * 3: LPAREN query RPAREN compare_op exp
	 * 4: LPAREN query RPAREN compare_op LPAREN query RPAREN
	 * 
	 */
	public Exec_Item leftItem;
	public Exec_Item rightItem;
	public Exec_CompareType compareType;
	public int compareFormation;
	
	public Exec_Condition_Compare(Exec_Item Left, Exec_Item Right, 
			Exec_CompareType Type, int Formation) {
		leftItem = Left;
		rightItem = Right;
		compareType = Type;
		compareFormation = Formation;
	}
}
