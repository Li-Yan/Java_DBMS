/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class HavingPart extends Absyn {

	public Conditions conditions;
	/**
	 * 
	 */
	public HavingPart(int p, Conditions c) {
		pos = p;
		conditions = c;
	}

	public String toString(){
		return " HAVING " + conditions.toString();
	}
}
