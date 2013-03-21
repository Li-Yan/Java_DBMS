package Exec_Tree;

public class Exec_Stm_Delete extends Exec_Stm {
	public String tableName;
	public Exec_Condition condition;
	
	public Exec_Stm_Delete(String Name, Exec_Condition con) {
		tableName = Name;
		condition = con;
	}
}
