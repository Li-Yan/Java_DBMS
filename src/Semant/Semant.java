/**
 * 
 */
package Semant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import qifeng.db.DbTable;
import qifeng.schema.Schema;
import qifeng.schema.SchemaBuilder;
import qifeng.schema.Type;
import qifeng.schema.Type.Category;

import Tools.Tools;

import Absyn.*;
import Constant.Const_For_Exec.*;
import ErrorMsg.ErrorMsg;
import Exec_Tree.*;

/**
 * @author Kiki
 *
 */
public class Semant {
	
	private ErrorMsg errorMsg;
	public Semant(ErrorMsg err) {
		errorMsg = err;
	}
	
	public Exec_ExprList transStmList(StmList stmList) {
		if (stmList == null) {
			return null;
		}
		Exec_Expr head = transStm(stmList.head);
		Exec_ExprList tail = transStmList(stmList.tail);
		return new Exec_ExprList(head, tail);
	}
	
	private Exec_Expr transStm(CreateTable stm) {
		SchemaBuilder sb = new SchemaBuilder();
		AttributeList al = stm.attr_list;
		String key = null;
		while(al != null) {
			Attribute attr = al.head;
			if (attr.isSetKey) {
				if (key != null) {
					System.err.println("Error: Multiple Set Primary Key Statements.");
					return null;
				}
				key = ((ExpId)attr.id_list.head).toString();
				Type tmp = sb.get(key);
				if (tmp == null) {
					System.err.println("Create Table Error! "+key+" does not exist!");
					return null;
				}
				sb.replace(key, sb.get(key).getCategory(), sb.get(key).getLength(), Type.Attribute.PRIMARY_KEY);
				al = al.tail;
				continue;
			}
			switch (attr.type.type) {
			case Parse.sym.INT:
				if (attr.isPrimaryKey) {
					if (key != null) {
						System.err.println("Error: Multiple Primary Key Attribute.");
						return null;
					}
					key = attr.name.toString();
					sb.add(attr.name.toString(), Type.Category.INTEGER, 4, Type.Attribute.PRIMARY_KEY);
					break;
				}
				if (attr.isNotNull) {
					sb.add(attr.name.toString(), Type.Category.INTEGER, 4, Type.Attribute.NOT_NULL);
					break;
				}
				if (attr.defaultvalue != null) {
					sb.add(attr.name.toString(), Type.Category.INTEGER, 4, 
							Type.Attribute.newDefault(checkType(attr.defaultvalue, new Type(Type.Category.INTEGER, 4))));
					break;
				}
				sb.add(attr.name.toString(), Type.Category.INTEGER, 4);
				break;
			case Parse.sym.DOUBLE:
				if (attr.isPrimaryKey) {
					if (key != null) {
						System.err.println("Error: Multiple Primary Key Attribute.");
						return null;
					}
					key = attr.name.toString();
					sb.add(attr.name.toString(), Type.Category.FLOATING_POINT, 8, Type.Attribute.PRIMARY_KEY);
					break;
				}
				if (attr.isNotNull) {
					sb.add(attr.name.toString(), Type.Category.FLOATING_POINT, 8, Type.Attribute.NOT_NULL);
					break;
				}
				if (attr.defaultvalue != null) {
					sb.add(attr.name.toString(), Type.Category.FLOATING_POINT, 8,
							Type.Attribute.newDefault(checkType(attr.defaultvalue, new Type(Type.Category.FLOATING_POINT, 8))));
					break;
				}
				sb.add(attr.name.toString(), Type.Category.FLOATING_POINT, 8);
				break;
			case Parse.sym.DECIMAL:
				int len1 = ((DecimalType) attr.type).lenth1;
				int len2 = ((DecimalType) attr.type).lenth2;
				if (attr.isPrimaryKey) {
					if (key != null) {
						System.err.println("Error: Multiple Primary Key Attribute.");
						return null;
					}
					key = attr.name.toString();
					sb.add(attr.name.toString(), Type.Category.FLOATING_POINT, (len1<<16)+len2, Type.Attribute.PRIMARY_KEY);
					break;
				}
				if (attr.isNotNull) {
					sb.add(attr.name.toString(), Type.Category.FLOATING_POINT, (len1<<16)+len2, Type.Attribute.NOT_NULL);
					break;
				}
				if (attr.defaultvalue != null) {
					sb.add(attr.name.toString(), Type.Category.FLOATING_POINT, (len1<<16)+len2,
							Type.Attribute.newDefault(checkType(attr.defaultvalue, new Type(Type.Category.FLOATING_POINT, (len1<<16)+len2))));
					break;
				}
				sb.add(attr.name.toString(), Type.Category.FLOATING_POINT, (len1<<16)+len2);
				break;
			case Parse.sym.CHAR:
				sb.add(attr.name.toString(), Type.Category.CHARACTER, ((CharType) attr.type).lenth);
				if (attr.isPrimaryKey) {
					if (key != null) {
						System.err.println("Error: Multiple Primary Key Attribute.");
						return null;
					}
					key = attr.name.toString();
					sb.add(attr.name.toString(), Type.Category.CHARACTER, ((CharType) attr.type).lenth, Type.Attribute.PRIMARY_KEY);
					break;
				}
				if (attr.isNotNull) {
					sb.add(attr.name.toString(), Type.Category.CHARACTER, ((CharType) attr.type).lenth, Type.Attribute.NOT_NULL);
					break;
				}
				if (attr.defaultvalue != null) {
					sb.add(attr.name.toString(), Type.Category.CHARACTER, ((CharType) attr.type).lenth,
							Type.Attribute.newDefault(checkType(attr.defaultvalue, 
									new Type(Type.Category.CHARACTER, ((CharType) attr.type).lenth))));
					break;
				}
				sb.add(attr.name.toString(), Type.Category.CHARACTER, ((CharType) attr.type).lenth);
				break;
			case Parse.sym.VARCHAR:
				if (attr.isPrimaryKey) {
					if (key != null) {
						System.err.println("Error: Multiple Primary Key Attribute.");
						return null;
					}
					key = attr.name.toString();
					sb.add(attr.name.toString(), Type.Category.VARY_LENGTH_CHARACTER, ((CharType) attr.type).lenth, Type.Attribute.PRIMARY_KEY);
					break;
				}
				if (attr.isNotNull) {
					sb.add(attr.name.toString(), Type.Category.VARY_LENGTH_CHARACTER, ((CharType) attr.type).lenth, Type.Attribute.NOT_NULL);
					break;
				}
				if (attr.defaultvalue != null) {
					sb.add(attr.name.toString(), Type.Category.VARY_LENGTH_CHARACTER, ((CharType) attr.type).lenth,
							Type.Attribute.newDefault(checkType(attr.defaultvalue, 
									new Type(Type.Category.VARY_LENGTH_CHARACTER, ((CharType) attr.type).lenth))));
					break;
				}
				sb.add(attr.name.toString(), Type.Category.VARY_LENGTH_CHARACTER, ((CharType) attr.type).lenth);
				break;
			default:
				System.err.println("Unsupported Attribute Type!");
				break;
			}
			al = al.tail;
		}
		Schema sc = sb.newSchema();
		try {
			Main.main.bm.sync();
			if(key != null)
				Main.main.database.createTable(stm.name.toString(), sc, key);
			else
				Main.main.database.createTable(stm.name.toString(), sc);
		} catch (IOException e) {
			System.err.println("Create Table Error! Table Exist!");
		} finally {
			Main.main.bm.sync();
		}
		Main.main.AppendGuiOutput("Create table succeed!");
		Main.main.RefreshTree();
		return null;
	}

