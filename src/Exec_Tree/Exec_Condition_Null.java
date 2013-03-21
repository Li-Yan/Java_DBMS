package Exec_Tree;

public class Exec_Condition_Null extends Exec_Condition {
	/*
	 * Null_Or_Not:
	 * true : is null
	 * false : is not null
	 */
	public Exec_Item item;
	public boolean Null_Or_Not;
	
	public Exec_Condition_Null(Exec_Item Item, boolean boo) {
		item = Item;
		Null_Or_Not = boo;
	}
}
