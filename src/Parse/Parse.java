/*
 * @(#)Parse.java	1.0 2011/01/09
 * Package: Parse
 * Copyright 2011 Kiki Tiger Compiler, Inc. All rights reserved.
 * @author ÕÅÐù  5080309672 SJTU
 */

package Parse;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parse the input file and get the output AST
 * 
 * @author Kiki
 */
public class Parse {
	
	/**
	 * Error massage
	 */
	private ErrorMsg.ErrorMsg errorMsg;
	
	/**
	 * Result AST
	 */
	private Absyn.StmList absyn;
	
	/**
	 * Constructor
	 * 
	 * @param filename
	 * @param errorMsg
	 */
	public Parse(InputStream inp, ErrorMsg.ErrorMsg err){
		this.errorMsg = err;
		Grm parser = new Grm(new Yylex(inp, errorMsg), errorMsg);
		/* open input files, etc. here */
		try {
			parser.parse();
		} 
		catch ( Exception ex ) {
			ex.printStackTrace();
			try {
				inp.close();
			} catch (IOException e) {}
//			System.exit(-1);
		}
		finally {
			try {inp.close();} catch (java.io.IOException e) {}
		}
//		if ( errorMsg.anyErrors )
//			System.exit(-1);
		absyn=parser.parseResult;
	}
	
	/**
	 * Get the result
	 * @return AST
	 */
	public Absyn.StmList GetExp() {
		return absyn;
	}
}