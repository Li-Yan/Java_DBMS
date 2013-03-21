/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class ValuesPart extends Absyn {

	public ValueList head;
	
	public ValuesPart tail;
	/**
	 * 
	 */
	public ValuesPart(int p, ValueList h, ValuesPart t) {
		pos = p;
		head = h;
		tail = t;
	}

}
