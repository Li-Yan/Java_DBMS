package Translate;

import java.util.ArrayList;

import Absyn.*;
import Constant.Const_For_Exec.*;
import Exec_Tree.*;
import Tools.*;

public class Translate {
	
	public Translate() {}
	
	public Exec_Expr translate(Stm stm) {
		if (stm instanceof Query) {
			return translateQuery((Query) stm);
		}
		else if (stm instanceof Delete) {
			return translateStm_Delete((Delete) stm);
		}
		else if (stm instanceof Update) {
			return translateStm_Update((Update) stm);
		}
		else {
			// error
			return null;
		}
	}
	
/* ******************************************************************************************************
 * Delete and Update
 */
	
	public Exec_Stm_Delete translateStm_Delete(Delete stm) {
		if (stm.where != null) {
			return new Exec_Stm_Delete(stm.name.toString(),
					translateWherePart(stm.where));
		}
		else {
			return new Exec_Stm_Delete(stm.name.toString(),
					null);
		}
	}
	
	public Exec_Stm_Update translateStm_Update(Update stm) {
		if (stm.refname != null) {
			if (stm.where != null) {
				return new Exec_Stm_Update(stm.name.toString(), stm.refname.toString(), 
						translateItem(stm.lvalue), stm.value, translateWherePart(stm.where));
			}
			else {
				return new Exec_Stm_Update(stm.name.toString(), stm.refname.toString(), 
						translateItem(stm.lvalue), stm.value, null);
			}
		}
		else {
			if (stm.where != null) {
				return new Exec_Stm_Update(stm.name.toString(), stm.name.toString(), 
						translateItem(stm.lvalue), stm.value, translateWherePart(stm.where));
			}
			else {
				return new Exec_Stm_Update(stm.name.toString(), stm.name.toString(), 
						translateItem(stm.lvalue), stm.value, null);
			}
		}
	}
	
/* *******************************************************************************************************
 * Query
 */
	