	private Exec_Expr transStm(ShowTable stm) {
		Collection<String> collection = null;
		try {
			collection = Main.main.database.lsTable();
		} catch (IOException e) {
			System.err.println("database lsTable error.");
			e.printStackTrace();
		}
		if (collection.size() == 0) {
			Main.main.AppendGuiOutput("There's no tables in the database!");
			return null;
		}
		String string = "Tables in the database:\r\n";
		Object[] a = collection.toArray();
		for (Object o: a) {
			string += "\t" + o.toString() + "\r\n";
		}
		Main.main.AppendGuiOutput(string);
		return null;
	}
	
	private Exec_Expr transStm(ShowView stm) {
		Collection<String> collection = null;
		try {
			collection = Main.main.database.lsView();
		} catch (IOException e) {
			System.err.println("database lsView error.");
			e.printStackTrace();
		}
		if (collection.size() == 0) {
			Main.main.AppendGuiOutput("There's no views in the database!");
			return null;
		}
		String string = "Views in the database:\r\n";
		Object[] a = collection.toArray();
		for (Object o: a) {
			string += "\t" + o.toString() + "\r\n";
		}
		Main.main.AppendGuiOutput(string);
		return null;
	}
	
	private Exec_Expr transStm(AlterTable stm) {
		// TODO
		switch (stm.AddOrDrop) {
		case 1:	//	ADD
		case 2:	//	DROP
		}
		return null;
	}
	
