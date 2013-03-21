package Execute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import java_cup.internal_error;

import qifeng.db.DbTable;
import qifeng.db.View;
import qifeng.file.Sequential;
import qifeng.schema.Schema;
import qifeng.schema.Symbol;
import qifeng.schema.Type;
import qifeng.schema.TypeHelper;

import Absyn.*;
import DataStyle.*;
import Constant.Const_For_Exec.*;
import Exec_Tree.*;
import Tools.*;
public class Execute {
	
/* *********************************************************************************************
 * Fundamental execute
 */
	public Object execute(Exec_Expr expr) {
		if (expr instanceof Exec_Stm_Create_Table) {
			executeCreateTable((Exec_Stm_Create_Table) expr);
			return null;
		}
		else if (expr instanceof Exec_Stm_Create_View) {
			executeCreateView((Exec_Stm_Create_View) expr);
			return null;
		}
		else if (expr instanceof Exec_Stm_Delete) {
			return executeStm_Delete((Exec_Stm_Delete) expr);
		}
		else if (expr instanceof Exec_Stm_Update) {
			return executeStm_Update((Exec_Stm_Update) expr);
		}
		else if (expr instanceof Exec_Exp) {
			return executeExp((Exec_Exp) expr);
		}
		else {
			// Error, no such expression
			return null;
		}
	}
	
	public void executeCreateTable(Exec_Stm_Create_Table expr) {
		
		// @TASK: Write metadata and table name into disk
	}
	
	public void executeCreateView(Exec_Stm_Create_View expr) {
		
	}
	
	public Records executeExp(Exec_Exp exp) {
		if (exp instanceof Exec_Exp_From) {
			return executeExp_From((Exec_Exp_From) exp);
		}
		else if (exp instanceof Exec_Exp_From_Join) {
			return executeExp_From_Join((Exec_Exp_From_Join) exp);
		}
		else if (exp instanceof Exec_Exp_Selection) {
			return executeExp_Selection((Exec_Exp_Selection) exp);
		}
		else if (exp instanceof Exec_Exp_Union) {
			return executeExp_Union((Exec_Exp_Union) exp);
		}
		else if (exp instanceof Exec_Exp_Intersect) {
			return executeExp_Intersect((Exec_Exp_Intersect) exp);
		}
		else if (exp instanceof Exec_Exp_Except) {
			return executeExp_Except((Exec_Exp_Except) exp);
		}
		else if (exp instanceof Exec_Exp_Group) {
			return executeExp_Group((Exec_Exp_Group) exp);
		}
		else if (exp instanceof Exec_Exp_Projection_NoAgg) {
			return executeExp_Projection_NoAgg((Exec_Exp_Projection_NoAgg) exp);
		}
		else if (exp instanceof Exec_Exp_Projection_Agg) {
			return executeExp_Projection_Agg((Exec_Exp_Projection_Agg) exp);
		}
		else if (exp instanceof Exec_Exp_OrderBy) {
			return executeExp_OrderBy((Exec_Exp_OrderBy) exp);
		}
		else if (exp instanceof Exec_Exp_Having) {
			return executeExp_Having((Exec_Exp_Having) exp);
		}
		else if (exp instanceof Exec_Exp_Distinct) {
			return executeExp_Distinct((Exec_Exp_Distinct) exp);
		}
		else if (exp instanceof Exec_Exp_Records) {
			return executeExp_Records((Exec_Exp_Records) exp);
		}
		else {
			// Error
			return null;
		}
	}
	
/* *******************************************************************************************
 * execute exp
 */
	
//	public Records executeExp_Rename(Exec_Exp_Rename exp)
//	{
//		Records newRecords = executeExp(exp.exp);
//		
//		int index = 0;
//		Exec_ItemList fl = exp.fieldList;
//		
//		while (fl != null) {
//			int attrIndex = newRecords.IndexOf_AttributeName(fl.head.name);
//			newRecords.Set_AttributeName(attrIndex, exp.nameList.get(index++));
//			fl = fl.tail;
//		}
//		
//		return newRecords;
//	}
	
	public Records executeExp_From(Exec_Exp_From exp) {
		int size = exp.recordsList.size();
		Records newRecords = executeExp_Records(exp.recordsList.get(0));
		
		if (size != 1) {
			// need to join
			
			String Name1 = exp.recordsList.get(0).name;
			if (exp.newNameList.get(0) != "") Name1 = exp.newNameList.get(0);
			
			for (int i = 1; i < size; i++) {
				Records tempRecords = executeExp_Records(exp.recordsList.get(i));
				
				String Name2 = exp.recordsList.get(i).name;
				if (exp.newNameList.get(i) != "") Name2 = exp.newNameList.get(i);
				
				newRecords = Tools.Do_Join(newRecords, Name1, tempRecords, Name2);
				
				Name1 = "";
			}
			
			ArrayList<String> sudoNameList = new ArrayList<String>();
			int newColumnNum = newRecords.Get_ColumnNum();
			for (int i = 0; i < newColumnNum; i++) {
				sudoNameList.add(newRecords.Get_AttributeName(i));
			}
			
			newRecords.Set_SudoNameList(sudoNameList);
			for (int i = 0; i < newColumnNum; i++) {
				boolean canChange = true;
				String name1 = Tools.Name_Behind_Point(newRecords.Get_AttributeName(i));
				for (int j = 0; j < newColumnNum; j++) {
					String name2 = Tools.Name_Behind_Point(newRecords.Get_AttributeName(j));
					if ((i != j) && (name1.equals(name2))) {
						canChange = false;
						break;
					}
				}
				
				if (canChange) newRecords.Set_AttributeName(i, name1);
			}
			
		}
		else {
			int columnNum = newRecords.Get_ColumnNum();
			ArrayList<String> sudoNameList = new ArrayList<String>();
			String newTableName = exp.newNameList.get(0);
			for (int i = 0; i < columnNum; i++) {
				sudoNameList.add(i, newTableName + "." + newRecords.Get_AttributeName(i));
			}
			
			newRecords.Set_SudoNameList(sudoNameList);
		}
		
		return newRecords;
	}
	
	public Records executeExp_From_Join(Exec_Exp_From_Join exp) {
		
		if (exp.joinType == 1) {
			Records newRecords = new Records();
			
			// nature join
			Records leftRecords = executeExp(exp.table1);
			Records rightRecords = executeExp(exp.table2);
			
			int leftRowNum = leftRecords.Get_RowNum(), leftColumnNum = leftRecords.Get_ColumnNum();
			int rightRowNum = rightRecords.Get_RowNum(), rightColumnNum = rightRecords.Get_ColumnNum();
			
			boolean[] leftMark = new boolean[leftColumnNum];
			boolean[] rightMark = new boolean[rightColumnNum];
			
			for (int i = 0; i < leftColumnNum; i++) {
				for (int j = 0; j < rightColumnNum; j++) {
					if (leftRecords.Get_AttributeName(i).equals(rightRecords.Get_AttributeName(j))) {
						leftMark[i] = true;
						rightMark[j] = true;
						break;
					}
				}
			}
			
			// for metafdata
			ArrayList<String> sudoNameList = new ArrayList<String>();
			for (int i = 0; i < leftColumnNum; i++) {
				newRecords.Add_Attribute(
						leftRecords.Get_AttributeName(i), 
						leftRecords.Get_AttributeType(i));
				sudoNameList.add(leftRecords.Get_AttributeName(i));
			}
				
			for (int j = 0; j < rightColumnNum; j++) {
				if (!rightMark[j]) {
					newRecords.Add_Attribute(
							rightRecords.Get_AttributeName(j), 
							rightRecords.Get_AttributeType(j));
					sudoNameList.add(rightRecords.Get_AttributeName(j));
				}
			}
				
			newRecords.Set_SudoNameList(sudoNameList);
			
			for (int leftIndex = 0; leftIndex < leftRowNum; leftIndex++) {
				for (int rightIndex = 0; rightIndex < rightRowNum; rightIndex++) {
					if (Tools.NatureJointMatch(leftRecords.Get_Tuple(leftIndex), leftRecords.Get_Metadata(),
							rightRecords.Get_Tuple(rightIndex), rightRecords.Get_Metadata())) {
						// now do join
						Tuple newTuple = new Tuple();
						for (int i = 0; i < leftColumnNum; i++) {
							newTuple.Add_AttributeValue(leftRecords.Get_AttributeValue(leftIndex, i));
						}
						for (int j = 0; j < rightColumnNum; j++) {
							if (!rightMark[j]) {
								newTuple.Add_AttributeValue(rightRecords.Get_AttributeValue(rightIndex, j));
							}
						}
						
						newRecords.Add_Tuple(newTuple);
					}
				}
			}
			
			return newRecords;
		}
		else if (exp.joinType == 2) {
			// FULL JOIN
			return null;
		}
		else if (exp.joinType == 3) {
			// cross join, the same as simple query
			ArrayList<Exec_Exp_Records> recordsList= new ArrayList<Exec_Exp_Records>();
			ArrayList<String> nameList = new ArrayList<String>();
			
			recordsList.add(exp.table1);
			recordsList.add(exp.table2);
			
			nameList.add(exp.table1.name);
			nameList.add(exp.table2.name);
			
			return executeExp(new Exec_Exp_From(recordsList, nameList));
		}
		else if (exp.joinType == 4) {
			// NATURAL LEFT JOIN
			return null;
		}
		else if (exp.joinType == 5) {
			// NATURAL RIGHT JOIN
			return null;
		}
		else {
			// error
			return null;
		}
		
	}
	
