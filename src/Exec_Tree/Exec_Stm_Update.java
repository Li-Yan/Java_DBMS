package Exec_Tree;

import Absyn.Const;

public class Exec_Stm_Update extends Exec_Stm {
	public String tableName;
	public String newTableName;
	public Exec_Item field;
	public Const newValue;
	public Exec_Condition condition;
	
	public Exec_Stm_Update(String Name, String newName, Exec_Item item, 
			Const Value, Exec_Condition con) {
		tableName = Name;
		newTableName = newName;
		field = item;
		newValue = Value;
		condition = con;
	}
}