	private Exec_Expr transStm(CreateDB stm) {
		// TODO
		return null;
	}
	
	private Exec_Expr transStm(CreateIndex stm) {
		String tablename = stm.table.toString();
		String indexname = stm.index.toString();
		ArrayList<String> ids = new ArrayList<String>();
		IdList idList = stm.id_list;
		while (idList!= null) {
			ids.add(((ExpId) idList.head).name.toString());
			idList = idList.tail;
		}
		DbTable dbt = null;
		try {
			dbt = Main.main.database.openTable(tablename);
		} catch (IOException e) {
			System.err.println("Table " + tablename + " does not exist!");
			e.printStackTrace();
		}
		try {
			// TODO support multiple index
			dbt.createIndexOn(ids.get(0));
		} catch (IOException e) {
			System.err.println("Error occurred when creating index!");
			e.printStackTrace();
		}
		dbt.close();
		Main.main.AppendGuiOutput("Create Index succeed!");
		return null;
	}
	
	private Exec_Expr transStm(CreateView stm) {
		// TODO
		String viewname = stm.name.toString();
		if (stm.id_list == null) {	//CREATE:c VIEW ID:i AS query:q 
			String query = stm.query.toString() + ";";
			try {
				Main.main.bm.sync();
				Main.main.database.createView(viewname, query);
				Main.main.bm.sync();
			} catch (IOException e) {
				System.err.println("Create View Error!");
				e.printStackTrace();
			}
		}
		else {
			// TODO rename
		}
		Main.main.AppendGuiOutput("Create View Succeed!");
		Main.main.RefreshTree();
		return null;
	}
	
	private Exec_Expr transStm(Delete stm) {
		if (stm.where != null) {
			return new Exec_Stm_Delete(stm.name.toString(),
					transWherePart(stm.where));
		}
		else {
			return new Exec_Stm_Delete(stm.name.toString(),
					null);
		}
	}
	
	private Exec_Expr transStm(DropDB stm) {
		// TODO
		return null;
	}
	
	private Exec_Expr transStm(Update stm) {
		if (stm.refname != null) {
			if (stm.where != null) {
				return new Exec_Stm_Update(stm.name.toString(), stm.refname.toString(), 
						transExp(stm.lvalue), stm.value, transWherePart(stm.where));
			}
			else {
				return new Exec_Stm_Update(stm.name.toString(), stm.refname.toString(), 
						transExp(stm.lvalue), stm.value, null);
			}
		}
		else {
			if (stm.where != null) {
				return new Exec_Stm_Update(stm.name.toString(), stm.name.toString(), 
						transExp(stm.lvalue), stm.value, transWherePart(stm.where));
			}
			else {
				return new Exec_Stm_Update(stm.name.toString(), stm.name.toString(), 
						transExp(stm.lvalue), stm.value, null);
			}
		}
	}
	
