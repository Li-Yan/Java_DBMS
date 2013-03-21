package Exec_Tree;

import java.util.ArrayList;

public class Exec_Exp_Rename extends Exec_Exp {
	public ArrayList<String> oldNameList;
	public ArrayList<String> newNameList;
	public Exec_Exp exp;
	
	public Exec_Exp_Rename(ArrayList<String> ONL, ArrayList<String> NNL, Exec_Exp Exp) {
		oldNameList = ONL;
		newNameList = NNL;
		exp = Exp;
	}
}
