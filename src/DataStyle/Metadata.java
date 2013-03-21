package DataStyle;

import java.util.ArrayList;

public class Metadata {
	public int columnNum;
	public int rowNum;
	public ArrayList<String> attributeList;
	public ArrayList<String> typeList;
	public ArrayList<String> propertyList;
	
	public Metadata() {
		columnNum = 0;
		rowNum = 0;
		attributeList = new ArrayList<String>();
		typeList = new ArrayList<String>();
		propertyList = new ArrayList<String>();
	}
	
	public void Add_Attribute(String Name, String Type) {
		attributeList.add(Name);
		typeList.add(Type);
		propertyList.add("");
		columnNum++;
	}
	
	public void Add_Attribute(String Name, String Type, String Prop) {
		attributeList.add(Name);
		typeList.add(Type);
		propertyList.add(Prop);
		columnNum++;
	}
	
	public int Get_ColumnNum() {return columnNum;}
	
	public int Get_RowNum() {return rowNum;}
	
	public String Get_AttributeName(int index) {return attributeList.get(index);}
	
	public String Get_AttributeType(int index) {return typeList.get(index);}
	
	public String Get_AttributeProperty(int index) {return propertyList.get(index);}
	
	public void Set_AttributeName(int index, String name) {
		attributeList.set(index, name);
	}
	
	public void Set_AttributeProperty(int index, String property) {
		propertyList.set(index, property);
	}
	
	public void Add_RowNum() {rowNum++;}
	
	public void Reduce_RowNum() {rowNum--;}
	
	public void Set_RowNum(int Num) {rowNum = Num;}
	
	public void Clear() {
		columnNum = 0;
		rowNum = 0;
		attributeList.clear();
		typeList.clear();
		propertyList.clear();
	}
}
