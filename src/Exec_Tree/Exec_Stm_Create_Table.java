package Exec_Tree;

public class Exec_Stm_Create_Table extends Exec_Stm_Create {
	public String tablename;
	public Exec_ItemList fieldList;
	
	public Exec_Stm_Create_Table(String Name, Exec_ItemList Fl) {
		tablename = Name;
		fieldList = Fl;
	}
}
