package Tools;

import java.util.ArrayList;

import DataStyle.*;
import Exec_Tree.*;
import Constant.Const_For_Exec.*;

public class Tools {
	
	public static Records Do_Join(Records R1, String Name1, Records R2, String Name2) {
		String prefix1 = "", prefix2 = "";
		
		if (Name1 != "") prefix1 = Name1 + ".";
		if (Name2 != "") prefix2 = Name2 + ".";
		
		Records newRecords = new Records();
		
		int columnNum1 = R1.Get_ColumnNum(), columnNum2 = R2.Get_ColumnNum();
		int rowNum1 = R1.Get_RowNum(), rowNum2 = R2.Get_RowNum();
		
		// for megadata
		for (int i = 0; i < columnNum1; i++) {
			newRecords.Add_Attribute(prefix1 + R1.Get_AttributeName(i),
					R1.Get_AttributeType(i));
		}
		for (int i = 0; i < columnNum2; i++) {
			newRecords.Add_Attribute(prefix2 + R2.Get_AttributeName(i),
					R2.Get_AttributeType(i));
		}
		
		// for data
		for (int i = 0; i < rowNum1; i++) {
			Tuple R1Tuple = R1.Get_Tuple(i);
			for (int j = 0; j < rowNum2; j++) {
				Tuple newTuple = new Tuple();
				for (int k = 0; k < columnNum1; k++) {
					newTuple.Add_AttributeValue(R1Tuple.Get_AttributeValue(k));
				}
				Tuple R2Tuple = R2.Get_Tuple(j);
				for (int k = 0; k < columnNum2; k++) {
					newTuple.Add_AttributeValue(R2Tuple.Get_AttributeValue(k));
				}
				newRecords.Add_Tuple(newTuple);
			}
		}
		
		return newRecords;
	}
	
	public static void Do_Check_Temp_AttributeName(Records R) {
		int columnNum = R.Get_ColumnNum();
		for (int i = 0; i < columnNum; i++) {
			boolean IsUnique = true;
			String Checking_Name = Name_Behind_Point(R.Get_AttributeName(i));
			for (int j = 0; j < columnNum; j++) {
				if (i != j) {
					if (Checking_Name.equals(Name_Behind_Point(R.Get_AttributeName(j)))) {
						IsUnique = false;
						break;
					}
				}
				if (!IsUnique) break;
			}
			if (IsUnique) {
				R.Set_AttributeName(i, Checking_Name);
			}
		}
	}
	
	public static String Name_Behind_Point(String S) {
		int pos = S.indexOf(".");
		if (pos == -1) return S;
		else return S.substring(pos+1);
	}
	
	public static Boolean Check_Condition(Exec_CompareType Type, 
			Exec_Item_Const Left, Exec_Item_Const Right) {
		if ((Left.value == null) || (Right.value == null)) return false;
		
		if ((Left.type == Exec_Item_ConstType.String) && (Right.type != Exec_Item_ConstType.String)) {
			// error
			System.out.println("Type does not match!");
			return null;
		}
		else if ((Left.type != Exec_Item_ConstType.String) && (Right.type == Exec_Item_ConstType.String)) {
			// error
			System.out.println("Type does not match!");
			return null;
		}
		if ((Left.type == Exec_Item_ConstType.Boolean) && (Right.type != Exec_Item_ConstType.Boolean)) {
			// error
			System.out.println("Type does not match!");
			return null;
		}
		else if ((Left.type != Exec_Item_ConstType.Boolean) && (Right.type == Exec_Item_ConstType.Boolean)) {
			// error
			System.out.println("Type does not match!");
			return null;
		}
		
		if (Type == Exec_CompareType.EQ) {
			if (Left.type == Exec_Item_ConstType.String) {
				return (Left.value.equals(Right.value));
			}
			else if (Left.type == Exec_Item_ConstType.Boolean) {
				return (Boolean.parseBoolean(Left.value) == Boolean.parseBoolean(Right.value));
			}
			else {
				return (Double.parseDouble(Left.value) == Double.parseDouble(Right.value));
			}
		}
		else if (Type == Exec_CompareType.NEQ) {
			if (Left.type == Exec_Item_ConstType.String) {
				return (!Left.value.equals(Right.value));
			}
			else if (Left.type == Exec_Item_ConstType.Boolean) {
				return (Boolean.parseBoolean(Left.value) != Boolean.parseBoolean(Right.value));
			}
			else {
				return (Double.parseDouble(Left.value) != Double.parseDouble(Right.value));
			}
		}
		else {
			if (Left.type == Exec_Item_ConstType.String) {
				if (Type == Exec_CompareType.GT) return Left.value.compareTo(Right.value) > 0;
				else if (Type == Exec_CompareType.GE) return Left.value.compareTo(Right.value) >= 0;
				else if (Type == Exec_CompareType.LT) return Left.value.compareTo(Right.value) < 0;
				else if (Type == Exec_CompareType.LE) return Left.value.compareTo(Right.value) <= 0;
				else {
					return false;
				}
			}
			double dl = Double.parseDouble(Left.value);
			double dr = Double.parseDouble(Right.value);
			if (Type == Exec_CompareType.GT) return dl > dr;
			else if (Type == Exec_CompareType.GE) return dl >= dr;
			else if (Type == Exec_CompareType.LT) return dl < dr;
			else if (Type == Exec_CompareType.LE) return dl <= dr;
			else {
				return false;
			}
		}
	}
	
