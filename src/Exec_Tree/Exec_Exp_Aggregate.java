package Exec_Tree;

import java.util.ArrayList;

public class Exec_Exp_Aggregate extends Exec_Exp {
	public Exec_ItemList aggregate_fieldList;
	public Exec_ItemList group_fieldList;
	public Exec_Exp exp;
	public ArrayList<String> aggregate_operationList;
	
	public Exec_Exp_Aggregate(Exec_ItemList AFL, Exec_ItemList GFL, 
			Exec_Exp Exp, ArrayList<String> List) {
		aggregate_fieldList = AFL;
		exp = Exp;
		aggregate_operationList = List;
	}
}