	private Exec_Expr transStm(UseDB stm) {
		// TODO
		return null;
	}
	private Exec_Expr transStm(DropIndex stm) {
		String tablename = stm.table.toString();
		String indexname = stm.index.toString();
		DbTable dbt = null;
		try {
			dbt = Main.main.database.openTable(tablename);
		} catch (IOException e) {
			System.err.println("Table " + tablename + " does not exist!");
			e.printStackTrace();
		}
		try {
			Main.main.bm.sync();
			dbt.deleteIndexOn(indexname);
		} catch (IOException e) {
			System.err.println("Error occurred when creating index!");
			e.printStackTrace();
		}
		dbt.close();
		Main.main.AppendGuiOutput("Drop Index succeed!");
		return null;
	}
	
	private Exec_Expr transStm(DropTable stm) {
		IdList ids = stm.id_list;
		int cnt = 0;
		while (ids != null) {
			String name = ((ExpId) ids.head).name.toString();
			try {
				Main.main.bm.sync();
				Main.main.database.delTable(name);
				Main.main.bm.sync();
			} catch (IOException e) {
				System.err.println("Error: Table \'"+name+"\' does not exist!" );
			}
			ids = ids.tail;
			cnt ++;
		}
		Main.main.AppendGuiOutput("Drop Table succeed!\r\n" + cnt + " table(s) have dropped!");
		Main.main.RefreshTree();
		return null;
	}
	
	private Exec_Expr transStm(DropView stm) {
		IdList ids = stm.id_list;
		int cnt = 0;
		while (ids != null) {
			String name = ((ExpId) ids.head).name.toString();
			try {
				Main.main.database.delView(name);
				Main.main.bm.sync();
			} catch (IOException e) {
				System.err.println("Error: View \'"+name+"\' does not exist!" );
			}
			ids = ids.tail;
			cnt ++;
		}
		Main.main.AppendGuiOutput("Drop View succeed!\r\n" + cnt + " view(s) have dropped!");
		Main.main.RefreshTree();
		return null;
	}
	
	private Exec_Expr transStm(Insert stm) {
		/**
		 * Insert Statement type:
		 * 	1:	INSERT INTO ID VALUES values_part
		 * 	2:	INSERT INTO ID query
		 * 	3:	INSERT INTO ID LPAREN id_list RPAREN VALUES values_part
		 * 	4:	INSERT INTO ID LPAREN id_list RPAREN query
		 */
		String tablename = stm.name.toString();
		DbTable dbt = null;
		int cnt = 0;
		try {
			dbt = Main.main.database.openTable(tablename);
		} catch (IOException e) {
			System.err.print(e.getMessage());
			System.err.println("Error: Table \'"+tablename+"\' does not exist!" );
		}
		Schema sc = dbt.getSchema();
		switch(stm.type) {
		case 1:	// INSERT INTO ID VALUES values_part
			ValuesPart values = stm.values;
			int num = 0;
			while (values != null) {
				ValueList valueList = values.head;
				ArrayList<Object> oo = new ArrayList<Object>();
				while (valueList != null) {
					Const value = valueList.head;
					Type t = sc.getType(num);
					oo.add(checkType(value, t));
					valueList = valueList.tail;
					num ++;
				}
				try {
					dbt.add(sc.newValues(oo.toArray()));
				} catch (IOException e) {
					System.err.println("Error occured when insert into table!");
					return null;
				} catch (IllegalArgumentException e) {
					System.err.println(e.getMessage());
					Main.main.AppendGuiOutput("Error: insert failed. " + e.getMessage());
					return null;
				}
				values = values.tail;
				cnt ++;
			}
			break;
		case 2:	// INSERT INTO ID query
			// TODO
			break;
		case 3:	// INSERT INTO ID LPAREN id_list RPAREN VALUES values_part
			break;
		case 4:	// INSERT INTO ID LPAREN id_list RPAREN query
			break;
		}
		System.out.println("Insert count = " + Main.main.insertcnt++);
		dbt.close();
		Main.main.AppendGuiOutput(cnt + " Tuples have been inserted!");
		return null;
	}
	