	public Exec_Exp translateQuery(Query query) {
		if (query instanceof SimpleQuery) {
			return translateQuery_SimpleQuery((SimpleQuery) query);
		}
		else if (query instanceof SetQuery) {
			return translateQuery_SetQuery((SetQuery) query);
		}
		else if (query instanceof JoinQuery) {
			return translateQuery_JoinQuery((JoinQuery) query);
		}
		else {
			// error
			return null;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public Exec_Exp translateQuery_SimpleQuery(SimpleQuery query) {
		Exec_Exp newExp;
		
		// first get tables according to TableList
		newExp = translateTableList(query.table_list);
		
		// do selection according to WherePart
		if (query.where != null) {
			newExp = new Exec_Exp_Selection(translateWherePart(query.where), newExp);
		}
		
		// do order by
		if (query.order != null) {
			Object[] orderObjects = new Object[2];
			translateOrderPart(query.order, orderObjects);
			newExp = new Exec_Exp_OrderBy(
					(ArrayList<Exec_Item>) orderObjects[0],
					(ArrayList<Boolean>) orderObjects[1],
					newExp);
		}
		
		// do group according to GroupPart
		Exec_ItemList groupItemList = null;
		if (query.group != null) {
			groupItemList = translateGroupPart(query.group);
			newExp = new Exec_Exp_Group(groupItemList, newExp);
		}	
		// do having according to HavingPart
		if (query.having != null) {
			newExp = new Exec_Exp_Having(translateHavingPart(query.having), groupItemList, newExp);
		}	
		// do projection according to 
		ArrayList<Exec_Item> itemList = null;
		ArrayList<String> aggOpList = null;
		ArrayList<String> newNameList = null;
		Object[] projectionObjects = new Object[3];
		if (translateFieldList(query.field_list, projectionObjects)) {
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
	
	public Exec_Exp translateQuery_SetQuery(SetQuery query) {
		Exec_Exp leftExp = translateQuery(query.query1);
		Exec_Exp rightExp = translateQuery(query.query2);
		
		Exec_Exp newExp = null;
		
		if (query.type <= 2 ) newExp = new Exec_Exp_Union(leftExp, rightExp, query.type == 2);
		else if (query.type <= 4) newExp = new Exec_Exp_Intersect(leftExp, rightExp, query.type == 4);
		else newExp = new Exec_Exp_Except(leftExp, rightExp, query.type == 6);
		
		if (query.type % 2 == 0) newExp = new Exec_Exp_Distinct(newExp);
		
		return newExp;
	}
	
	@SuppressWarnings("unchecked")
	public Exec_Exp translateQuery_JoinQuery(JoinQuery query) {
		Exec_Exp newExp;
		
		newExp = new Exec_Exp_From_Join(
				new Exec_Exp_Records(query.table1.toString()),
				new Exec_Exp_Records(query.table2.toString()),
				query.type);
		
		if (query.on != null) {
			newExp = new Exec_Exp_Selection(translateConditionList(query.on.conditions), newExp);
		}
		
		ArrayList<Exec_Item> itemList = null;
		ArrayList<String> newNameList = null;
		Object[] projectionObjects = new Object[3];
		translateFieldList(query.field_list, projectionObjects);
		itemList = (ArrayList<Exec_Item>) projectionObjects[0];
		newNameList = (ArrayList<String>) projectionObjects[2];
		
		newExp = new Exec_Exp_Projection_NoAgg(itemList, null, newNameList, newExp);
		
		if (query.isDistinct) {
			newExp = new Exec_Exp_Distinct(newExp);
		}
		
		return newExp;
		
	}
	
/* ******************************************************************************************************
 * order by part	
 */

	public void translateOrderPart(OrderPart orderpart, Object[] ob) {
		OrderByList orderByList = orderpart.order_list;
		
		ArrayList<Exec_Item> itemList = new ArrayList<Exec_Item>();
		ArrayList<Boolean> isDownList = new ArrayList<Boolean>();
		
		while (orderByList != null) {
			itemList.add(translateItem(orderByList.head.exp));
			isDownList.add(!orderByList.head.isDesc);
			orderByList = orderByList.tail;
		}
		
		ob[0] = itemList;
		ob[1] = isDownList;
	}
	
/* ******************************************************************************************************
 * Having part
 */
	
	public Exec_Condition translateHavingPart(HavingPart havePart) {
		return translateConditionList(havePart.conditions);
	}

/* *******************************************************************************************************
 * Condition	
 */
	
	public Exec_Condition translateWherePart(WherePart where) {
		return translateConditionList(where.conditions);
	}
	
	public Exec_Condition translateConditionList(Conditions conditionList) {
		Conditions tempConditions = conditionList;
		
		if (tempConditions.op == 0) {
			// Single Condition
			return translateCondition(tempConditions.condition);
		}
		else if (tempConditions.op == 1) {
			// AND
			return new Exec_Condition_AND(
					translateConditionList(tempConditions.left),
					translateConditionList(tempConditions.right)
					);
		}
		else if (tempConditions.op == 2) {
			// OR
			return new Exec_Condition_OR(
					translateConditionList(tempConditions.left),
					translateConditionList(tempConditions.right)
					);
		}
		else if (tempConditions.op == 3) {
			// NOT
			return new Exec_Condition_NOT(
					translateConditionList(tempConditions.left)
					);
		}
		else {
			// error
			System.out.println("No such condition!");
			return null;
		}
	}

	
	public Exec_Condition translateCondition(Condition condition) {
		if (condition.type == 1) {
			// exp compare_op exp
			return new Exec_Condition_Compare(
					translateItem(condition.exp1),
					translateItem(condition.exp2),
					Tools.Convert_CompareType(condition.compareop), 
					condition.type);
		}
		else if (condition.type == 2) {
			// 2: exp compare_op LPAREN query RPAREN
			Exec_Item_Exp expItem = new Exec_Item_Exp(translateQuery(condition.query1));
			
			return new Exec_Condition_Compare(
					translateItem(condition.exp1),
					expItem,
					Tools.Convert_CompareType(condition.compareop),
					condition.type);
		}
		else if (condition.type == 3) {
			// 3: LPAREN query RPAREN compare_op exp
			Exec_Item_Exp expItem = new Exec_Item_Exp(translateQuery(condition.query1));
			
			return new Exec_Condition_Compare(
					expItem,
					translateItem(condition.exp1),
					Tools.Convert_CompareType(condition.compareop),
					condition.type);
		}
		else if (condition.type == 4) {
			// LPAREN query RPAREN compare_op LPAREN query RPAREN
			Exec_Item_Exp expItem1 = new Exec_Item_Exp(translateQuery(condition.query1));
			Exec_Item_Exp expItem2 = new Exec_Item_Exp(translateQuery(condition.query2));
			
			return new Exec_Condition_Compare(
					expItem1,
					expItem2,
					Tools.Convert_CompareType(condition.compareop),
					condition.type);
		}
		else if (condition.type == 5) {
			// exp compare_op ANY LPAREN query RPAREN
			return new Exec_Condition_ANY(
					translateItem(condition.exp1),
					translateQuery(condition.query1),
					Tools.Convert_CompareType(condition.compareop));
		}
		else if (condition.type == 6) {
			// exp compare_op ALL LPAREN query RPAREN
			return new Exec_Condition_ALL(
					translateItem(condition.exp1),
					translateQuery(condition.query1),
					Tools.Convert_CompareType(condition.compareop));
		}
		else if (condition.type == 7) {
			// exp IN LPAREN query RPAREN
			Exec_ItemList itemList = null, tailList = null;
			
			ExpList tempExp = condition.exp_list;
			while (tempExp != null) {
				if (itemList == null) {
					itemList = tailList = new Exec_ItemList(translateItem(tempExp.head), null);
				}
				else {
					tailList = tailList.tail = new Exec_ItemList(translateItem(tempExp.head), null);
				}
				tempExp = tempExp.tail;
			}
			
			return new Exec_Condition_IN(
					itemList,
					translateQuery(condition.query1));
		}
		else if (condition.type == 8) {
			// exp NOT IN LPAREN query RPAREN
			Exec_ItemList itemList = null, tailList = null;
			
			ExpList tempExp = condition.exp_list;
			while (tempExp != null) {
				if (itemList == null) {
					itemList = tailList = new Exec_ItemList(translateItem(tempExp.head), null);
				}
				else {
					tailList = tailList.tail = new Exec_ItemList(translateItem(tempExp.head), null);
				}
				tempExp = tempExp.tail;
			}
			Exec_Condition iN_Condition = new Exec_Condition_IN(
					itemList,
					translateQuery(condition.query1));
			
			return new Exec_Condition_NOT(iN_Condition);
		}
		else {
			// error
			return null;
		}
	}

/* ********************************************************************************************************
 * Table & TableList	
 */
	
	public Exec_Exp translateTableList(TableList tableList) {
		ArrayList<Exec_Exp_Records> recordsList = new ArrayList<Exec_Exp_Records>();
		ArrayList<String> newNameList = new ArrayList<String>();
		
		TableList tempTableList = tableList;
		
		while (tempTableList != null) {
			Table table = tempTableList.head;
			Exec_Exp_Records newExecExpRecords = new Exec_Exp_Records(table.name.toString());
			recordsList.add(newExecExpRecords);
			
			if (table.refname == null) newNameList.add("");
			else newNameList.add(table.refname.toString());
			
			tempTableList = tempTableList.tail;
		}
		
		Exec_Exp_From newExecExpFrom = new Exec_Exp_From(recordsList, newNameList);
		
		return newExecExpFrom;
	}

/* **************************************************************************************************
 * Items
 */
	
	public Exec_Item translateItem(Exp exp) {
		if (exp instanceof ExpId) {
			return translateItem_ExpId((ExpId) exp);
		}
		else if (exp instanceof ExpIdDotId) {
			return translateItem_ExpIdDotId((ExpIdDotId) exp);
		}
		else if (exp instanceof ExpOp) {
			return translateItem_Op((ExpOp) exp);
		}
		else if (exp instanceof Const) {
			return translateItem_Const((Const)exp);
		}
		else if (exp instanceof ExpStar) {
			return translateItem_ExpStar((ExpStar) exp);
		}
		else if (exp instanceof Function) {
			return translateItem_ExpFunction((Function) exp);
		}
		return null;
	}
	
	public Exec_Item translateItem_ExpId(ExpId exp) {
		return new Exec_Item_Field(exp.name.toString());
	}
	
	public Exec_Item translateItem_ExpIdDotId(ExpIdDotId exp) {
		return new Exec_Item_FieldDotField(exp.name1.toString(), exp.name2.toString());
	}
	
	public Exec_Item translateItem_ExpStar(ExpStar exp) {
		return new Exec_Item_Field_Star();
	}
	
	public Exec_Item translateItem_Op(ExpOp exp) {
		return new Exec_Item_Op(
				translateItem(exp.left),
				translateItem(exp.right),
				Exec_Item_OpType.values()[exp.type - 1]);
	}
	
	public Exec_Item translateItem_Const(Const exp) {
		Exec_Item_ConstType type = null;
		
		if (exp instanceof ConstBool) type = Exec_Item_ConstType.Boolean;
		else if (exp instanceof ConstDouble) type = Exec_Item_ConstType.Double;
		else if (exp instanceof ConstInt) type = Exec_Item_ConstType.Int;
		else if (exp instanceof ConstString) type = Exec_Item_ConstType.String;
		else {
			// error
			System.out.println("No such const type!");
			return null;
		}
		
		return new Exec_Item_Const(exp.toString(), type);
	}
	
	public Exec_Item translateItem_ExpFunction(Function exp) {
		String aggOpString = null;
		
		switch(exp.type) {
		case 76:
			aggOpString = "COUNT";
			break;
		case 77:
			aggOpString = "SUM";
			break;
		case 78:
			aggOpString = "AVG";
			break;
		case 79:
			aggOpString = "MIN";
			break;
		case 80:
			aggOpString = "MAX";
			break;
		default:
		}
		
		Exp newExp = exp.exp;
		
		if (newExp instanceof ExpId) {
			return new Exec_Item_Field_Agg(((ExpId) newExp).name.toString(), aggOpString); 
		}
		else if (newExp instanceof ExpIdDotId) {
			return new Exec_Item_FieldDotField_Agg(((ExpIdDotId) newExp).name1.toString(), 
					((ExpIdDotId) newExp).name2.toString(), aggOpString); 
		}
		else {
			// error
			System.out.println("Having field wrong!");
			return null;
		}
	}
	
/* *****************************************************************************************************************
 * Group
 */
	
	public Exec_ItemList translateGroupPart(GroupPart group) {
		Exec_ItemList itemList = null, tail = null;
		IdList idList = group.id_list;
		while (idList != null) {
			if (itemList == null) {
				itemList = tail = new Exec_ItemList(translateItem(idList.head), null);
			}
			else {
				tail = tail.tail = new Exec_ItemList(translateItem(idList.head), null);
			}
			idList = idList.tail;
		}
		
		return itemList;
	}
	
	
/* ******************************************************************************************************************
 * FieldList
 * For Projection, Remane, Aggregate
 */
	
	public boolean translateFieldList(FieldList fl, Object[] ob) {	
		/*
		 * return whether it should do aggregate
		 */
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
				projectionItemList.add(translateItem(functionExp.exp));
				switch(functionExp.type) {
				case 76:
					aggOpList.add("COUNT");
					break;
				case 77:
					aggOpList.add("SUM");
					break;
				case 78:
					aggOpList.add("AVG");
					break;
				case 79:
					aggOpList.add("MIN");
					break;
				case 80:
					aggOpList.add("MAX");
					break;
				default:
				}
			}
			else {
				projectionItemList.add(translateItem(field.exp));
				aggOpList.add("");
			}
			
			
			if (field.refname != null) newNameList.add(field.refname.toString());
			else newNameList.add(field.toString());
			
			fieldList = fieldList.tail;
		}
		
		ob[0] = projectionItemList;
		ob[1] = aggOpList;
		ob[2] = newNameList;
		
		return isAggregate;
	}
	
	
}
