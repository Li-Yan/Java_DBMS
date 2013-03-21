/*
 * @(#)ExpList.java	1.0 2011/01/09
 * Package: Absyn
 * Copyright 2011 Kiki Tiger Compiler, Inc. All rights reserved.
 * @author ÕÅÐù  5080309672 SJTU
 */

package Absyn;

/**
 * Class representing an expression list
 * 
 * @author Kiki
 */
public class StmList {
	
	/**
	 * The expression
	 */
	public Stm head;
	
	/**
	 * The next expression
	 */
	public StmList tail;
	
	/**
	 * Constructor
	 * 
	 * @param h The expression
	 * @param t The next expression
	 */
	public StmList(Stm h, StmList t) {
		head=h;
		tail=t;
	}
}