	public Records executeExp_Projection_NoAgg(Exec_Exp_Projection_NoAgg exp) {
		Records newRecords = new Records();
		Records tempRecords = executeExp(exp.exp);
		
		int columnNum = tempRecords.Get_ColumnNum(), rowNum = tempRecords.Get_RowNum();
		
		ArrayList<Exec_Item> il = exp.itemList;
		int ilSize = il.size();
		
		// finish metadata
		ArrayList<String> sudoNameList = new ArrayList<String>();
		for (int itemIndex = 0; itemIndex < ilSize; itemIndex++) {
			if (il.get(itemIndex) instanceof Exec_Item_Field_Star) {
				for (int j = 0; j < columnNum; j++) {
					newRecords.Add_Attribute(tempRecords.Get_AttributeName(j), 
							tempRecords.Get_AttributeType(j));
					sudoNameList.add(tempRecords.Get_SudoName(j));
				}
			}
			else {
				newRecords.Add_Attribute(exp.newNameList.get(itemIndex), 
						executeTools_CheckItemType(il.get(itemIndex), tempRecords));
				sudoNameList.add(tempRecords.Get_SudoName(itemIndex));
			}
		}
		newRecords.Set_SudoNameList(sudoNameList);
		
		// for data
		if (exp.group_itemList == null) {
			for (int i = 0; i < rowNum; i++) {
				il = exp.itemList;
				Tuple newTuple = new Tuple();
				
				for (int itemIndex = 0; itemIndex < ilSize; itemIndex++) {
					if (il.get(itemIndex) instanceof Exec_Item_Field_Star) {
						for (int j = 0; j < columnNum; j++) {
							newTuple.Add_AttributeValue(tempRecords.Get_AttributeValue(i, j));
						}
					}
					else {
						newTuple.Add_AttributeValue(
								executeItem(il.get(itemIndex), tempRecords.Get_Tuple(i), tempRecords).value);
					}
				}
				
				newRecords.Add_Tuple(newTuple);
			}
		}
		else {
			// has group
			int headPoint = 0, tailPoint = 0;
			
			Metadata metadata = new Metadata();
			Exec_ItemList groupItemList = exp.group_itemList;
			
			while (groupItemList != null) {
				metadata.Add_Attribute("", executeTools_CheckItemType(groupItemList.head, tempRecords));
				groupItemList = groupItemList.tail;
			}
			
			while (tailPoint < rowNum) {
				headPoint = tailPoint;
				
				Tuple tempTuple = executeTools_TupleProjection(exp.group_itemList, 
						tempRecords.Get_Tuple(headPoint), tempRecords);
				
				while (true) {
					if (++tailPoint == rowNum) break;
					Tuple checkTuple = executeTools_TupleProjection(exp.group_itemList, 
							tempRecords.Get_Tuple(tailPoint), tempRecords);
					if (!Tools.Check_Tuple_Equal(tempTuple, metadata, checkTuple, metadata)) break;
				}
				
				Tuple newTuple = new Tuple();
				
				for (int itemIndex = 0; itemIndex < ilSize; itemIndex++) {
					if (il.get(itemIndex) instanceof Exec_Item_Field_Star) {
						for (int j = 0; j < columnNum; j++) {
							newTuple.Add_AttributeValue(tempRecords.Get_AttributeValue(headPoint, j));
						}
					}
					else {
						newTuple.Add_AttributeValue(
								executeItem(il.get(itemIndex), tempRecords.Get_Tuple(headPoint), tempRecords).value);
					}
				}
				
				newRecords.Add_Tuple(newTuple);
			}
		}
		
		return newRecords;
	}
	
	public Records executeExp_Projection_Agg(Exec_Exp_Projection_Agg exp) {
		Records newRecords = new Records();
		Records tempRecords = executeExp(exp.exp);
		
		int columnNum = tempRecords.Get_ColumnNum(), rowNum = tempRecords.Get_RowNum();
		
		ArrayList<Exec_Item> il = exp.itemList;
		int ilSize = il.size();
		
		// finish metadata
		ArrayList<String> sudoNameList = new ArrayList<String>();
		for (int itemIndex = 0; itemIndex < ilSize; itemIndex++) {
			if (il.get(itemIndex) instanceof Exec_Item_Field_Star) {
				for (int j = 0; j < columnNum; j++) {
					newRecords.Add_Attribute(tempRecords.Get_AttributeName(j), 
							tempRecords.Get_AttributeType(j));
					sudoNameList.add(tempRecords.Get_SudoName(j));
				}
			}
			else {
				newRecords.Add_Attribute(exp.newNameList.get(itemIndex), 
						executeTools_CheckItemType(il.get(itemIndex), tempRecords));
				sudoNameList.add(tempRecords.Get_SudoName(itemIndex));
			}
		}
		newRecords.Set_SudoNameList(sudoNameList);
		
		// finish data
		int headPoint = 0, tailPoint = 0;
//		
		while (tailPoint < rowNum) {
			headPoint = tailPoint;
			
			Metadata metadata = new Metadata();
			Exec_ItemList groupItemList = exp.group_itemList;
			
			while (groupItemList != null) {
				metadata.Add_Attribute("", executeTools_CheckItemType(groupItemList.head, tempRecords));
				groupItemList = groupItemList.tail;
			}
			
			Tuple tempTuple = executeTools_TupleProjection(exp.group_itemList, 
					tempRecords.Get_Tuple(headPoint), tempRecords);
			
			while (true) {
				if (++tailPoint == rowNum) break;
				Tuple checkTuple = executeTools_TupleProjection(exp.group_itemList, 
						tempRecords.Get_Tuple(tailPoint), tempRecords);
				if (!Tools.Check_Tuple_Equal(tempTuple, metadata, checkTuple, metadata)) break;
			}
			
			Tuple newTuple = new Tuple();
			
			for (int itemIndex = 0; itemIndex < ilSize; itemIndex++) {
				if (il.get(itemIndex) instanceof Exec_Item_Field_Star) {
					for (int j = 0; j < columnNum; j++) {
						newTuple.Add_AttributeValue(tempRecords.Get_AttributeValue(headPoint, j));
					}
				}
				else {
					if (exp.aggOpList.get(itemIndex) != "") {
						ArrayList<String> valueList = new ArrayList<String>();
						for (int i = headPoint; i < tailPoint; i++) {
							valueList.add(
									executeItem(il.get(itemIndex), tempRecords.Get_Tuple(i), tempRecords).value);
						}
					newTuple.Add_AttributeValue(Tools.Do_Aggregate(valueList, exp.aggOpList.get(itemIndex)));
					}
					else {
						newTuple.Add_AttributeValue(
								executeItem(il.get(itemIndex), tempRecords.Get_Tuple(headPoint), tempRecords).value);
					}
				}
			}
			
			newRecords.Add_Tuple(newTuple);
		}
		
		return newRecords;
	}
	
	public Records executeExp_Selection(Exec_Exp_Selection exp) {
		Records newRecords = new Records();
		Records tempRecords = executeExp(exp.exp);
		
		int rowNum = tempRecords.Get_RowNum();
		
		newRecords.Copy_Metadata(tempRecords);
		
		for (int i = 0; i < rowNum; i++) {
			Tuple tempTuple = tempRecords.Get_Tuple(i);
			Temporary.Temp_Records.Clear();
			
			if (executeCondition(exp.condition, tempTuple, tempRecords)) {
				Tuple newTuple = new Tuple();
				newTuple.Copy(tempTuple);
				newRecords.Add_Tuple(newTuple);
			}
		}
		
		return newRecords;
	}
	
