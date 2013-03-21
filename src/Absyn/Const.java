/**
 * 
 */
package Absyn;

/**
 * @author Kiki
 *
 */
public class Const extends Exp {

	/**
	 * Const type according to sym[] in Parse.sym.java
	 */
	public int type;
	
	/**
	 * 
	 */
	public Const(int p, int t) {
		pos = p;
		type = t;
	}

}