	/**
	 * Check the value and type
	 * @return null: error
	 * @return String: formatted value
	 */
	private Object checkType(Const value, Type t) {
		t.getCategory();
		if (value instanceof ConstNull)
			return null;
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
	
	private Exec_Exp transStm(JoinQuery stm) {
		Exec_Exp newExp;
		newExp = new Exec_Exp_From_Join(
				new Exec_Exp_Records(stm.table1.toString()),
				new Exec_Exp_Records(stm.table2.toString()),
				stm.type);
		
		if (stm.on != null) {
			newExp = new Exec_Exp_Selection(transConditions(stm.on.conditions), newExp);
		}
		ArrayList<Exec_Item> itemList = null;
		ArrayList<String> newNameList = null;
		Object[] projectionObjects = new Object[3];
		transFieldList(stm.field_list, projectionObjects);
		itemList = (ArrayList<Exec_Item>) projectionObjects[0];
		newNameList = (ArrayList<String>) projectionObjects[2];
		newExp = new Exec_Exp_Projection_NoAgg(itemList, null, newNameList, newExp);
		if (stm.isDistinct) {
			newExp = new Exec_Exp_Distinct(newExp);
		}
		return newExp;
	}
	
	private Exec_Exp transStm(SetQuery stm) {
		Exec_Exp leftExp = transStm(stm.query1);
		Exec_Exp rightExp = transStm(stm.query2);
		
		Exec_Exp newExp = null;
		
		if (stm.type <= 2 ) newExp = new Exec_Exp_Union(leftExp, rightExp, stm.type == 2);
		else if (stm.type <= 4) newExp = new Exec_Exp_Intersect(leftExp, rightExp, stm.type == 4);
		else newExp = new Exec_Exp_Except(leftExp, rightExp, stm.type == 6);
		
		if (stm.type % 2 == 0) newExp = new Exec_Exp_Distinct(newExp);
		
		return newExp;
	}
	
	@SuppressWarnings("unchecked")
	private Exec_Exp transStm(SimpleQuery query) {
		Exec_Exp newExp;
		// first get tables according to TableList
		newExp = transTableList(query.table_list);
		// do selection according to WherePart
		if (query.where != null)
			newExp = new Exec_Exp_Selection(transWherePart(query.where), newExp);
		// do order by
		if (query.order != null) {
			Object[] orderObjects = new Object[2];
			transOrderPart(query.order, orderObjects);
			newExp = new Exec_Exp_OrderBy(
					(ArrayList<Exec_Item>) orderObjects[0],
					(ArrayList<Boolean>) orderObjects[1],
					newExp);
		}
		// do group according to GroupPart
		Exec_ItemList groupItemList = null;
		if (query.group != null) {
			groupItemList = transGroupPart(query.group);
			newExp = new Exec_Exp_Group(groupItemList, newExp);
		}
		// do having according to HavingPart
		if (query.having != null) {
			newExp = new Exec_Exp_Having(transHavingPart(query.having), groupItemList, newExp);
		}
		// do projection according to 
		ArrayList<Exec_Item> itemList = null;
		ArrayList<String> aggOpList = null;
		ArrayList<String> newNameList = null;
		Object[] projectionObjects = new Object[3];
		if (transFieldList(query.field_list, projectionObjects)) {
			itemList = (ArrayList<Exec_Item>) projectionObjects[0];
			aggOpList = (ArrayList<String>) projectionObjects[1];
			newNameList = (ArrayList<String>) projectionObjects[2];
			newExp = new Exec_Exp_Projection_Agg(itemList, aggOpList, groupItemList, newNameList, newExp);
		}
		else {
			itemList = (ArrayList<Exec_Item>) projectionObjects[0];
			aggOpList = (ArrayList<String>) projectionObjects[1];
			newNameList = (ArrayList<String>) projectionObjects[2];
			newExp = new Exec_Exp_Projection_NoAgg(itemList, groupItemList, newNameList, newExp);
		}
		if (query.isDistinct) {
			newExp = new Exec_Exp_Distinct(newExp);
		}
		return newExp;
	}
	
	private Exec_Condition transHavingPart(HavingPart having) {
		return transConditions(having.conditions);
	}

	private void transOrderPart(OrderPart orderpart, Object[] ob) {
		OrderByList orderByList = orderpart.order_list;
		ArrayList<Exec_Item> itemList = new ArrayList<Exec_Item>();
		ArrayList<Boolean> isDownList = new ArrayList<Boolean>();
		
		while (orderByList != null) {
			itemList.add(transExp(orderByList.head.exp));
			isDownList.add(!orderByList.head.isDesc);
			orderByList = orderByList.tail;
		}
		ob[0] = itemList;
		ob[1] = isDownList;
	}

	private boolean transFieldList(FieldList fl, Object[] ob) {	
		// return whether it should do aggregate
		boolean isAggregate = false;
		ArrayList<Exec_Item> projectionItemList = new ArrayList<Exec_Item>();
		ArrayList<String> newNameList = new ArrayList<String>();
		ArrayList<String> aggOpList = new ArrayList<String>();
		FieldList fieldList = fl;
		
		while (fieldList != null) {
			Field field = fieldList.head;
			
			if (field.exp instanceof Function) {
				isAggregate = true;
				Function functionExp = (Function) field.exp;
				projectionItemList.add(transExp(functionExp.exp));
				switch(functionExp.type) {
				case Parse.sym.COUNT:
					aggOpList.add("COUNT");		break;
				case Parse.sym.SUM:
					aggOpList.add("SUM");		break;
				case Parse.sym.AVG:
					aggOpList.add("AVG");		break;
				case Parse.sym.MIN:
					aggOpList.add("MIN");		break;
				case Parse.sym.MAX:
					aggOpList.add("MAX");		break;
				}
			}
			else {
				projectionItemList.add(transExp(field.exp));
				aggOpList.add("");
			}
			if (field.refname != null)
				newNameList.add(field.refname.toString());
			else
				newNameList.add(field.toString());
			fieldList = fieldList.tail;
		}
		ob[0] = projectionItemList;
		ob[1] = aggOpList;
		ob[2] = newNameList;
		return isAggregate;
	}

	private Exec_ItemList transGroupPart(GroupPart group) {
		Exec_ItemList itemList = null, tail = null;
		IdList idList = group.id_list;
		while (idList != null) {
			if (itemList == null) {
				itemList = tail = new Exec_ItemList(transExp(idList.head), null);
			}
			else {
				tail = tail.tail = new Exec_ItemList(transExp(idList.head), null);
			}
			idList = idList.tail;
		}
		return itemList;
	}

	private Exec_Exp transTableList(TableList tableList) {
		ArrayList<Exec_Exp_Records> recordsList = new ArrayList<Exec_Exp_Records>();
		ArrayList<String> newNameList = new ArrayList<String>();
		TableList tmp = tableList;
		Set<String> nameSet = new HashSet<String>();
		
		while (tmp != null) {
			Table table = tmp.head;
			Exec_Exp_Records newExecExpRecords = new Exec_Exp_Records(table.name.toString());
			recordsList.add(newExecExpRecords);
			String refname = table.refname.toString();
			if (nameSet.contains(refname)) {
				errorMsg.error(table.pos, "Not Unique Table/Alias: "+refname);
				return null;
			}
			newNameList.add(table.refname.toString());
			tmp = tmp.tail;
		}
		return new Exec_Exp_From(recordsList, newNameList);
	}

	private Exec_Condition transWherePart(WherePart where) {
	//	if (where == null)
		return transConditions(where.conditions);
	}

	private Exec_Condition transConditions(Conditions conditions) {
		switch (conditions.op) {
		case 0:	// Single condition
			return transCondition(conditions.condition);
		case 1:	// AND
			return new Exec_Condition_AND(transConditions(conditions.left),
					transConditions(conditions.right));
		case 2:	// OR
			return new Exec_Condition_OR(transConditions(conditions.left),
					transConditions(conditions.right));
		case 3:	// NOT
			return new Exec_Condition_NOT(transConditions(conditions.left));
		default:
			return null;
		}
	}

	private Exec_Condition transCondition(Condition condition) {
		switch(condition.type) {
		case 1:
			return new Exec_Condition_Compare(
					transExp(condition.exp1),
					transExp(condition.exp2),
					Tools.Convert_CompareType(condition.compareop), 
					condition.type);
		case 2:
			return new Exec_Condition_Compare(
					transExp(condition.exp1),
					new Exec_Item_Exp(transStm(condition.query1)),
					Tools.Convert_CompareType(condition.compareop),
					condition.type);
		case 3:
			return new Exec_Condition_Compare(
					new Exec_Item_Exp(transStm(condition.query1)),
					transExp(condition.exp1),
					Tools.Convert_CompareType(condition.compareop),
					condition.type);
		case 4:
			return new Exec_Condition_Compare(
					new Exec_Item_Exp(transStm(condition.query1)),
					new Exec_Item_Exp(transStm(condition.query2)),
					Tools.Convert_CompareType(condition.compareop),
					condition.type);
		case 5:
			return new Exec_Condition_ANY(
					transExp(condition.exp1),
					transStm(condition.query1),
					Tools.Convert_CompareType(condition.compareop));
		case 6:
			return new Exec_Condition_ALL(
					transExp(condition.exp1),
					transStm(condition.query1),
					Tools.Convert_CompareType(condition.compareop));
		case 7:	// exp IN LPAREN query RPAREN
			return new Exec_Condition_IN(
					transExpList(condition.exp_list),
					transStm(condition.query1));
		case 8:	// exp NOT IN LPAREN query RPAREN
			return new Exec_Condition_NOT(new Exec_Condition_IN(
					transExpList(condition.exp_list),
					transStm(condition.query1)));
		case 9:	// exp IS NULL
		case 10:	//exp IS NOT NULL
				/*
				 * 9:	exp IS NULL
				 * 10:	exp IS NOT NULL
				 */
			return new Exec_Condition_Null(transExp(condition.exp1), condition.type == 9);
			
		case 11:	//exp BETWEEN exp AND exp
			Exec_Condition_Compare leftCompare = new Exec_Condition_Compare(
					transExp(condition.exp1),
					transExp(condition.exp2),
					Tools.Convert_CompareType(Parse.sym.GE),
					1);
			Exec_Condition_Compare rightCompare = new Exec_Condition_Compare(
					transExp(condition.exp1),
					transExp(condition.exp3),
					Tools.Convert_CompareType(Parse.sym.LT),
					1);
			
			return new Exec_Condition_AND(leftCompare, rightCompare);
		default:
			return null;
		}
	}

	private Exec_ItemList transExpList(ExpList exp) {
		if (exp == null) {
			return null;
		}
		Exec_Item head = transExp(exp.head);
		Exec_ItemList tail = transExpList(exp.tail);
		return new Exec_ItemList(head, tail);
	}

	private Exec_Item transExp(ExpId exp) {
		return new Exec_Item_Field(exp.name.toString());
	}
	
	private Exec_Item transExp(ExpIdDotId exp) {
		return new Exec_Item_FieldDotField(exp.name1.toString(), exp.name2.toString());
	}
	
	private Exec_Item transExp(ExpOp exp) {
		return new Exec_Item_Op(
				transExp(exp.left),
				transExp(exp.right),
				Exec_Item_OpType.values()[exp.type - 1]);
	}
	
	private Exec_Item transExp(ExpStar exp) {
		return new Exec_Item_Field_Star();
	}
	
	private Exec_Item transExp(Function exp) {
		String aggOpString = null;
		switch(exp.type) {
		case Parse.sym.COUNT:
			aggOpString = "COUNT";		break;
		case Parse.sym.SUM:
			aggOpString = "SUM";		break;
		case Parse.sym.AVG:
			aggOpString = "AVG";		break;
		case Parse.sym.MIN:
			aggOpString = "MIN";		break;
		case Parse.sym.MAX:
			aggOpString = "MAX";		break;
		}
		Exp newExp = exp.exp;
		if (newExp instanceof ExpId)
			return new Exec_Item_Field_Agg(((ExpId) newExp).name.toString(), aggOpString); 
		if (newExp instanceof ExpIdDotId)
			return new Exec_Item_FieldDotField_Agg(((ExpIdDotId) newExp).name1.toString(), 
					((ExpIdDotId) newExp).name2.toString(), aggOpString);
		return null;
	}
	private Exec_Item transExp(Exp exp) {
		if (exp instanceof ExpId)
			return transExp((ExpId) exp);
		if (exp instanceof ExpIdDotId)
			return transExp((ExpIdDotId) exp);
		if (exp instanceof ExpOp)
			return transExp((ExpOp) exp);
		if (exp instanceof ExpStar)
			return transExp((ExpStar) exp);
		if (exp instanceof Function)
			return transExp((Function) exp);
		// Const
		if (exp instanceof ConstBool)
			return new Exec_Item_Const(exp.toString(),
					Exec_Item_ConstType.Boolean);
		if (exp instanceof ConstInt)
			return new Exec_Item_Const(exp.toString(),
					Exec_Item_ConstType.Int);
		if (exp instanceof ConstDouble)
			return new Exec_Item_Const(exp.toString(),
					Exec_Item_ConstType.Double);
		if (exp instanceof ConstString)
			return new Exec_Item_Const(exp.toString(),
					Exec_Item_ConstType.String);
		return null;
	}

	private Exec_Exp transStm(Query query) {
		if (query instanceof JoinQuery)
			return transStm((JoinQuery)query);
		if (query instanceof SetQuery)
			return transStm((SetQuery)query);
		if (query instanceof SimpleQuery)
			return transStm((SimpleQuery)query);
		return null;
	}
	

	
	public Exec_Expr transStm(Stm stm) {
		if (stm instanceof CreateTable)
			return transStm((CreateTable) stm);
		if (stm instanceof AlterTable)
			return transStm((AlterTable) stm);
		if (stm instanceof CreateDB)
			return transStm((CreateDB) stm);
		if (stm instanceof CreateIndex)
			return transStm((CreateIndex) stm);
		if (stm instanceof CreateView)
			return transStm((CreateView) stm);
		if (stm instanceof Delete)
			return transStm((Delete) stm);
		if (stm instanceof DropDB)
			return transStm((DropDB) stm);
		if (stm instanceof DropIndex)
			return transStm((DropIndex) stm);
		if (stm instanceof DropTable)
			return transStm((DropTable) stm);
		if (stm instanceof DropView)
			return transStm((DropView) stm);
		if (stm instanceof Insert)
			return transStm((Insert) stm);
		if (stm instanceof Query)
			return transStm((Query) stm);
		if (stm instanceof Update)
			return transStm((Update) stm);
		if (stm instanceof UseDB)
			return transStm((UseDB) stm);
		if (stm instanceof ShowTable)
			return transStm((ShowTable) stm);
		if (stm instanceof ShowView)
			return transStm((ShowView) stm);
		return null;
	}
}
