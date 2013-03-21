package Exec_Tree;

public class Exec_Item_FieldDotField extends Exec_Item {
	public String beforeDotName;
	public String afterDotName;
	
	public Exec_Item_FieldDotField(String BN, String AN) {
		beforeDotName = BN;
		afterDotName = AN;
	}
	
	public String Real_Name() {
		return (beforeDotName + "." + afterDotName);
	}
	
	public String toString() {
		return (beforeDotName + "." + afterDotName);
	}
}