	public Records executeExp_Union(Exec_Exp_Union exp) {
		Records newrRecords = new Records();
		
		Records left_tempRecords = executeExp(exp.leftExp);
		Records right_tempRecords = executeExp(exp.rightExp);
		
		int left_columnNum = left_tempRecords.Get_ColumnNum();
		int right_columnNum = right_tempRecords.Get_ColumnNum();
		
		// check whether they match
		ArrayList<String> sudoNameList = new ArrayList<String>();
		if (left_columnNum != right_columnNum) {
			// error
			System.out.println("They cannot do Union!");
			return null;
		}
		else {
			for (int i = 0; i < left_columnNum; i++) {
				if (!left_tempRecords.Get_AttributeName(i).equals(right_tempRecords.Get_AttributeName(i))) {
					// error
					System.out.println("They cannot do Union!");
					return null;
				}
				else if (Tools.String_to_Type(left_tempRecords.Get_AttributeType(i)) !=
					Tools.String_to_Type(right_tempRecords.Get_AttributeType(i))) {
					// error
					System.out.println("They cannot do Union!");
					return null;
				}
				else {
					newrRecords.Add_Attribute(left_tempRecords.Get_AttributeName(i), 
							left_tempRecords.Get_AttributeType(i));
					sudoNameList.add(left_tempRecords.Get_SudoName(i));
				}
			}
		}
		newrRecords.Set_SudoNameList(sudoNameList);
		
		int left_rowNum = left_tempRecords.Get_RowNum();
		int right_rowNum = right_tempRecords.Get_RowNum();
		boolean[] left_mark = new boolean[left_rowNum];
		boolean[] right_mark = new boolean[right_rowNum];
		for (int i = 0; i < left_rowNum; i++) left_mark[i] = false;
		for (int i = 0; i < right_rowNum; i++) right_mark[i] = false;
		boolean left_finish = false;
		
		while (!left_finish) {
			left_finish = true;
			int i = 0;
			
			for (i = 0; i < left_rowNum; i++) {
				if (!left_mark[i]) {
					left_finish = false;
					break;
				}
			}
			
			if (!left_finish) {
				Tuple tempTuple = left_tempRecords.Get_Tuple(i);
				int left_count = Tools.Count_Same_Tuple(tempTuple, left_tempRecords, 
						left_tempRecords, left_mark);
				int right_count = Tools.Count_Same_Tuple(tempTuple, right_tempRecords, 
						right_tempRecords, right_mark);
				
				int count = left_count + right_count;
				if (exp.isAll) count = 1;
				for (int j = 0; j < count; j++) {
					Tuple newTuple = new Tuple();
					newTuple.Copy(tempTuple);
					newrRecords.Add_Tuple(newTuple);
				}
			}
		}
		
		// deal with the remaining right Tuples
		for (int j = 0; j < right_rowNum; j++) {
			if (!right_mark[j]) {
				Tuple newTuple = new Tuple();
				newTuple.Copy(right_tempRecords.Get_Tuple(j));
				newrRecords.Add_Tuple(newTuple);
			}
		}
		
		return newrRecords;
	}
	
	public Records executeExp_Intersect(Exec_Exp_Intersect exp) {
		Records newrRecords = new Records();
		
		Records left_tempRecords = executeExp(exp.leftExp);
		Records right_tempRecords = executeExp(exp.rightExp);
		
		int left_columnNum = left_tempRecords.Get_ColumnNum();
		int right_columnNum = right_tempRecords.Get_ColumnNum();
		
		// check whether they match
		ArrayList<String> sudoNameList = new ArrayList<String>();
		if (left_columnNum != right_columnNum) {
			// error
			System.out.println("They cannot do Union!");
			return null;
		}
		else {
			for (int i = 0; i < left_columnNum; i++) {
				if (!left_tempRecords.Get_AttributeName(i).equals(right_tempRecords.Get_AttributeName(i))) {
					// error
					System.out.println("They cannot do Union!");
					return null;
				}
				else if (Tools.String_to_Type(left_tempRecords.Get_AttributeType(i)) !=
					Tools.String_to_Type(right_tempRecords.Get_AttributeType(i))) {
					// error
					System.out.println("They cannot do Union!");
					return null;
				}
				else {
					newrRecords.Add_Attribute(left_tempRecords.Get_AttributeName(i), 
							left_tempRecords.Get_AttributeType(i));
					sudoNameList.add(left_tempRecords.Get_SudoName(i));
				}
			}
		}
		newrRecords.Set_SudoNameList(sudoNameList);
		
		int left_rowNum = left_tempRecords.Get_RowNum();
		int right_rowNum = right_tempRecords.Get_RowNum();
		boolean[] left_mark = new boolean[left_rowNum];
		boolean[] right_mark = new boolean[right_rowNum];
		for (int i = 0; i < left_rowNum; i++) left_mark[i] = false;
		for (int i = 0; i < right_rowNum; i++) right_mark[i] = false;
		boolean left_finish = false;
		
		while (!left_finish) {
			left_finish = true;
			int i = 0;
			
			for (i = 0; i < left_rowNum; i++) {
				if (!left_mark[i]) {
					left_finish = false;
					break;
				}
			}
			
			if (!left_finish) {
				Tuple tempTuple = left_tempRecords.Get_Tuple(i);
				int left_count = Tools.Count_Same_Tuple(tempTuple, left_tempRecords, 
						left_tempRecords, left_mark);
				int right_count = Tools.Count_Same_Tuple(tempTuple, left_tempRecords, 
						right_tempRecords, right_mark);
				
				int real_count = left_count;
				if (real_count > right_count) real_count = right_count;
				if ((real_count > 0) && (exp.isAll)) real_count = 1;
				
				for (int j = 0; j < real_count; j++) {
					Tuple newTuple = new Tuple();
					newTuple.Copy(tempTuple);
					newrRecords.Add_Tuple(newTuple);
				}
			}
		}
		
		return newrRecords;
	}
	
	public Records executeExp_Except(Exec_Exp_Except exp) {
		Records newrRecords = new Records();
		
		Records left_tempRecords = executeExp(exp.leftExp);
		Records right_tempRecords = executeExp(exp.rightExp);
		
		int left_columnNum = left_tempRecords.Get_ColumnNum();
		int right_columnNum = right_tempRecords.Get_ColumnNum();
		
		// check whether they match
		ArrayList<String> sudoNameList = new ArrayList<String>();
		if (left_columnNum != right_columnNum) {
			// error
			System.out.println("They cannot do Union!");
			return null;
		}
		else {
			for (int i = 0; i < left_columnNum; i++) {
				if (!left_tempRecords.Get_AttributeName(i).equals(right_tempRecords.Get_AttributeName(i))) {
					// error
					System.out.println("They cannot do Union!");
					return null;
				}
				else if (Tools.String_to_Type(left_tempRecords.Get_AttributeType(i)) !=
					Tools.String_to_Type(right_tempRecords.Get_AttributeType(i))) {
					// error
					System.out.println("They cannot do Union!");
					return null;
				}
				else {
					newrRecords.Add_Attribute(left_tempRecords.Get_AttributeName(i), 
							left_tempRecords.Get_AttributeType(i));
					sudoNameList.add(left_tempRecords.Get_SudoName(i));
				}
			}
		}
		newrRecords.Set_SudoNameList(sudoNameList);
		
		int left_rowNum = left_tempRecords.Get_RowNum();
		int right_rowNum = right_tempRecords.Get_RowNum();
		boolean[] left_mark = new boolean[left_rowNum];
		boolean[] right_mark = new boolean[right_rowNum];
		for (int i = 0; i < left_rowNum; i++) left_mark[i] = false;
		for (int i = 0; i < right_rowNum; i++) right_mark[i] = false;
		boolean left_finish = false;
		
		while (!left_finish) {
			left_finish = true;
			int i = 0;
			
			for (i = 0; i < left_rowNum; i++) {
				if (!left_mark[i]) {
					left_finish = false;
					break;
				}
			}
			
			if (!left_finish) {
				Tuple tempTuple = left_tempRecords.Get_Tuple(i);
				int left_count = Tools.Count_Same_Tuple(tempTuple, left_tempRecords, 
						left_tempRecords, left_mark);
				int right_count = Tools.Count_Same_Tuple(tempTuple, right_tempRecords, 
						right_tempRecords, right_mark);
				
				int real_count = left_count - right_count;
				if ((real_count > 0) && (exp.isAll)) real_count = 1;
				if (real_count < 0) real_count = 0;
				
				for (int j = 0; j < real_count; j++) {
					Tuple newTuple = new Tuple();
					newTuple.Copy(tempTuple);
					newrRecords.Add_Tuple(newTuple);
				}
			}
		}
		
		return newrRecords;
	}
	
