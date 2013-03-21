/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class OrderBy extends Absyn {

	public Exp exp;
	
	/**
	 * Whether or not is Descend
	 */
	public Boolean isDesc;
	
	/**
	 * 
	 */
	public OrderBy(int p, Exp e, Boolean i) {
		pos = p;
		exp = e;
		isDesc = i;
	}

	public String toString() {
		if(isDesc)
			return exp.toString() + " DESC ";
		else
			return exp.toString();
	}
}
