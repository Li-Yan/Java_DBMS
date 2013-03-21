/*
 * @(#)Lexer.java	1.0 2011/01/09
 * Package: Parse
 * Copyright 2011 Kiki Tiger Compiler, Inc. All rights reserved.
 * @author ÕÅÐù  5080309672 SJTU
 */

package Parse;

/**
 * Interface to get the next token
 * 
 * @author Kiki
 */
interface Lexer {
	
	/**
	 * Get the next token
	 * @return Symbol
	 * @throws java.io.IOException
	 */
    public java_cup.runtime.Symbol nextToken() throws java.io.IOException;
}
