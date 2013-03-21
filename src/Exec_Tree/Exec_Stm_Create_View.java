package Exec_Tree;

public class Exec_Stm_Create_View extends Exec_Stm_Create {
	public String viewname;
	public String description;
	
	public Exec_Stm_Create_View(String Name, String Description) {
		viewname = Name;
		description = Description;
	}
}
