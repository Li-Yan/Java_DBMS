package DataStyle;

import java.util.ArrayList;

public class Data {
	public ArrayList<Tuple> tupleList;
	
	public Data() {
		tupleList = new ArrayList<Tuple>();
	}
	
	public void Add_Tuple(Tuple T) {
		tupleList.add(T);
	}
	
	public Tuple Get_Tuple(int index) {
		return tupleList.get(index);
	}
	
	public void Set_Tuple(int index, Tuple T) {
		tupleList.set(index, T);
	}
}
