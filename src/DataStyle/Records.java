package DataStyle;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import qifeng.db.DbTable;
import qifeng.schema.Schema;

import Tools.Tools;

public class Records {
	private Metadata metadata;
	private Data data;
	private Schema sc;
	private ArrayList<String> SudoNameList = null;
	
	public Records() {
		metadata = new Metadata();
		data = new Data();
	}
	
	public void setSchema(Schema s) {
		sc = s;
	}
	public final Metadata Get_Metadata() {return metadata;}
//	public final Data Get_Data() {return data;}
	
	public void Add_Attribute(String Name, String Type) {
		metadata.Add_Attribute(Name, Type);
	}
	
	public void Add_Attribute(String Name, String Type, String Prop) {
		metadata.Add_Attribute(Name, Type, Prop);
	}
	
	public void Add_Tuple(Tuple T) {
		data.Add_Tuple(T);
		metadata.rowNum++;
	}
	
	public String Get_AttributeName(int index) {
		return metadata.Get_AttributeName(index);
	}
	
	public String Get_AttributeType(int index) {
		return metadata.Get_AttributeType(index);
	}
	
	public String Get_AttributeProperty(int index) {
		return metadata.Get_AttributeProperty(index);
	}
	
	public String Get_AttributeValue(int rowIndex, int columnIndex) {
		return this.data.Get_Tuple(rowIndex).Get_AttributeValue(columnIndex);
	}
	
	public String Get_AttributeValue(int rowIndex, String attributeName) {
		int columnIndex = this.IndexOf_AttributeName(attributeName);
		return this.Get_AttributeValue(rowIndex, columnIndex);
	}
	
	public String Get_AttributeValue(Tuple T, int columnIndex) {
		return T.Get_AttributeValue(columnIndex);
	}
	
	public String Get_AttributeValue(Tuple T, String attributeName) {
		int columnIndex = this.IndexOf_AttributeName(attributeName);
		return T.Get_AttributeValue(columnIndex);
	}
	
	public final Tuple Get_Tuple(int index) {
		return data.Get_Tuple(index);
	}
	
	public final void Set_AttributeName(int index, String name) {
		metadata.Set_AttributeName(index, name);
	}
	
	public final void Set_AttributeProperty(int index, String property) {
		metadata.Set_AttributeProperty(index, property);
	}
	
	public int Get_ColumnNum() {
		return metadata.Get_ColumnNum();
	}
		
	public int Get_RowNum() {
		return metadata.Get_RowNum();
	}
	
	public int IndexOf_AttributeName(String Name) {
		return metadata.attributeList.indexOf(Name);
	}
	
	public void Copy_Metadata(Records R) {
		int columnNum = R.Get_ColumnNum();
		
		metadata.Clear();
		this.SudoNameList = new ArrayList<String>();
		
		for (int i = 0; i < columnNum; i++) {
			this.Add_Attribute(R.Get_AttributeName(i), R.Get_AttributeType(i));
			this.metadata.Set_AttributeProperty(i, R.Get_AttributeProperty(i));
			this.SudoNameList.add(R.Get_SudoName(i));
		}
	}
	
