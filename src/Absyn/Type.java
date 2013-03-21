package Absyn;

public class Type extends Absyn {

	/**
	 * Attribute Type : Parse.sym[]
	 */
	public int type;
	
	/**
	 * 
	 * @param p
	 * @param t
	 */
	public Type(int p, int t) {
		pos = p;
		type = t;
	}
	
	public String toString() {
		return Parse.Lex.symnames[type];
	}

}
