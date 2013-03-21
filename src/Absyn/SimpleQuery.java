/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class SimpleQuery extends Query {

	public Boolean isDistinct;
	public FieldList field_list;
	public TableList table_list;
	public WherePart where;
	public GroupPart group;
	public HavingPart having;
	public OrderPart order;
	
	/**
	 *	
	 */
	public SimpleQuery(int p, Boolean isDis, FieldList fl, TableList tl,
			WherePart w, GroupPart g, HavingPart h, OrderPart o) {
		super(p);
		isDistinct = isDis;
		field_list = fl;
		table_list = tl;
		where = w;
		group = g;
		having = h;
		order = o;
	}

	public String toString() {
		String ret = "select ";
		if (isDistinct)
			ret += "DISTINCT ";
		ret += field_list.toString();
		ret += " FROM " + table_list.toString();
		if (where != null)
			ret += " " + where.toString();
		if (group != null)
			ret += " " + group.toString();
		if (having != null)
			ret += " " + having.toString();
		if (order != null)
			ret += " " + order.toString();
		return ret;
	}
}