	public int ReadMetadata(String filename) {
		ArrayList<String> meta = new ArrayList<String>();
		try {
			java.io.InputStream inp = new java.io.FileInputStream(filename);
			int ret = -2;
			StringBuilder sb = new StringBuilder();
			while ((ret = inp.read()) != -1) {
				if (ret == '\r') {
					meta.add(sb.toString());
					ret = inp.read();
					sb = new StringBuilder();
					continue;
				}
				sb.append((char) ret);
			}
			inp.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String meta1 = meta.get(0);
		String[] str = meta1.split("#");
		int colnum = Integer.parseInt(str[0]);
		int rownum = Integer.parseInt(str[1]);
		metadata.Set_RowNum(rownum);
		int pos = 2;
		for (int i = 0; i < colnum; i++) {
			String id = str[pos++];
			String type = str[pos++];
			metadata.Add_Attribute(id, type);
		}
		return 1;
	}
	public int ReadData(String filename) {
		ArrayList<String> meta = new ArrayList<String>();
		try {
			java.io.InputStream inp = new java.io.FileInputStream(filename);
			int ret = -2;
			StringBuilder sb = new StringBuilder();
			while ((ret = inp.read()) != -1) {
				if (ret == '\r') {
					meta.add(sb.toString());
					ret = inp.read();
					sb = new StringBuilder();
					continue;
				}
				sb.append((char) ret);
			}
			inp.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < meta.size(); i++) {
			String data1 = meta.get(i);
			String[] str = data1.split("#");
			Tuple t = new Tuple();
			for (int j = 0; j < str.length; j++)
				t.Add_AttributeValue(str[j]);
			data.Add_Tuple(t);
		}
		return 1;
	}
	public int WriteMetadata(String filename) {
		try {
			PrintStream out = new PrintStream(new FileOutputStream(filename), true);
			out.print(metadata.columnNum + "#" + metadata.rowNum + "#");
			for (int i = 0; i < metadata.columnNum; i++) {
				out.print(metadata.Get_AttributeName(i) + "#" + metadata.Get_AttributeType(i) + "#");
			}
			out.println();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return 1;
	}
	public int WriteData(String filename) {
		try {
			PrintStream out = new PrintStream(new FileOutputStream(filename), true);
			for (int i = 0; i < data.tupleList.size(); i++) {
				for (int j = 0; j < metadata.columnNum; j++) {
					out.print(data.tupleList.get(i).attribute.get(j) + "#");
				}
				out.println();
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return 1;
	}
	public String toString() {
		ArrayList<Integer> lenList = new ArrayList<Integer>();
		for (int i = 0; i < metadata.columnNum; i++) {
			lenList.add(metadata.Get_AttributeName(i).length() + 2);
		}
		for (int i = 0; i < data.tupleList.size(); i++) {
			Tuple t = data.tupleList.get(i);
			for (int j = 0; j < metadata.columnNum; j++) {
				int l = t.attribute.get(j).length() + 2;
				if (l > lenList.get(j))
					lenList.set(j, l);
			}
		}
		String ret = "+";
		for (int i = 0; i < lenList.size(); i++) {
			int l = lenList.get(i);
			for (int j = 0; j < l; j++)
				ret += "-";
			ret += "+";
		}
		ret += "\n|";
		for (int i = 0; i < lenList.size(); i++) {
			ret += FillBlank(lenList.get(i), metadata.Get_AttributeName(i));
			ret += "|";
		}
		ret += "\n+";
		for (int i = 0; i < lenList.size(); i++) {
			int l = lenList.get(i);
			for (int j = 0; j < l; j++)
				ret += "-";
			ret += "+";
		}
		ret += "\n";
		for (int i = 0; i < data.tupleList.size(); i++) {
			Tuple t = data.tupleList.get(i);
			ret += "|";
			for (int j = 0; j < metadata.columnNum; j++) {
				String tmp = t.Get_AttributeValue(j);
				if (Tools.String_to_Type(tmp) == 
					Constant.Const_For_Exec.Exec_Item_ConstType.Int) {
					ret += FillBlank(lenList.get(j), 
							(new Integer((int)Double.parseDouble(tmp)).toString()));
					ret += "|";
					continue;
				}
				if (tmp == null || tmp.equals("")) {
					ret += FillBlank(lenList.get(j), "NULL");
					ret += "|";
					continue;
				}
				ret += FillBlank(lenList.get(j), String.valueOf(tmp));
				ret += "|";
			}
			ret += "\n";
		}
		ret += "+";
		for (int i = 0; i < lenList.size(); i++) {
			int l = lenList.get(i);
			for (int j = 0; j < l; j++)
				ret += "-";
			ret += "+";
		}
		ret += "\n";
		return ret;
	}
	private String FillBlank(int size, String s) {
		String ret = " ";
		int l = size - 2;
		if (s.length() >= l)
			ret += s.substring(0, l);
		else {
			for (int i = 0; i < l - s.length(); i++)
				ret += " ";
			ret += s;
		}
		ret += " ";
		return ret;
	}
	
	public void Set_SudoNameList(ArrayList<String> list) {
		this.SudoNameList = list;
	}
	
	public String Get_SudoName(int index) {
		return this.SudoNameList.get(index);
	}
}
