package Exec_Tree;

public class Exec_Item_FieldDotField_Agg extends Exec_Item_FieldDotField {
	public String aggOp;
	
	public Exec_Item_FieldDotField_Agg(String BN, String AN, String Op) {
		super(BN, AN);
		aggOp = Op;
	}
}

