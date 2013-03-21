package DataStyle;

import java.util.ArrayList;

public class Tuple {
	public ArrayList<String> attribute;
	
	public Tuple() {
		attribute = new ArrayList<String>();
	}
	
	public void Add_AttributeValue(String Attr) {
		attribute.add(Attr);
	}
	
	public void Set_AttributeValue(int index, String Attr) {
		attribute.set(index, Attr);
	}
	
	public String Get_AttributeValue(int index) {
		return attribute.get(index);
	}
	
	public void Copy(Tuple T) {
		this.attribute.clear();
		int size = T.attribute.size();
		for (int i = 0;i < size; i++) this.attribute.add(T.Get_AttributeValue(i));
	}
}