	public Records executeExp_Group(Exec_Exp_Group exp) {
		Records newRecords = new Records();
		
		Records tempRecords = executeExp(exp.exp);
		ArrayList<Tuple> tupleList = new ArrayList<Tuple>();
		
		newRecords.Copy_Metadata(tempRecords);
		
		int rowNum = tempRecords.Get_RowNum();
		
		Metadata metadata = new Metadata();
		Exec_ItemList groupItemList = exp.group_itemList;
		
		while (groupItemList != null) {
			metadata.Add_Attribute("", executeTools_CheckItemType(groupItemList.head, tempRecords));
			groupItemList = groupItemList.tail;
		}
		
		for (int i = 0; i < rowNum; i++) {
			Tuple newTuple = executeTools_TupleProjection(exp.group_itemList, 
					tempRecords.Get_Tuple(i), tempRecords);
			tupleList.add(newTuple);
		}
		
		boolean[] mark = new boolean[rowNum];
		boolean finish = false;
		
		while (!finish) {
			finish = true;
			int index;
			
			for (index = 0; index < rowNum; index++) {
				if (!mark[index]) {
					finish = false;
					break;
				}
			}
			
			if (!finish) {
				Tuple tempTuple = tupleList.get(index);
				for (int j = 0; j < rowNum; j++) {
					if (Tools.Check_Tuple_Equal(tempTuple, metadata, tupleList.get(j), metadata)) {
						mark[j] = true;
						Tuple newTuple = new Tuple();
						newTuple.Copy(tempRecords.Get_Tuple(j));
						newRecords.Add_Tuple(newTuple);
					}
				}
			}
		}
		
		return newRecords;
	}
	
	public Records executeExp_OrderBy(Exec_Exp_OrderBy exp) {
		Records newRecords = new Records();
		
		Records tempRecords = executeExp(exp.exp);
		
		newRecords.Copy_Metadata(tempRecords);
		
		int rowNum = tempRecords.Get_RowNum();
		int itemSize = exp.itemList.size();
		
		boolean[] mark = new boolean[rowNum];
		boolean finish = false;
		
		while (!finish) {
			finish = true;
			int index = 0;
			
			for (index = 0; index < rowNum; index++) {
				if (!mark[index]) {
					finish = false;
					break;
				}
			}
			
			if (finish) break;
			
			int j = index;
			
			while (true) {
				if (++j == rowNum) break;
				if (!mark[j]) {
					for (int k = 0; k < itemSize; k++) {
						Exec_Item_Const choseItemConst = executeItem(exp.itemList.get(k), 
								tempRecords.Get_Tuple(index), tempRecords);
						Exec_Item_Const tryItemConst = executeItem(exp.itemList.get(k), 
								tempRecords.Get_Tuple(j), tempRecords);

						Exec_CompareType compareType;

						// satisfy the condition, get the trial one: (index =
						// j);
						if (exp.isDownList.get(k)) compareType = Exec_CompareType.GT;
						else compareType = Exec_CompareType.LT;
						if (Tools.Check_Condition(compareType, choseItemConst, tryItemConst)) {
							index = j;
							break;
						}

						// violate the condition, get the original one: no
						// (index = j)
						if (exp.isDownList.get(k)) compareType = Exec_CompareType.LT;
						else compareType = Exec_CompareType.GT;
						if (Tools.Check_Condition(compareType, choseItemConst, tryItemConst)) {
							break;
						}
					}
				}
			}
			
			newRecords.Add_Tuple(tempRecords.Get_Tuple(index));
			mark[index] = true;
		}
		
		return newRecords;
	}
	
//	public Records executeExp_Aggregate(Exec_Exp_Aggregate exp) {
//		Records newRecords = new Records();
//		
//		Records tempRecords = executeExp(exp.exp);
//		
//		newRecords.Copy_Metadata(tempRecords);
//		
//		int rowNum = tempRecords.Get_RowNum();
//		
//		int headPoint = 0, tailPoint = 0;
//		
//		while (tailPoint < rowNum) {
//			headPoint = tailPoint;
//			
//			Tuple tempTuple = Tools.Tuple_Projection(exp.group_fieldList, 
//					tempRecords.Get_Tuple(headPoint), tempRecords);
//			
//			while (true) {
//				Tuple checkTuple = Tools.Tuple_Projection(exp.group_fieldList, 
//						tempRecords.Get_Tuple(++tailPoint), tempRecords);
//				if (!Tools.Check_Tuple_Equal(tempTuple, checkTuple)) break;
//				if (tailPoint == rowNum) {
//					tailPoint++;
//					break;
//				}
//			}
//			
//			int index = 0, aggOp_count = 0;
//			Exec_ItemList aggList = exp.aggregate_fieldList;
//			Tuple newTuple = new Tuple();
//			
//			while (aggList != null) {
//				int tempIndex = tempRecords.IndexOf_AttributeName(aggList.head.name);
//				for (int i = index; i < tempIndex; i++) {
//					newTuple.Add_AttributeValue(tempRecords.Get_Tuple(headPoint).Get_AttributeValue(i));
//				}
//				
//				newTuple.Add_AttributeValue(Tools.Do_Aggregate(headPoint, tailPoint-1, 
//						aggList.head, tempRecords, exp.aggregate_operationList.get(aggOp_count++)));
//				aggList = aggList.tail;
//			}
//		}
//		
//		return newRecords;
//		
//	}
	
	public Records executeExp_Having(Exec_Exp_Having exp) {
		Records newRecords = new Records();
		Records tempRecords = executeExp(exp.exp);
		
		newRecords.Copy_Metadata(tempRecords);
		
		int rowNum = tempRecords.Get_RowNum();
		int headPoint = 0, tailPoint = 0;
	
		while (tailPoint < rowNum) {
			headPoint = tailPoint;
			
			Metadata metadata = new Metadata();
			Exec_ItemList groupItemList = exp.group_itemList;
			
			while (groupItemList != null) {
				metadata.Add_Attribute("", executeTools_CheckItemType(groupItemList.head, tempRecords));
				groupItemList = groupItemList.tail;
			}
			
			Tuple tempTuple = executeTools_TupleProjection(exp.group_itemList, 
					tempRecords.Get_Tuple(headPoint), tempRecords);
			
			while (true) {
				if (++tailPoint == rowNum) break;
				Tuple checkTuple = executeTools_TupleProjection(exp.group_itemList, 
						tempRecords.Get_Tuple(tailPoint), tempRecords);
				if (!Tools.Check_Tuple_Equal(tempTuple, metadata, checkTuple, metadata)) break;
			}
			
			// Now row(headpoint) to row(tailpoint - 1) belongs to the same group
			
			if (executeCondition(exp.condition, tempRecords, headPoint, tailPoint-1)) {
				for (int i = headPoint; i < tailPoint; i++) {
					Tuple validTuple = new Tuple();
					validTuple.Copy(tempRecords.Get_Tuple(i));
					newRecords.Add_Tuple(validTuple);
				}
			}
		}
		
		return newRecords;
	}
	
	public Records executeExp_Distinct(Exec_Exp_Distinct exp) {
		Records newRecords = new Records();
		Records tempRecords = executeExp(exp.exp);
		
		newRecords.Copy_Metadata(tempRecords);
		
		int rowNum = tempRecords.Get_RowNum();
		
		boolean[] mark = new boolean[rowNum];
		boolean finish = false;
		
		while (!finish) {
			finish = true;
			int index;
			
			for (index = 0; index < rowNum; index++) {
				if (!mark[index]) {
					finish = false;
					break;
				}
			}
			
			if (!finish) {
				mark[index] = true;
				Tuple newTuple = new Tuple();
				newTuple.Copy(tempRecords.Get_Tuple(index));
				
				for (int j = 0; j < rowNum; j++) {
					if (!mark[j]) {
						if (Tools.Check_Tuple_Equal(newTuple, newRecords.Get_Metadata(),
								tempRecords.Get_Tuple(j), tempRecords.Get_Metadata())) {
							mark[j] = true;
						}
					}
				}
				
				newRecords.Add_Tuple(newTuple);
			}
		}
		
		return newRecords;
		
	}
	
