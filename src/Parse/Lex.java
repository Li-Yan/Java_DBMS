/*
 * @(#)Lex.java	1.0 2011/01/09
 * Package: Parse
 * Copyright 2011 Kiki Tiger Compiler, Inc. All rights reserved.
 * @author ÕÅÐù  5080309672 SJTU
 */

package Parse;

import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * This class print the abstract syntax tree to file
 * 
 * @author Kiki
 */
public class Lex {
	
	/**
	 * Print
	 * @param filename input file name
	 * @param filenameLex output file name
	 * @throws Exception
	 */
	public static void lex(String filename, String filenameLex) throws Exception{
		ErrorMsg.ErrorMsg errorMsg = new ErrorMsg.ErrorMsg(filename);
		java.io.InputStream inp = new java.io.FileInputStream(filename);
		PrintStream out = new PrintStream(new FileOutputStream(filenameLex), true);
		Lexer lexer = new Yylex(inp, errorMsg);
		java_cup.runtime.Symbol tok;
		do {
			tok = lexer.nextToken();
			String sym = symnames[tok.sym];
			if ( sym.length() < 8 )
				sym += "\t\t";
			else
				sym += "\t";
			out.print(sym);
			out.print("( " + errorMsg.getLineNum()+ ", " + tok.left + ", " + tok.right + " )\t");
			if ( tok.value != null ) 
				out.print( tok.value.toString() );
			out.println();
		} while (tok.sym != sym.EOF);
		inp.close();
		out.close();
	}

	public static String symnames[] = new String[100];
	static {
		symnames[sym.EOF] = "EOF";
		symnames[sym.error] = "error";
		symnames[sym.ID] = "ID";
		symnames[sym.STRING] = "STRING";
		symnames[sym.DATEVAL] = "DATEVAL";
		symnames[sym.TIMEVAL] = "TIMEVAL";
		symnames[sym.TIMESTAMPVAL] = "TIMESTAMPVAL";
		symnames[sym.INTVAL] = "INTVAL";
		symnames[sym.DOUBLEVAL] = "DOUBLEVAL";
		symnames[sym.DOT] = "DOT";
		symnames[sym.COMMA] = "COMMA";
		symnames[sym.SEMICOLON] = "SEMICOLON";
		symnames[sym.LPAREN] = "LPAREN";
		symnames[sym.RPAREN] = "RPAREN";
		symnames[sym.STAR] = "STAR";
		symnames[sym.EQ] = "EQ";
		symnames[sym.NEQ] = "NEQ";
		symnames[sym.LT] = "LT";
		symnames[sym.LE] = "LE";
		symnames[sym.GT] = "GT";
		symnames[sym.GE] = "GE";
		symnames[sym.PLUS] = "PLUS";
		symnames[sym.MINUS] = "MINUS";
		symnames[sym.DIVIDE] = "DIVIDE";
		symnames[sym.CREATE] = "CREATE";
		symnames[sym.DROP] = "DROP";
		symnames[sym.ALTER] = "ALTER";
		symnames[sym.ADD] = "ADD";
		symnames[sym.DATABASE] = "DATABASE";
		symnames[sym.USE] = "USE";
		symnames[sym.SHOW] = "SHOW";
		symnames[sym.TABLE] = "TABLE";
		symnames[sym.VIEW] = "VIEW";
		symnames[sym.INDEX] = "INDEX";
		symnames[sym.SELECT] = "SELECT";
		symnames[sym.INSERT] = "INSERT";
		symnames[sym.UPDATE] = "UPDATE";
		symnames[sym.SET] = "SET";
		symnames[sym.DELETE] = "DELETE";
		symnames[sym.PRIMARY] = "PRIMARY";
		symnames[sym.KEY] = "KEY";
		symnames[sym.UNIQUE] = "UNIQUE";
		symnames[sym.DEFAULT] = "DEFAULT";
		symnames[sym.FROM] = "FROM";
		symnames[sym.WHERE] = "WHERE";
		symnames[sym.GROUP] = "GROUP";
		symnames[sym.ORDER] = "ORDER";
		symnames[sym.DESC] = "DESC";
		symnames[sym.UNION] = "UNION";
		symnames[sym.INTERSECT] = "INTERSECT";
		symnames[sym.EXCEPT] = "EXCEPT";
		symnames[sym.JOIN] = "JOIN";
		symnames[sym.CROSS] = "CROSS";
		symnames[sym.NATURAL] = "NATURAL";
		symnames[sym.FULL] = "FULL";
		symnames[sym.LEFT] = "LEFT";
		symnames[sym.RIGHT] = "RIGHT";
		symnames[sym.HAVING] = "HAVING";
		symnames[sym.BETWEEN] = "BETWEEN";
		symnames[sym.AND] = "AND";
		symnames[sym.OR] = "OR";
		symnames[sym.NOT] = "NOT";
		symnames[sym.VALUES] = "VALUES";
		symnames[sym.DISTINCT] = "DISTINCT";
		symnames[sym.ANY] = "ANY";
		symnames[sym.ALL] = "ALL";
		symnames[sym.NULL] = "NULL";
		symnames[sym.TRUE] = "TRUE";
		symnames[sym.FALSE] = "FALSE";
		symnames[sym.UNKNOWN] = "UNKNOWN";
		symnames[sym.ON] = "ON";
		symnames[sym.IN] = "IN";
		symnames[sym.AS] = "AS";
		symnames[sym.IS] = "IS";
		symnames[sym.BY] = "BY";
		symnames[sym.INTO] = "INTO";
		symnames[sym.COUNT] = "COUNT";
		symnames[sym.SUM] = "SUM";
		symnames[sym.AVG] = "AVG";
		symnames[sym.MIN] = "MIN";
		symnames[sym.MAX] = "MAX";
		symnames[sym.BOOLEAN] = "BOOLEAN";
		symnames[sym.INT] = "INT";
		symnames[sym.CHAR] = "CHAR";
		symnames[sym.VARCHAR] = "VARCHAR";
		symnames[sym.DECIMAL] = "DECIMAL";
		symnames[sym.DOUBLE] = "DOUBLE";		
		symnames[sym.DATE] = "DATE";
		symnames[sym.TIME] = "TIME";
		symnames[sym.TIMESTAMP] = "TIMESTAMP";
	}
}