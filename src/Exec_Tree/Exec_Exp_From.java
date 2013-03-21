package Exec_Tree;

import java.util.ArrayList;

public class Exec_Exp_From extends Exec_Exp{
	public ArrayList<Exec_Exp_Records> recordsList;
	public ArrayList<String> newNameList;
	
	
	public Exec_Exp_From(ArrayList<Exec_Exp_Records> RL, ArrayList<String> NL) {
		recordsList = RL;
		newNameList = NL;
	}
}