	public Records executeExp_Records(Exec_Exp_Records exp) {
		String tablename = exp.name;
		DbTable dbt = null;
		View dbv = null;
		int ty = 0;
		Collection<String> collection = null;
		try {
			collection = Main.main.database.lsTable();
		} catch (IOException e) {
			System.err.println("database lsTable error.");
			e.printStackTrace();
		}
		Object[] a = collection.toArray();
		for (Object o: a) {
			if (tablename.equals(o.toString()))
				ty = 1;
		}
		try {
			collection = Main.main.database.lsView();
		} catch (IOException e) {
			System.err.println("database lsTable error.");
			e.printStackTrace();
		}
		a = collection.toArray();
		for (Object o: a) {
			if (tablename.equals(o.toString()))
				ty = 2;
		}
		switch(ty) {
		case 0:
			System.err.println("Table/View not exist!");
			return null;
		case 1:
			try {
				dbt = Main.main.database.openTable(tablename);
			} catch (IOException e) {
				System.err.println("Error: Table \'"+tablename+"\' does not exist!" );
			}
			break;
		case 2:
			try {
				dbv = Main.main.database.openView(tablename);
			} catch (IOException e) {
				System.err.println("Error: View \'"+tablename+"\' does not exist!");
				e.printStackTrace();
			}
			String query = dbv.getStatement();
			ArrayList<Records> records = Main.main.excutePlan(query);
			if (records.size()!=0)
				return Main.main.excutePlan(query).get(0);
			else {
				return null;
			}
		default:
			System.err.println("Unknown Error!");
		}

		Schema sc = dbt.getSchema();
		Records records = new Records();
		records.setSchema(sc);
		String type = null;
		for (int i = 0; i < sc.size(); i++) {
			Type t = sc.getType(i);
			String n = sc.getSymbol(i).toString();
			if (t.getCategory() == Type.Category.INTEGER)
				type = "INT";
			else if (t.getCategory() == Type.Category.CHARACTER ||
					t.getCategory() == Type.Category.VARY_LENGTH_CHARACTER)
				type = "CHAR(" + t.getLength() + ")";
			else if (t.getCategory() == Type.Category.FLOATING_POINT) {
				if (t.getLength() == 4 || t.getLength() == 8)
					type = "DOUBLE";
				else {
					int len1 = t.getLength()>>16;
					int len2 = t.getLength() - (len1<<16);
					type = "DECIMAL("+len1+", "+len2+")";
				}
			}
			records.Add_Attribute(n, type);
		}
		sc = dbt.getSchema();
		for (byte[] record : dbt) { // iterate on the table
			Tuple t = new Tuple();
			for (int i = 0; i < sc.size(); i++) {
				try {
					Object result = sc.getValue(record, i);
					if (result == null) {
						t.Add_AttributeValue(null);
						continue;
					}
					if (sc.getType(i).getCategory() == Type.Category.CHARACTER) //||
						//	sc.getType(i).getCategory() == Type.Category.VARY_LENGTH_CHARACTER)
						t.Add_AttributeValue(Tools.bytes2string((byte[]) result));
					else
						t.Add_AttributeValue(result.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			records.Add_Tuple(t);
		}
		dbt.close();
		return records;
	}
	
/* ********************************************************************************************************
 * execute condtion and item for having
 */
	public boolean executeCondition(Exec_Condition con, 
			Records tempRecords, int start, int end) {
		if (con instanceof Exec_Condition_Compare) {
			return executeCondition_Compare((Exec_Condition_Compare) con, 
					tempRecords, start, end);
		}
		else if (con instanceof Exec_Condition_NOT) {
			return executeCondition_NOT((Exec_Condition_NOT) con, 
					tempRecords, start, end);
		}
		else if (con instanceof Exec_Condition_AND) {
			return executeCondition_AND((Exec_Condition_AND) con,
					tempRecords, start, end);
		}
		else if (con instanceof Exec_Condition_OR) {
			return executeCondition_OR((Exec_Condition_OR) con, 
					tempRecords, start, end);
		}
//		else if (con instanceof Exec_Condition_IN) {
//			return executeCondition_IN((Exec_Condition_IN) con,
//					tempTuple, tempRecords, start, end);
//		}
		else if (con instanceof Exec_Condition_ALL) {
			return executeCondition_ALL((Exec_Condition_ALL) con,
					tempRecords, start, end);
		}
		else if (con instanceof Exec_Condition_ANY) {
			return executeCondition_ANY((Exec_Condition_ANY) con, 
					tempRecords, start, end);
		}
		return false;
	}
	
	public boolean executeCondition_Compare(Exec_Condition_Compare con, 
			Records tempRecords, int start, int end) {
		/*
		 * compareFormation:
		 * 
		 * 1: exp compare_op exp
		 * 2: exp compare_op LPAREN query RPAREN
		 * 3: LPAREN query RPAREN compare_op exp
		 * 4: LPAREN query RPAREN compare_op LPAREN query RPAREN
		 * 
		 */
		return Tools.Check_Condition(con.compareType, 
					executeItem(con.leftItem, tempRecords, start, end), 
					executeItem(con.rightItem, tempRecords, start, end));
	}
	
	public boolean executeCondition_NOT(Exec_Condition_NOT con, 
			Records tempRecords, int start, int end) {
		return !executeCondition(con.condition, tempRecords, start, end);
	}
	
	public boolean executeCondition_AND(Exec_Condition_AND con, 
			Records tempRecords, int start, int end) {
		
		if (!executeCondition(con.leftCondition, tempRecords, start, end)) {
			return false;
		}
		else return executeCondition(con.righCondition, tempRecords, start, end);
	}
	
	public boolean executeCondition_OR(Exec_Condition_OR con, 
			Records tempRecords, int start, int end) {
		
		if (executeCondition(con.leftCondition, tempRecords, start, end)) {
			return true;
		}
		else return executeCondition(con.righCondition, tempRecords, start, end);
	}
	
//	public boolean executeCondition_IN(Exec_Condition_IN con,
//			Tuple tempTuple, Records tempRecords, int start, int end) {
//		
//		int num = ++Temporary.Temp_Records.num;
//		Records memory_tempRecords;
//		
//		// read from memory to see whether the temperory Records exists
//		if (num > Temporary.Temp_Records.tempRecordsList.size()) {
//			memory_tempRecords = executeExp(con.exp);
//			Temporary.Temp_Records.tempRecordsList.add(memory_tempRecords);
//		}
//		else memory_tempRecords = Temporary.Temp_Records.tempRecordsList.get(num - 1);
//		
//		Metadata metadata = new Metadata();
//		Exec_ItemList groupItemList = con.itemList;
//		
//		while (groupItemList != null) {
//			metadata.Add_Attribute("", executeTools_CheckItemType(groupItemList.head, tempRecords));
//			groupItemList = groupItemList.tail;
//		}
//		
//		Tuple subTuple = executeTools_TupleProjection(con.itemList, tempTuple, tempRecords);
//		
//		return Tools.Tuple_In_Records(subTuple, metadata, memory_tempRecords);
//		
//	}
	
	public boolean executeCondition_ALL(Exec_Condition_ALL con,
			Records tempRecords, int start, int end) {
		
		Exec_Item_Const tempValue = executeItem(con.item, tempRecords, start, end);
		
		// read from memory to see whether the temperory Records exists
		int num = ++Temporary.Temp_Records.num;
		Records memory_tempRecords;
		
		if (num > Temporary.Temp_Records.tempRecordsList.size()) {
			memory_tempRecords = executeExp(con.exp);
			Temporary.Temp_Records.tempRecordsList.add(memory_tempRecords);
		}
		else memory_tempRecords = Temporary.Temp_Records.tempRecordsList.get(num - 1);
		
		if (memory_tempRecords.Get_ColumnNum() != 1) {
			// error it can only have one column
			System.out.println("In ALL condition, it can only have one column!");
			return false;
		}
		
		int rowNum = memory_tempRecords.Get_RowNum();
		
		if (rowNum == 0) return true;
		
		for (int i = 0; i < rowNum; i++) {
			Exec_Item_Const checkConst = new Exec_Item_Const(
					memory_tempRecords.Get_AttributeValue(i, 0), 
					Tools.String_to_Type(memory_tempRecords.Get_AttributeType(0)));
			
			if (!Tools.Check_Condition(con.compareType, tempValue, checkConst)) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean executeCondition_ANY(Exec_Condition_ANY con,
			Records tempRecords, int start, int end) {
		
		Exec_Item_Const tempValue = executeItem(con.item, tempRecords, start, end);
		
		// read from memory to see whether the temperory Records exists
		int num = ++Temporary.Temp_Records.num;
		Records memory_tempRecords;
		
		if (num > Temporary.Temp_Records.tempRecordsList.size()) {
			memory_tempRecords = executeExp(con.exp);
			Temporary.Temp_Records.tempRecordsList.add(memory_tempRecords);
		}
		else memory_tempRecords = Temporary.Temp_Records.tempRecordsList.get(num - 1);
		
		if (memory_tempRecords.Get_ColumnNum() != 1) {
			// error it can only have one column
			System.out.println("In ALL condition, it can only have one column!");
			return false;
		}
		
		int rowNum = memory_tempRecords.Get_RowNum();
		
		if (rowNum == 0) return false;
		
		for (int i = 0; i < rowNum; i++) {
			Exec_Item_Const checkConst = new Exec_Item_Const(
					memory_tempRecords.Get_AttributeValue(i, 0), 
					Tools.String_to_Type(memory_tempRecords.Get_AttributeType(0)));
			
			if (Tools.Check_Condition(con.compareType, tempValue, checkConst)) {
				return true;
			}
		}
		
		return false;
	}
	
	public Exec_Item_Const executeItem(Exec_Item item, Records tempRecords
			, int start, int end) {
		if (item instanceof Exec_Item_Const) {
			return executeItem_Const((Exec_Item_Const) item);
		}
		else if (item instanceof Exec_Item_Exp) {
			return executeItem_Exp((Exec_Item_Exp) item);
		}
		else if (item instanceof Exec_Item_Field_Agg) {
			return executeItem_Field_Agg((Exec_Item_Field_Agg) item, 
					tempRecords, start, end);
		}
		else if (item instanceof Exec_Item_FieldDotField_Agg) {
			return executeItem_FieldDotField_Agg((Exec_Item_FieldDotField_Agg) item, 
					tempRecords, start, end);
		}
		else if (item instanceof Exec_Item_Op) {
			return executeItem_Op((Exec_Item_Op) item, tempRecords, start, end);
		}
		else {
			// error
			System.out.println("Wrong Item!");
			return null;
		}
	}
	
	public Exec_Item_Const executeItem_Field_Agg(Exec_Item_Field_Agg item,
			Records tempRecords, int start, int end) {
		
		ArrayList<String> valueList = new ArrayList<String>();
		Exec_Item_ConstType type = null;
		
		for (int i = start; i <= end; i++) {
			Exec_Item_Const tempConst = executeItem_Field(item, 
					tempRecords.Get_Tuple(i), tempRecords);
			valueList.add(tempConst.value);
			type = tempConst.type;
		}
		
		return new Exec_Item_Const(Tools.Do_Aggregate(valueList, item.aggOp), type);
	}
	
	public Exec_Item_Const executeItem_FieldDotField_Agg(Exec_Item_FieldDotField_Agg item,
			Records tempRecords, int start, int end) {
		ArrayList<String> valueList = new ArrayList<String>();
		Exec_Item_ConstType type = null;
		
		for (int i = start; i <= end; i++) {
			Exec_Item_Const tempConst = executeItem_FieldDotField(item, 
					tempRecords.Get_Tuple(i), tempRecords);
			valueList.add(tempConst.value);
			type = tempConst.type;
		}
		
		return new Exec_Item_Const(Tools.Do_Aggregate(valueList, item.aggOp), type);
	}
	
	public Exec_Item_Const executeItem_Op(Exec_Item_Op item,
			Records tempRecords, int start, int end) {
		return Tools.Do_Item_Op(
				executeItem(item.leftItem, tempRecords, start, end), 
				executeItem(item.rightItem, tempRecords, start, end), 
				item.OpType);
	}
	
/* ********************************************************************************************************
 * execute condtion
 */
	public boolean executeCondition(Exec_Condition con, 
			Tuple tempTuple, Records tempRecords) {
		if (con instanceof Exec_Condition_Compare) {
			return executeCondition_Compare((Exec_Condition_Compare) con, tempTuple, tempRecords);
		}
		else if (con instanceof Exec_Condition_NOT) {
			return executeCondition_NOT((Exec_Condition_NOT) con, tempTuple, tempRecords);
		}
		else if (con instanceof Exec_Condition_AND) {
			return executeCondition_AND((Exec_Condition_AND) con, tempTuple, tempRecords);
		}
		else if (con instanceof Exec_Condition_OR) {
			return executeCondition_OR((Exec_Condition_OR) con, tempTuple, tempRecords);
		}
		else if (con instanceof Exec_Condition_IN) {
			return executeCondition_IN((Exec_Condition_IN) con, tempTuple, tempRecords);
		}
		else if (con instanceof Exec_Condition_ALL) {
			return executeCondition_ALL((Exec_Condition_ALL) con, tempTuple, tempRecords);
		}
		else if (con instanceof Exec_Condition_ANY) {
			return executeCondition_ANY((Exec_Condition_ANY) con, tempTuple, tempRecords);
		}
		else if (con instanceof Exec_Condition_Null) {
			return executeCondition_Null((Exec_Condition_Null) con, tempTuple, tempRecords);
		}
		return false;
	}
	
	public boolean executeCondition_Compare(Exec_Condition_Compare con, 
			Tuple tempTuple, Records tempRecords) {
		/*
		 * compareFormation:
		 * 
		 * 1: exp compare_op exp
		 * 2: exp compare_op LPAREN query RPAREN
		 * 3: LPAREN query RPAREN compare_op exp
		 * 4: LPAREN query RPAREN compare_op LPAREN query RPAREN
		 * 
		 */
		if (con.leftItem == null) {
			Main.main.AppendGuiOutput("Left part of the conditon is Incorrect!");
			return false;
		}
		if (con.rightItem == null) {
			Main.main.AppendGuiOutput("Right part of the conditon is Incorrect!");
			return false;
		}
		return Tools.Check_Condition(con.compareType, 
					executeItem(con.leftItem, tempTuple, tempRecords), 
					executeItem(con.rightItem, tempTuple, tempRecords));
	}
	
	public boolean executeCondition_NOT(Exec_Condition_NOT con, 
			Tuple tempTuple, Records tempRecords) {
		return !executeCondition(con.condition, tempTuple, tempRecords);
	}
	
	public boolean executeCondition_AND(Exec_Condition_AND con, 
			Tuple tempTuple, Records tempRecords) {
		
		if (!executeCondition(con.leftCondition, tempTuple, tempRecords)) {
			return false;
		}
		else return executeCondition(con.righCondition, tempTuple, tempRecords);
	}
	
	public boolean executeCondition_OR(Exec_Condition_OR con, 
			Tuple tempTuple, Records tempRecords) {
		
		if (executeCondition(con.leftCondition, tempTuple, tempRecords)) {
			return true;
		}
		else return executeCondition(con.righCondition, tempTuple, tempRecords);
	}
	
	public boolean executeCondition_IN(Exec_Condition_IN con,
			Tuple tempTuple, Records tempRecords) {
		
		int num = ++Temporary.Temp_Records.num;
		Records memory_tempRecords;
		
		// read from memory to see whether the temperory Records exists
		if (num > Temporary.Temp_Records.tempRecordsList.size()) {
			memory_tempRecords = executeExp(con.exp);
			Temporary.Temp_Records.tempRecordsList.add(memory_tempRecords);
		}
		else memory_tempRecords = Temporary.Temp_Records.tempRecordsList.get(num - 1);
		
		Metadata metadata = new Metadata();
		Exec_ItemList groupItemList = con.itemList;
		
		while (groupItemList != null) {
			metadata.Add_Attribute("", executeTools_CheckItemType(groupItemList.head, tempRecords));
			groupItemList = groupItemList.tail;
		}
		
		Tuple subTuple = executeTools_TupleProjection(con.itemList, tempTuple, tempRecords);
		
		return Tools.Tuple_In_Records(subTuple, metadata, memory_tempRecords);
		
	}
	
	public boolean executeCondition_ALL(Exec_Condition_ALL con,
			Tuple tempTuple, Records tempRecords) {
		
		Exec_Item_Const tempValue = executeItem(con.item, tempTuple, tempRecords);
		
		// read from memory to see whether the temperory Records exists
		int num = ++Temporary.Temp_Records.num;
		Records memory_tempRecords;
		
		if (num > Temporary.Temp_Records.tempRecordsList.size()) {
			memory_tempRecords = executeExp(con.exp);
			Temporary.Temp_Records.tempRecordsList.add(memory_tempRecords);
		}
		else memory_tempRecords = Temporary.Temp_Records.tempRecordsList.get(num - 1);
		
		if (memory_tempRecords.Get_ColumnNum() != 1) {
			// error it can only have one column
			System.out.println("In ALL condition, it can only have one column!");
			return false;
		}
		
		int rowNum = memory_tempRecords.Get_RowNum();
		
		if (rowNum == 0) return true;
		
		for (int i = 0; i < rowNum; i++) {
			Exec_Item_Const checkConst = new Exec_Item_Const(
					memory_tempRecords.Get_AttributeValue(i, 0), 
					Tools.String_to_Type(memory_tempRecords.Get_AttributeType(0)));
			
			if (!Tools.Check_Condition(con.compareType, tempValue, checkConst)) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean executeCondition_ANY(Exec_Condition_ANY con,
			Tuple tempTuple, Records tempRecords) {
		
		Exec_Item_Const tempValue = executeItem(con.item, tempTuple, tempRecords);
		
		// read from memory to see whether the temperory Records exists
		int num = ++Temporary.Temp_Records.num;
		Records memory_tempRecords;
		
		if (num > Temporary.Temp_Records.tempRecordsList.size()) {
			memory_tempRecords = executeExp(con.exp);
			Temporary.Temp_Records.tempRecordsList.add(memory_tempRecords);
		}
		else memory_tempRecords = Temporary.Temp_Records.tempRecordsList.get(num - 1);
		
		if (memory_tempRecords.Get_ColumnNum() != 1) {
			// error it can only have one column
			System.out.println("In ALL condition, it can only have one column!");
			return false;
		}
		
		int rowNum = memory_tempRecords.Get_RowNum();
		
		if (rowNum == 0) return false;
		
		for (int i = 0; i < rowNum; i++) {
			Exec_Item_Const checkConst = new Exec_Item_Const(
					memory_tempRecords.Get_AttributeValue(i, 0), 
					Tools.String_to_Type(memory_tempRecords.Get_AttributeType(0)));
			
			if (Tools.Check_Condition(con.compareType, tempValue, checkConst)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean executeCondition_Null(Exec_Condition_Null con, 
			Tuple tempTuple, Records tempRecords) {
		Exec_Item_Const tempValue = executeItem(con.item, tempTuple, tempRecords);
		if (con.Null_Or_Not) return (tempValue.value == "");
		else return (tempValue.value != "");
	}
	
	
/* *****************************************************************************************************************
 * Item
 */
	
	public Exec_Item_Const executeItem(Exec_Item item, Tuple tempTuple, Records tempRecords) {
		if (item instanceof Exec_Item_Const) {
			return executeItem_Const((Exec_Item_Const) item);
		}
		else if (item instanceof Exec_Item_Exp) {
			return executeItem_Exp((Exec_Item_Exp) item);
		}
		else if (item instanceof Exec_Item_Field) {
			return executeItem_Field((Exec_Item_Field) item, tempTuple, tempRecords);
		}
		else if (item instanceof Exec_Item_FieldDotField) {
			return executeItem_FieldDotField((Exec_Item_FieldDotField) item, tempTuple, tempRecords);
		}
		else if (item instanceof Exec_Item_Op) {
			return executeItem_Op((Exec_Item_Op) item, tempTuple, tempRecords);
		}
		else {
			// error
			System.out.println("Wrong Item!");
			return null;
		}
	}
	
	public Exec_Item_Const executeItem_Const(Exec_Item_Const item) {
		return item;
	}
	
	public Exec_Item_Const executeItem_Exp(Exec_Item_Exp item) {
		Records tempRecords = executeExp(item.exp);
		
		if ((tempRecords.Get_ColumnNum() != 1) || (tempRecords.Get_RowNum() != 1)) {
			// error, it must have exactly one value
			System.out.println("Exec_Item_Exp error!");
			return null;
		}
		
		return new Exec_Item_Const(tempRecords.Get_AttributeValue(0, 0),
				Tools.String_to_Type(tempRecords.Get_AttributeType(0)));
	}
	
	public Exec_Item_Const executeItem_Field(Exec_Item_Field item,
			Tuple tempTuple, Records tempRecords) {
		String name = item.name;
		
		int columnNum = tempRecords.Get_ColumnNum(), index = 0;
		
		for (index = 0; index < columnNum; index++) {
			if (name.equals(Tools.Name_Behind_Point(tempRecords.Get_SudoName(index)))) {
				break;
			}
		}
		
		if (index == columnNum) {
			// error
			Main.main.AppendGuiOutput(item.name + " Does not Exist in the table!");
			return null;
		}
		
		return new Exec_Item_Const(tempTuple.Get_AttributeValue(index),
				Tools.String_to_Type(tempRecords.Get_AttributeType(index)));
	}
	
	public Exec_Item_Const executeItem_FieldDotField(Exec_Item_FieldDotField item,
			Tuple tempTuple, Records tempRecords) {
		String name = item.Real_Name();
		
		int columnNum = tempRecords.Get_ColumnNum(), index = 0;
		
		for (index = 0; index < columnNum; index++) {
			if (name.equals(tempRecords.Get_SudoName(index))) {
				break;
			}
		}
		
		if (index == columnNum) {
			// error
			Main.main.AppendGuiOutput(item.Real_Name() + " Does not Exist in the table!");
			return null;
		}
		
		return new Exec_Item_Const(tempTuple.Get_AttributeValue(index),
				Tools.String_to_Type(tempRecords.Get_AttributeType(index)));
	}
	
	public Exec_Item_Const executeItem_Op(Exec_Item_Op item,
			Tuple tempTuple, Records tempRecords) {
		return Tools.Do_Item_Op(
				executeItem(item.leftItem, tempTuple, tempRecords), 
				executeItem(item.rightItem, tempTuple, tempRecords), 
				item.OpType);
	}
	
/* **************************************************************************************************************
 * Tools 
 */
	
	public Tuple executeTools_TupleProjection(Exec_ItemList IL, Tuple tempTuple, Records tempRecords) {
		Tuple newTuple = new Tuple();
		Exec_ItemList il = IL;
		while (il != null) {
			newTuple.Add_AttributeValue(
					executeItem(il.head, tempTuple, tempRecords).value);
			il = il.tail;
		}

		return newTuple; 
	}
	
	public String executeTools_CheckItemType(Exec_Item item, Records R) {
		if (item instanceof Exec_Item_Const) {
			return Tools.Type_to_String(((Exec_Item_Const) item).type);
		}
		else if (item instanceof Exec_Item_Exp) {
			return executeExp(((Exec_Item_Exp) item).exp).Get_AttributeType(0);
		}
		else if (item instanceof Exec_Item_Field) {
			int columnNum = R.Get_ColumnNum();
			for (int i = 0; i < columnNum; i++) {
				if (((Exec_Item_Field) item).name.equals(Tools.Name_Behind_Point(R.Get_AttributeName(i)))) {
					return R.Get_AttributeType(i);
				}
			}
			
			// error
			Main.main.AppendGuiOutput("Error: No such Field in the table!\r\nPlease check your Input!");
			return null;
		}
		else if (item instanceof Exec_Item_FieldDotField) {
			int columnNum = R.Get_ColumnNum();
			for (int i = 0; i < columnNum; i++) {
				if (((Exec_Item_FieldDotField) item).Real_Name().equals(R.Get_AttributeName(i))) {
					return R.Get_AttributeType(i);
				}
			}
			
			// error
			Main.main.AppendGuiOutput("Error: No such Field in the table!\r\nPlease check your Input!");
			return null;
		}
		else if (item instanceof Exec_Item_Op) {
			Exec_Item_Op tempItemOp = (Exec_Item_Op) item;
			String leftType = executeTools_CheckItemType(tempItemOp.leftItem, R);
			String rightType = executeTools_CheckItemType(tempItemOp.rightItem, R);
			
			if (Tools.String_to_Type(leftType) == Exec_Item_ConstType.Double) {
				return "DOUBLE";
			}
			else if (Tools.String_to_Type(leftType) == Exec_Item_ConstType.Int) {
				if (Tools.String_to_Type(rightType) == Exec_Item_ConstType.Double) {
					return "DOUBLE";
				}
				if (Tools.String_to_Type(rightType) == Exec_Item_ConstType.Int) {
					return "INT";
				}
				else {
					// error
					System.out.println("executeTools_CheckItemType fails!");
					return null;
				}
			}
			else if ((Tools.String_to_Type(leftType) == Exec_Item_ConstType.Boolean) &&
					(Tools.String_to_Type(rightType) == Exec_Item_ConstType.Boolean)) {
				return "BOOLEAN";
			}
			else if ((Tools.String_to_Type(leftType) == Exec_Item_ConstType.String) &&
					(Tools.String_to_Type(rightType) == Exec_Item_ConstType.String)) {
				return "CHAR";
			}
			else {
				// error
				System.out.println("executeTools_CheckItemType fails!");
				return null;
			}
		}
		else {
			// error
			System.out.println("executeTools_CheckItemType fails : no such item!");
			return null;
		}
	}
	
/* ************************************************************************************************************
 * Delete and Update 
 */
	
	public String executeStm_Delete(Exec_Stm_Delete stm) {
		
		String tablename = stm.tableName;
		DbTable dbt = null;
		int ty = 0;
		Collection<String> collection = null;
		try {
			collection = Main.main.database.lsTable();
		} catch (IOException e) {
			System.err.println("database lsTable error.");
			e.printStackTrace();
		}
		Object[] a = collection.toArray();
		for (Object o: a) {
			if (tablename.equals(o.toString()))
				ty = 1;
		}
		try {
			collection = Main.main.database.lsView();
		} catch (IOException e) {
			System.err.println("database lsTable error.");
			e.printStackTrace();
		}
		a = collection.toArray();
		for (Object o: a) {
			if (tablename.equals(o.toString()))
				ty = 2;
		}
		if (ty != 1) {
			System.err.println("Table " + tablename + " is not exist!");
			return null;
		}
		try {
			dbt = Main.main.database.openTable(tablename);
		} catch (IOException e) {
			System.err.println("Error: Table \'"+tablename+"\' does not exist!" );
		}
		Schema sc = dbt.getSchema();
		Records records = new Records();
		records.setSchema(sc);
		String type = null;
		for (int i = 0; i < sc.size(); i++) {
			Type t = sc.getType(i);
			String n = sc.getSymbol(i).toString();
			if (t.getCategory() == Type.Category.INTEGER)
				type = "INT";
			else if (t.getCategory() == Type.Category.CHARACTER ||
					t.getCategory() == Type.Category.VARY_LENGTH_CHARACTER)
				type = "CHAR(" + t.getLength() + ")";
			else if (t.getCategory() == Type.Category.FLOATING_POINT) {
				if (t.getLength() == 4 || t.getLength() == 8)
					type = "DOUBLE";
				else {
					int len1 = t.getLength()>>16;
					int len2 = t.getLength() - (len1<<16);
					type = "DECIMAL("+len1+", "+len2+")";
				}
			}
			records.Add_Attribute(n, type);
		}
		sc = dbt.getSchema();
		for (byte[] record : dbt) { // iterate on the table
			Tuple t = new Tuple();
			for (int i = 0; i < sc.size(); i++) {
				try {
					if (sc.getType(i).getCategory() == Type.Category.CHARACTER) //||
						//	sc.getType(i).getCategory() == Type.Category.VARY_LENGTH_CHARACTER)
						t.Add_AttributeValue(Tools.bytes2string((byte[])sc.getValue(record, i)));
					else
						t.Add_AttributeValue(sc.getValue(record, i).toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			records.Add_Tuple(t);
		}
		
		int columnNum = records.Get_ColumnNum();
		ArrayList<String> sudoNameList = new ArrayList<String>();
		for (int i = 0; i < columnNum; i++) {
			sudoNameList.add(stm.tableName + "." + records.Get_AttributeName(i));
		}
		records.Set_SudoNameList(sudoNameList);
		
		int count = 0;
		int rowNum = records.Get_RowNum();
		
		Sequential<byte[]> iter = dbt.iterator();
		while (iter.hasNext()) {
			byte[] record = iter.next();
			try {
				Integer id = (Integer) sc.getValue(record, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("delete error!");
			}
			if (executeCondition(stm.condition, records.Get_Tuple(count++), records)) { // condition test
				iter.remove(); // removal
				continue;
			}
		}
		dbt.close();
		return "Delete Succeed!";
	}
	
	public String executeStm_Update(Exec_Stm_Update stm) {
		
		String tablename = stm.newTableName;
		DbTable dbt = null;
		int ty = 0;
		Collection<String> collection = null;
		try {
			collection = Main.main.database.lsTable();
		} catch (IOException e) {
			System.err.println("database lsTable error.");
			e.printStackTrace();
		}
		Object[] a = collection.toArray();
		for (Object o: a) {
			if (tablename.equals(o.toString()))
				ty = 1;
		}
		try {
			collection = Main.main.database.lsView();
		} catch (IOException e) {
			System.err.println("database lsTable error.");
			e.printStackTrace();
		}
		a = collection.toArray();
		for (Object o: a) {
			if (tablename.equals(o.toString()))
				ty = 2;
		}
		if (ty != 1) {
			System.err.println("Table " + tablename + " is not exist!");
			return null;
		}
		try {
			dbt = Main.main.database.openTable(tablename);
		} catch (IOException e) {
			System.err.println("Error: Table \'"+tablename+"\' does not exist!" );
		}
		Schema sc = dbt.getSchema();
		Records records = new Records();
		records.setSchema(sc);
		String type = null;
		for (int i = 0; i < sc.size(); i++) {
			Type t = sc.getType(i);
			String n = sc.getSymbol(i).toString();
			if (t.getCategory() == Type.Category.INTEGER)
				type = "INT";
			else if (t.getCategory() == Type.Category.CHARACTER ||
					t.getCategory() == Type.Category.VARY_LENGTH_CHARACTER)
				type = "CHAR(" + t.getLength() + ")";
			else if (t.getCategory() == Type.Category.FLOATING_POINT) {
				if (t.getLength() == 4 || t.getLength() == 8)
					type = "DOUBLE";
				else {
					int len1 = t.getLength()>>16;
					int len2 = t.getLength() - (len1<<16);
					type = "DECIMAL("+len1+", "+len2+")";
				}
			}
			records.Add_Attribute(n, type);
		}
		sc = dbt.getSchema();
		for (byte[] record : dbt) { // iterate on the table
			Tuple t = new Tuple();
			for (int i = 0; i < sc.size(); i++) {
				try {
					if (sc.getType(i).getCategory() == Type.Category.CHARACTER) //||
						//	sc.getType(i).getCategory() == Type.Category.VARY_LENGTH_CHARACTER)
						t.Add_AttributeValue(Tools.bytes2string((byte[])sc.getValue(record, i)));
					else
						t.Add_AttributeValue(sc.getValue(record, i).toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			records.Add_Tuple(t);
		}
		
		int columnNum = records.Get_ColumnNum();
		ArrayList<String> sudoNameList = new ArrayList<String>();
		for (int i = 0; i < columnNum; i++) {
			sudoNameList.add(stm.newTableName + "." + records.Get_AttributeName(i));
		}
		records.Set_SudoNameList(sudoNameList);
		
		int count = 0; 
		int index = 0;
		
		for (index = 0; index < columnNum; index++) {
			if (stm.field instanceof Exec_Item_Field) {
				if (((Exec_Item_Field) stm.field).name.equals(
						Tools.Name_Behind_Point(records.Get_SudoName(index)))) {
					break;
				}
			}
			else if (stm.field instanceof Exec_Item_FieldDotField) {
				if (((Exec_Item_FieldDotField) stm.field).Real_Name().equals(
						records.Get_SudoName(index))) {
					break;
				}
			}
			else {
				// error
				System.out.println("Wrong fieldType!");
			}
		}
		
		int rowNum = records.Get_RowNum();
		
		Sequential<byte[]> iter = dbt.iterator();
		while (iter.hasNext()) {
			if (count == rowNum) break;
			
			byte[] record = iter.next();
			Integer id;
			try {
				id = (Integer) sc.getValue(record, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Update failed!");
			}
			if (executeCondition(stm.condition, records.Get_Tuple(count++), records)) {
				
				// convert a string to byte[], at most `len' bytes
				// change the column (in memory)
				try {
					record = sc.putValue(record, index, checkType(stm.newValue, sc.getType(index)));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("Update failed!");
				}
				// now `record' references new Record
				// replace the old by force (if cannot replace in-place, remove
				// the old and add one record)
				iter.replaceWithForce(record);
				// if the schema is fixed-length, replacing in-place will always
				// succeed
			}
		}
		dbt.close();
		return "Update Succeed!";
		
	}
	
/* **************************************************************************************************************
 * 	
 */
	/**
	 * Check the value and type
	 * @return null: error
	 * @return String: formatted value
	 */
	private Object checkType(Const value, Type t) {
		t.getCategory();
		if (t.getCategory() == Type.Category.INTEGER) {
			if (value instanceof ConstInt)
				return Integer.parseInt(value.toString());
			else {
				System.err.println("Error: Value \'" + value + "\' type does not match!");
				System.err.println("Error: Value \'" + value + "\' Shoule be an Integer!");
				return null;
			}
		}
		if (t.getCategory() == Type.Category.CHARACTER) {
			if (value instanceof ConstString) {
				String ret = value.toString();
				if (ret.length() > t.getLength())
					return value.toString().substring(0, t.getLength()).getBytes();
				else
					return ret.getBytes();
			}
			else {
				System.err.println("Error: Value \'" + value + "\' type does not match!");
				System.err.println("Error: Value \'" + value + "\' Shoule be a String!");
				return null;
			}
		}
		if (t.getCategory() == Type.Category.VARY_LENGTH_CHARACTER) {
			if (value instanceof ConstString) {
				String ret = value.toString();
				if (ret.length() > t.getLength())
					return value.toString().substring(0, t.getLength());
				else
					return ret;
			}
			else {
				System.err.println("Error: Value \'" + value + "\' type does not match!");
				System.err.println("Error: Value \'" + value + "\' Shoule be a String!");
				return null;
			}
		}
		if (t.getCategory() == Type.Category.FLOATING_POINT) {
			String v = value.toString();

			if (value instanceof ConstDouble || value instanceof ConstInt) {
				if (value instanceof ConstInt)
					v += ".0";
				float f = Float.parseFloat(v);
				if (t.getLength() == 4)
					return f;
				if (t.getLength() == 8)
					return Double.parseDouble(v);
				int len1 = t.getLength()>>16;
				int len2 = t.getLength() - (len1<<16);
				int dotpos = v.lastIndexOf('.');
				if (dotpos > len1) {
					String ret = "";
					for (int i = 0; i < len1; i++)
						ret += "9";
					ret += ".";
					for (int i = 0; i < len2; i++)
						ret += "9";
					return Float.parseFloat(ret);
				}
				String minStr = ".";
				for (int i = 0; i < len2-1; i++)
					minStr += "0";
				minStr += "1";
				if (f < Float.parseFloat(minStr))
					return new Float(0);
				if (len2 < v.length() - dotpos)
					return Float.parseFloat(v.substring(0, dotpos + len2 + 1));
				return f;
			}
			else {
				System.err.println("Error: Value \'" + value + "\' type does not match!");
				System.err.println("Error: Value \'" + value + "\' Shoule be a Float!");
				return null;
			}
		}
		return null;
	}
	
	
}
