package Exec_Tree;

public class Exec_Item_Field_Agg extends Exec_Item_Field {
	public String aggOp;
	
	public Exec_Item_Field_Agg(String name, String Op) {
		super(name);
		aggOp = Op;
	}
	
}