	public static boolean NatureJointMatch(Tuple T1, Metadata M1, Tuple T2, Metadata M2) {
		int columnNum1 = M1.columnNum;
		int columnNum2 = M2.columnNum;
		
		for (int i1 = 0; i1 < columnNum1; i1++) {
			for (int i2 = 0; i2 < columnNum2; i2++) {
				if (M1.Get_AttributeName(i1).equals(M2.Get_AttributeName(i2))) {
					if (String_to_Type(M1.Get_AttributeType(i1)) !=
						String_to_Type(M2.Get_AttributeType(i2))) {
						// error
						System.out.println("Cannot do nature join, same name different type!");
						return false;
					}
					
					Exec_Item_Const item1 = new Exec_Item_Const(T1.Get_AttributeValue(i1), 
							Tools.String_to_Type(M1.Get_AttributeType(i1)));
					Exec_Item_Const item2 = new Exec_Item_Const(T2.Get_AttributeValue(i2), 
							Tools.String_to_Type(M1.Get_AttributeType(i2)));
					if (!Check_Condition(Exec_CompareType.EQ, item1, item2)) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	public static Exec_Item_Const Do_Item_Op(Exec_Item_Const Left, Exec_Item_Const Right, 
			Exec_Item_OpType OpType) {
		
		Exec_Item_ConstType newType;
		
		if (Left.type == Exec_Item_ConstType.Double) {
			if ((Right.type != Exec_Item_ConstType.Double) && (Right.type != Exec_Item_ConstType.Int)) {
				// error
				System.out.println("Type does not math!");
				return null;
			}
			
			newType = Exec_Item_ConstType.Double;
		}
		else if (Left.type == Exec_Item_ConstType.Int) {
			if (Right.type == Exec_Item_ConstType.Double) {
				newType = Exec_Item_ConstType.Double;
			}
			else if (Right.type == Exec_Item_ConstType.Int) {
				newType = Exec_Item_ConstType.Int;
			}
			else {
				// error
				System.out.println("Type does not math!");
				return null;
			}
		}
		else if ((Left.type == Exec_Item_ConstType.Boolean) && (Right.type == Exec_Item_ConstType.Boolean)) {
			newType = Exec_Item_ConstType.Boolean;
		}
		else if ((Left.type == Exec_Item_ConstType.String) && (Right.type == Exec_Item_ConstType.String)) {
			newType = Exec_Item_ConstType.String;
		}
		else {
			// error
			System.out.println("Type does not math!");
			return null;
		}
		
		if ((Left.value == null) || (Right.value == null)) return new Exec_Item_Const(null, newType);
		
		if (OpType == Exec_Item_OpType.Plus) {
			if (newType == Exec_Item_ConstType.Double) {
				return new Exec_Item_Const(
						String.valueOf(Double.parseDouble(Left.value) + Double.parseDouble(Right.value)),
						Exec_Item_ConstType.Double);
			}
			else if (newType == Exec_Item_ConstType.Int) {
				return new Exec_Item_Const(
						String.valueOf(Integer.parseInt(Left.value) + Integer.parseInt(Right.value)),
						Exec_Item_ConstType.Int);
			}
			else {
				// error
				System.out.println("Type does not match operation!");
				return null;
			}
		}
		else if (OpType == Exec_Item_OpType.Minus) {
			if (newType == Exec_Item_ConstType.Double) {
				return new Exec_Item_Const(
						String.valueOf(Double.parseDouble(Left.value) - Double.parseDouble(Right.value)),
						Exec_Item_ConstType.Double);
			}
			else if (newType == Exec_Item_ConstType.Int) {
				return new Exec_Item_Const(
						String.valueOf(Integer.parseInt(Left.value) - Integer.parseInt(Right.value)),
						Exec_Item_ConstType.Int);
			}
			else {
				// error
				System.out.println("Type does not match operation!");
				return null;
			}
		}
		else if (OpType == Exec_Item_OpType.Times) {
			if (newType == Exec_Item_ConstType.Double) {
				return new Exec_Item_Const(
						String.valueOf(Double.parseDouble(Left.value) * Double.parseDouble(Right.value)),
						Exec_Item_ConstType.Double);
			}
			else if (newType == Exec_Item_ConstType.Int) {
				return new Exec_Item_Const(
						String.valueOf(Integer.parseInt(Left.value) * Integer.parseInt(Right.value)),
						Exec_Item_ConstType.Int);
			}
			else {
				// error
				System.out.println("Type does not match operation!");
				return null;
			}
		}
		else if (OpType == Exec_Item_OpType.Divide) {
			if (newType == Exec_Item_ConstType.Double) {
				return new Exec_Item_Const(
						String.valueOf(Double.parseDouble(Left.value) / Double.parseDouble(Right.value)),
						Exec_Item_ConstType.Double);
			}
			else if (newType == Exec_Item_ConstType.Int) {
				return new Exec_Item_Const(
						String.valueOf(Integer.parseInt(Left.value) / Integer.parseInt(Right.value)),
						Exec_Item_ConstType.Int);
			}
			else {
				// error
				System.out.println("Type does not match operation!");
				return null;
			}
		}
		else if (OpType == Exec_Item_OpType.StringAppend) {
			if (newType == Exec_Item_ConstType.String) {
				return new Exec_Item_Const(Left.value + Right.value, 
						Exec_Item_ConstType.String);
			}
			else {
				// error
				System.out.println("Type does not match operation!");
				return null;
			}
		}
		else {
			// error
			System.out.println("Wrong operation!");
			return null;
		}
	}
	
	public static Exec_Item_ConstType String_to_Type(String str) {
		if (str.contains("CHAR"))
			return Exec_Item_ConstType.String;
		if (str.contains("DOUBLE"))
			return Exec_Item_ConstType.Double;
		if (str.contains("INT"))
			return Exec_Item_ConstType.Int;
		if (str.contains("BOOLEAN"))
			return Exec_Item_ConstType.Boolean;
		if (str.contains("DECIMAL"))
			return Exec_Item_ConstType.Double;
		return null;
	}
	
	public static String Type_to_String(Exec_Item_ConstType Type) {
		if (Type == Exec_Item_ConstType.String)
			return "CHAR";
		if (Type == Exec_Item_ConstType.Double)
			return "DOUBLE";
		if (Type == Exec_Item_ConstType.Int)
			return "INT";
		if (Type == Exec_Item_ConstType.Boolean)
			return "BOOLEAN";
		return null;
	}
	
	public static Exec_CompareType Convert_CompareType(int compareop) {
		switch(compareop) {
		case Parse.sym.GT:
			return Exec_CompareType.GT;
		case Parse.sym.GE:
			return Exec_CompareType.GE;
		case Parse.sym.EQ:
			return Exec_CompareType.EQ;
		case Parse.sym.LT:
			return Exec_CompareType.LT;
		case Parse.sym.LE:
			return Exec_CompareType.LE;
		case Parse.sym.NEQ:
			return Exec_CompareType.NEQ;
		}
		return null;
	}
	
	public static boolean Check_Tuple_Equal(Tuple T1, Metadata M1, Tuple T2, Metadata M2) {
		/*
		 * R1, R2 only for metadata
		 */
		
		int size1 = T1.attribute.size();
		int size2 = T2.attribute.size();
		
		if (size1 != size2) {
			// error!
			System.out.println("Tuple sizes do not match!");
			return false;
		}
		
		// check type
		for (int i = 0; i < size1; i++) {
			if (Tools.String_to_Type(M1.Get_AttributeType(i)) 
					!= Tools.String_to_Type(M1.Get_AttributeType(i))) {
				// error!
				System.out.println("The tuples' type does not match");
				return false;
			}
		}
		
		for (int i = 0; i < size1; i++) {
			Exec_Item_ConstType type = Tools.String_to_Type(M1.Get_AttributeType(i));
			
			if ((T1.Get_AttributeValue(i) == null) || (T2.Get_AttributeValue(i) == null)) {
				System.out.println("There is null value in the tuple!");
				return false;
			}
			
			if ((type == Exec_Item_ConstType.Boolean) || (type == Exec_Item_ConstType.String)) {
				if (!T1.Get_AttributeValue(i).equals(T2.Get_AttributeValue(i))) return false;
			}
			else {
				if (Double.parseDouble(T1.Get_AttributeValue(i)) 
						!= Double.parseDouble(T2.Get_AttributeValue(i))) return false;
			}
		}
		
		return true;
	}
	
//	public static Tuple Tuple_Projection(Exec_ItemList IL, Tuple T, Records R) {
//		Tuple newTuple = new Tuple();
//		
//		Exec_ItemList il = IL;
//		
//		while (il != null) {
//			int index = R.IndexOf_AttributeName(((Exec_Item_Field) fl.head).name);
//			newTuple.Add_AttributeValue(T.Get_AttributeValue(index));
//		}
//		
//		return newTuple;
//	}
	
	public static boolean Tuple_In_Records(Tuple T, Metadata M, Records R) {
		int rowNum = R.Get_RowNum();
		
		for (int i = 0; i < rowNum; i++) {
			if (Check_Tuple_Equal(T, M, R.Get_Tuple(i), R.Get_Metadata())) return true;
		}
		
		return false;
	}
	
	public static int Count_Same_Tuple(Tuple T, Records R_of_T, Records R, boolean[] Mark) {
		int rowNum = R.Get_RowNum();
		int count = 0;
		
		for (int i = 0; i < rowNum; i++) {
			if (Check_Tuple_Equal(
					T, R_of_T.Get_Metadata(), R.Get_Tuple(i), R.Get_Metadata())) {
				count++;
				Mark[i] = true;
			}
		}
		
		return count;
	}
	
	public static String Do_Aggregate(ArrayList<String> valueList, String Op) {
		int listSize = valueList.size();
		
		if (Op == "COUNT") return String.valueOf(listSize);
		
		double value = 0;
		boolean first = true;
		
		for (int i = 0; i < listSize; i++) {
			if (valueList.get(i) == null) return null;
			double tempValue = Double.parseDouble(valueList.get(i));
			
			if ((Op == "SUM") || (Op == "AVG")) value += tempValue;
			else if (Op == "MIN") {
				if ((first) || (value > tempValue)) value = tempValue;
			}
			else if (Op == "MAX") {
				if ((first) || (value < tempValue)) value = tempValue;
			}
			else {
				// error
				System.out.println("No such Aggregate Operation");
				return null;
			}
			
			first = false;
		}
		
		if (Op == "AVG") value = value / ((double) (listSize));
		
		return String.valueOf(value);
	}
	
	public static String bytes2string(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++){
			if (b[i] == 0)
				break;
			ret += (char) b[i];
		}
		return ret;
	}
}
