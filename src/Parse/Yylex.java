package Parse;
import ErrorMsg.ErrorMsg;


class Yylex implements Lexer, java_cup.runtime.Scanner {
	private final int YY_BUFFER_SIZE = 512;
	private final int YY_F = -1;
	private final int YY_NO_STATE = -1;
	private final int YY_NOT_ACCEPT = 0;
	private final int YY_START = 1;
	private final int YY_END = 2;
	private final int YY_NO_ANCHOR = 4;
	private final int YY_BOL = 128;
	private final int YY_EOF = 129;

	private ErrorMsg errorMsg;
	String string;
	private void newline() {
		errorMsg.newline(yychar+1);
	}
	private void err(int pos, String s) {
  		errorMsg.error(pos, s);
	}
	private void err(String s) {
 		err(yychar, s);
	}
	private java_cup.runtime.Symbol tok(int kind, Object value) {
    	return new java_cup.runtime.Symbol(kind, yychar, yychar+yylength(), value);
	}
	private java_cup.runtime.Symbol tok(int kind) {
    	return new java_cup.runtime.Symbol(kind, yychar, yychar+yylength(), null);
	}
	@Override
	public java_cup.runtime.Symbol next_token() throws Exception{
		return nextToken();
	}
	Yylex(java.io.InputStream s, ErrorMsg e) {
		this(s);
		errorMsg=e;
	}
	private java.io.BufferedReader yy_reader;
	private int yy_buffer_index;
	private int yy_buffer_read;
	private int yy_buffer_start;
	private int yy_buffer_end;
	private char yy_buffer[];
	private int yychar;
	private boolean yy_at_bol;
	private int yy_lexical_state;

	Yylex (java.io.Reader reader) {
		this ();
		if (null == reader) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(reader);
	}

	Yylex (java.io.InputStream instream) {
		this ();
		if (null == instream) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));
	}

	private Yylex () {
		yy_buffer = new char[YY_BUFFER_SIZE];
		yy_buffer_read = 0;
		yy_buffer_index = 0;
		yy_buffer_start = 0;
		yy_buffer_end = 0;
		yychar = 0;
		yy_at_bol = true;
		yy_lexical_state = YYINITIAL;
	}

	private boolean yy_eof_done = false;
	private final int STRING = 2;
	private final int YYINITIAL = 0;
	private final int COMMENT = 1;
	private final int yy_state_dtrans[] = {
		0,
		30,
		32
	};
	private void yybegin (int state) {
		yy_lexical_state = state;
	}
	private int yy_advance ()
		throws java.io.IOException {
		int next_read;
		int i;
		int j;

		if (yy_buffer_index < yy_buffer_read) {
			return yy_buffer[yy_buffer_index++];
		}

		if (0 != yy_buffer_start) {
			i = yy_buffer_start;
			j = 0;
			while (i < yy_buffer_read) {
				yy_buffer[j] = yy_buffer[i];
				++i;
				++j;
			}
			yy_buffer_end = yy_buffer_end - yy_buffer_start;
			yy_buffer_start = 0;
			yy_buffer_read = j;
			yy_buffer_index = j;
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}

		while (yy_buffer_index >= yy_buffer_read) {
			if (yy_buffer_index >= yy_buffer.length) {
				yy_buffer = yy_double(yy_buffer);
			}
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}
		return yy_buffer[yy_buffer_index++];
	}
	private void yy_move_end () {
		if (yy_buffer_end > yy_buffer_start &&
		    '\n' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
		if (yy_buffer_end > yy_buffer_start &&
		    '\r' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
	}
	private boolean yy_last_was_cr=false;
	private void yy_mark_start () {
		yychar = yychar
			+ yy_buffer_index - yy_buffer_start;
		yy_buffer_start = yy_buffer_index;
	}
	private void yy_mark_end () {
		yy_buffer_end = yy_buffer_index;
	}
	private void yy_to_mark () {
		yy_buffer_index = yy_buffer_end;
		yy_at_bol = (yy_buffer_end > yy_buffer_start) &&
		            ('\r' == yy_buffer[yy_buffer_end-1] ||
		             '\n' == yy_buffer[yy_buffer_end-1] ||
		             2028/*LS*/ == yy_buffer[yy_buffer_end-1] ||
		             2029/*PS*/ == yy_buffer[yy_buffer_end-1]);
	}
	private java.lang.String yytext () {
		return (new java.lang.String(yy_buffer,
			yy_buffer_start,
			yy_buffer_end - yy_buffer_start));
	}
	private int yylength () {
		return yy_buffer_end - yy_buffer_start;
	}
	private char[] yy_double (char buf[]) {
		int i;
		char newbuf[];
		newbuf = new char[2*buf.length];
		for (i = 0; i < buf.length; ++i) {
			newbuf[i] = buf[i];
		}
		return newbuf;
	}
	private final int YY_E_INTERNAL = 0;
	private final int YY_E_MATCH = 1;
	private java.lang.String yy_error_string[] = {
		"Error: Internal error.\n",
		"Error: Unmatched input.\n"
	};
	private void yy_error (int code,boolean fatal) {
		java.lang.System.out.print(yy_error_string[code]);
		java.lang.System.out.flush();
		if (fatal) {
			throw new Error("Fatal Error.\n");
		}
	}
	private int[][] unpackFromString(int size1, int size2, String st) {
		int colonIndex = -1;
		String lengthString;
		int sequenceLength = 0;
		int sequenceInteger = 0;

		int commaIndex;
		String workString;

		int res[][] = new int[size1][size2];
		for (int i= 0; i < size1; i++) {
			for (int j= 0; j < size2; j++) {
				if (sequenceLength != 0) {
					res[i][j] = sequenceInteger;
					sequenceLength--;
					continue;
				}
				commaIndex = st.indexOf(',');
				workString = (commaIndex==-1) ? st :
					st.substring(0, commaIndex);
				st = st.substring(commaIndex+1);
				colonIndex = workString.indexOf(':');
				if (colonIndex == -1) {
					res[i][j]=Integer.parseInt(workString);
					continue;
				}
				lengthString =
					workString.substring(colonIndex+1);
				sequenceLength=Integer.parseInt(lengthString);
				workString=workString.substring(0,colonIndex);
				sequenceInteger=Integer.parseInt(workString);
				res[i][j] = sequenceInteger;
				sequenceLength--;
			}
		}
		return res;
	}
	private int yy_acpt[] = {
		/* 0 */ YY_NOT_ACCEPT,
		/* 1 */ YY_NO_ANCHOR,
		/* 2 */ YY_NO_ANCHOR,
		/* 3 */ YY_NO_ANCHOR,
		/* 4 */ YY_NO_ANCHOR,
		/* 5 */ YY_NO_ANCHOR,
		/* 6 */ YY_NO_ANCHOR,
		/* 7 */ YY_NO_ANCHOR,
		/* 8 */ YY_NO_ANCHOR,
		/* 9 */ YY_NO_ANCHOR,
		/* 10 */ YY_NO_ANCHOR,
		/* 11 */ YY_NO_ANCHOR,
		/* 12 */ YY_NO_ANCHOR,
		/* 13 */ YY_NO_ANCHOR,
		/* 14 */ YY_NO_ANCHOR,
		/* 15 */ YY_NO_ANCHOR,
		/* 16 */ YY_NO_ANCHOR,
		/* 17 */ YY_NO_ANCHOR,
		/* 18 */ YY_NO_ANCHOR,
		/* 19 */ YY_NO_ANCHOR,
		/* 20 */ YY_NO_ANCHOR,
		/* 21 */ YY_NO_ANCHOR,
		/* 22 */ YY_NO_ANCHOR,
		/* 23 */ YY_NO_ANCHOR,
		/* 24 */ YY_NO_ANCHOR,
		/* 25 */ YY_NO_ANCHOR,
		/* 26 */ YY_NO_ANCHOR,
		/* 27 */ YY_NO_ANCHOR,
		/* 28 */ YY_NO_ANCHOR,
		/* 29 */ YY_NO_ANCHOR,
		/* 30 */ YY_NO_ANCHOR,
		/* 31 */ YY_NO_ANCHOR,
		/* 32 */ YY_NO_ANCHOR,
		/* 33 */ YY_NO_ANCHOR,
		/* 34 */ YY_NO_ANCHOR,
		/* 35 */ YY_NOT_ACCEPT,
		/* 36 */ YY_NO_ANCHOR,
		/* 37 */ YY_NO_ANCHOR,
		/* 38 */ YY_NO_ANCHOR,
		/* 39 */ YY_NOT_ACCEPT,
		/* 40 */ YY_NO_ANCHOR,
		/* 41 */ YY_NOT_ACCEPT,
		/* 42 */ YY_NOT_ACCEPT,
		/* 43 */ YY_NOT_ACCEPT,
		/* 44 */ YY_NOT_ACCEPT,
		/* 45 */ YY_NOT_ACCEPT,
		/* 46 */ YY_NOT_ACCEPT,
		/* 47 */ YY_NOT_ACCEPT,
		/* 48 */ YY_NOT_ACCEPT,
		/* 49 */ YY_NOT_ACCEPT,
		/* 50 */ YY_NOT_ACCEPT,
		/* 51 */ YY_NOT_ACCEPT,
		/* 52 */ YY_NOT_ACCEPT,
		/* 53 */ YY_NOT_ACCEPT,
		/* 54 */ YY_NOT_ACCEPT,
		/* 55 */ YY_NOT_ACCEPT,
		/* 56 */ YY_NOT_ACCEPT,
		/* 57 */ YY_NOT_ACCEPT,
		/* 58 */ YY_NOT_ACCEPT,
		/* 59 */ YY_NOT_ACCEPT,
		/* 60 */ YY_NOT_ACCEPT,
		/* 61 */ YY_NOT_ACCEPT,
		/* 62 */ YY_NOT_ACCEPT,
		/* 63 */ YY_NOT_ACCEPT,
		/* 64 */ YY_NOT_ACCEPT,
		/* 65 */ YY_NOT_ACCEPT,
		/* 66 */ YY_NOT_ACCEPT,
		/* 67 */ YY_NOT_ACCEPT,
		/* 68 */ YY_NOT_ACCEPT,
		/* 69 */ YY_NOT_ACCEPT,
		/* 70 */ YY_NOT_ACCEPT,
		/* 71 */ YY_NOT_ACCEPT,
		/* 72 */ YY_NOT_ACCEPT,
		/* 73 */ YY_NOT_ACCEPT,
		/* 74 */ YY_NOT_ACCEPT,
		/* 75 */ YY_NOT_ACCEPT,
		/* 76 */ YY_NOT_ACCEPT,
		/* 77 */ YY_NOT_ACCEPT,
		/* 78 */ YY_NOT_ACCEPT
	};
	private int yy_cmap[] = unpackFromString(1,130,
"30:8,17:2,16,30,17,15,30:18,28,29:2,20,29:2,10,21,4,5,6,12,2,13,1,14,22,23," +
"24,25,27:2,19:4,26,3,8,7,9,29:2,18:26,29:4,20,29,18:26,29,11,29:2,30,0:2")[0];

	private int yy_rmap[] = unpackFromString(1,79,
"0,1,2,1:6,3,4,5,1,6,7,8,9,10,11,2,1:8,12,1,13,1,14,1:2,15,16,17,18,2,1,19,2" +
"0,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,4" +
"5,46,47,48,49,50,51,52,53,54,55,56")[0];

	private int yy_nxt[][] = unpackFromString(57,31,
"1,2,3,4,5,6,7,8,9,10,11,36,12,13,14,35,15:2,16,17,40,18,17:4,40,17,15,40:2," +
"-1:50,19,-1:2,19:4,-1,19,-1:10,20,-1,21,-1:28,22,-1:33,23,-1:21,39,-1:17,17" +
",-1:2,17:4,-1,17,-1:17,25,-1:32,15:2,-1:10,15,-1:20,16:3,-1,16:4,-1,16,-1:4" +
",19,-1:17,17,-1:2,17:4,-1,17,-1:22,41,-1:2,62:2,63,41,-1,41,-1:31,56,-1:2,1" +
",37:14,61,-1:2,37:3,-1,37:8,-1,1,38:14,-1:2,33,38:3,34,38:8,33,-1:16,26,-1:" +
"25,24,-1:20,37:14,-1:3,37:3,-1,37:8,-1:2,38:14,-1:3,38:3,-1,38:8,-1:20,71,-" +
"1:2,71:4,-1,71,-1:22,64,-1:2,64:4,43,64,-1:25,65:4,-1,65,-1:16,45,-1:39,47," +
"48,-1:33,72,-1:23,49,-1:3,49:3,-1,49,-1:25,49:3,-1:19,50,-1:39,52,67:2,53,-" +
"1:6,68,-1:19,27,-1:28,54,-1:3,54:3,-1,54,-1:25,54:2,-1:28,28,-1:28,55,-1,27" +
",55:4,-1,55,-1:24,57,-1:31,74:2,58,-1:28,73:4,-1:6,70,-1:19,29,-1:28,60,-1," +
"29,60:4,-1,60,-1:19,31,-1:33,42,-1:2,42:4,-1,42,-1:22,71,-1:2,42:4,-1,71,-1" +
":22,44,-1:2,44:4,-1,44,-1:22,46,-1:2,46:4,-1,46,-1:22,51,-1:2,51:4,-1,51,-1" +
":22,54,-1:2,54:4,-1,54,-1:22,55,-1:2,55:4,-1,55,-1:22,59,-1:2,59:4,-1,59,-1" +
":22,60,-1:2,60:4,-1,60,-1:22,64,-1:2,64:4,-1,64,-1:25,66:4,-1,66,-1:29,78,-" +
"1:23,73,-1:2,73:4,-1,73,-1:25,69:4,-1,69,-1:29,75,-1:23,76,-1:2,76:4,-1,76," +
"-1:25,77:4,-1,77,-1:3");

	public java_cup.runtime.Symbol nextToken ()
		throws java.io.IOException {
		int yy_lookahead;
		int yy_anchor = YY_NO_ANCHOR;
		int yy_state = yy_state_dtrans[yy_lexical_state];
		int yy_next_state = YY_NO_STATE;
		int yy_last_accept_state = YY_NO_STATE;
		boolean yy_initial = true;
		int yy_this_accept;

		yy_mark_start();
		yy_this_accept = yy_acpt[yy_state];
		if (YY_NOT_ACCEPT != yy_this_accept) {
			yy_last_accept_state = yy_state;
			yy_mark_end();
		}
		while (true) {
			if (yy_initial && yy_at_bol) yy_lookahead = YY_BOL;
			else yy_lookahead = yy_advance();
			yy_next_state = YY_F;
			yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
			if (YY_EOF == yy_lookahead && true == yy_initial) {

{	
	if (yy_state == STRING || yy_state == COMMENT)
		err("Unexpected EOF!");
	return tok(sym.EOF, null);
}
			}
			if (YY_F != yy_next_state) {
				yy_state = yy_next_state;
				yy_initial = false;
				yy_this_accept = yy_acpt[yy_state];
				if (YY_NOT_ACCEPT != yy_this_accept) {
					yy_last_accept_state = yy_state;
					yy_mark_end();
				}
			}
			else {
				if (YY_NO_STATE == yy_last_accept_state) {
					throw (new Error("Lexical Error: Unmatched Input."));
				}
				else {
					yy_anchor = yy_acpt[yy_last_accept_state];
					if (0 != (YY_END & yy_anchor)) {
						yy_move_end();
					}
					yy_to_mark();
					switch (yy_last_accept_state) {
					case 1:
						
					case -2:
						break;
					case 2:
						{ return tok(sym.DOT); }
					case -3:
						break;
					case 3:
						{ return tok(sym.COMMA); }
					case -4:
						break;
					case 4:
						{ return tok(sym.SEMICOLON); }
					case -5:
						break;
					case 5:
						{ return tok(sym.LPAREN); }
					case -6:
						break;
					case 6:
						{ return tok(sym.RPAREN); }
					case -7:
						break;
					case 7:
						{ return tok(sym.STAR); }
					case -8:
						break;
					case 8:
						{ return tok(sym.EQ); }
					case -9:
						break;
					case 9:
						{ return tok(sym.LT); }
					case -10:
						break;
					case 10:
						{ return tok(sym.GT); }
					case -11:
						break;
					case 11:
						{ err("Illegal character: <" + yytext() + ">"); }
					case -12:
						break;
					case 12:
						{ return tok(sym.PLUS); }
					case -13:
						break;
					case 13:
						{ return tok(sym.MINUS); }
					case -14:
						break;
					case 14:
						{ return tok(sym.DIVIDE); }
					case -15:
						break;
					case 15:
						{ }
					case -16:
						break;
					case 16:
						{ 
		String str;
		str = yytext().toLowerCase();
		if(str.equals("create"))		return tok(sym.CREATE);
		if(str.equals("drop"))			return tok(sym.DROP);
		if(str.equals("alter"))			return tok(sym.ALTER);
		if(str.equals("add"))			return tok(sym.ADD);
		if(str.equals("database"))		return tok(sym.DATABASE);
		if(str.equals("use"))			return tok(sym.USE);
		if(str.equals("show"))			return tok(sym.SHOW);
		if(str.equals("table"))			return tok(sym.TABLE);
		if(str.equals("view"))			return tok(sym.VIEW);
		if(str.equals("index"))			return tok(sym.INDEX);
		if(str.equals("select"))		return tok(sym.SELECT);
		if(str.equals("insert"))		return tok(sym.INSERT);
		if(str.equals("update"))		return tok(sym.UPDATE);
		if(str.equals("set"))			return tok(sym.SET);
		if(str.equals("delete"))		return tok(sym.DELETE);
		if(str.equals("primary"))		return tok(sym.PRIMARY);
		if(str.equals("key"))			return tok(sym.KEY);
		if(str.equals("unique"))		return tok(sym.UNIQUE);
		if(str.equals("default"))		return tok(sym.DEFAULT);
		if(str.equals("from"))			return tok(sym.FROM);
		if(str.equals("where"))			return tok(sym.WHERE);
		if(str.equals("group"))			return tok(sym.GROUP);
		if(str.equals("order"))			return tok(sym.ORDER);
		if(str.equals("desc"))			return tok(sym.DESC);
		if(str.equals("union"))			return tok(sym.UNION);
		if(str.equals("intersect"))		return tok(sym.INTERSECT);
		if(str.equals("except"))		return tok(sym.EXCEPT);
		if(str.equals("join"))			return tok(sym.JOIN);
		if(str.equals("cross"))			return tok(sym.CROSS);
		if(str.equals("natural"))		return tok(sym.NATURAL);
		if(str.equals("full"))			return tok(sym.FULL);
		if(str.equals("left"))			return tok(sym.LEFT);
		if(str.equals("right"))			return tok(sym.RIGHT);
		if(str.equals("having"))		return tok(sym.HAVING);
		if(str.equals("between"))		return tok(sym.BETWEEN);
		if(str.equals("and"))			return tok(sym.AND);
		if(str.equals("or"))			return tok(sym.OR);
		if(str.equals("not"))			return tok(sym.NOT);
		if(str.equals("values"))		return tok(sym.VALUES);
		if(str.equals("distinct"))		return tok(sym.DISTINCT);
		if(str.equals("any"))			return tok(sym.ANY);
		if(str.equals("all"))			return tok(sym.ALL);
		if(str.equals("null"))			return tok(sym.NULL);
		if(str.equals("true"))			return tok(sym.TRUE);
		if(str.equals("false"))			return tok(sym.FALSE);
		if(str.equals("unknown"))		return tok(sym.UNKNOWN);
		if(str.equals("on"))			return tok(sym.ON);
		if(str.equals("in"))			return tok(sym.IN);
		if(str.equals("as"))			return tok(sym.AS);
		if(str.equals("is"))			return tok(sym.IS);
		if(str.equals("by"))			return tok(sym.BY);
		if(str.equals("into"))			return tok(sym.INTO);
		if(str.equals("count"))			return tok(sym.COUNT);
		if(str.equals("sum"))			return tok(sym.SUM);
		if(str.equals("avg"))			return tok(sym.AVG);
		if(str.equals("min"))			return tok(sym.MIN);
		if(str.equals("max"))			return tok(sym.MAX);
		if(str.equals("boolean"))		return tok(sym.BOOLEAN);
		if(str.equals("int") || str.equals("integer"))
										return tok(sym.INT);
		if(str.equals("char"))			return tok(sym.CHAR);
		if(str.equals("varchar"))		return tok(sym.VARCHAR);
		if(str.equals("decimal"))		return tok(sym.DECIMAL);
		if(str.equals("numeric"))		return tok(sym.DECIMAL);
		if(str.equals("float"))			return tok(sym.DOUBLE);
		if(str.equals("real"))			return tok(sym.DOUBLE);
		if(str.equals("double"))		return tok(sym.DOUBLE);
		if(str.equals("date"))			return tok(sym.DATE);
		if(str.equals("time"))			return tok(sym.TIME);
		if(str.equals("timestamp"))		return tok(sym.TIMESTAMP);
		return tok(sym.ID, yytext()); 
	}
					case -17:
						break;
					case 17:
						{ return tok(sym.INTVAL, new Integer(yytext())); }
					case -18:
						break;
					case 18:
						{ yybegin(STRING); string = ""; }
					case -19:
						break;
					case 19:
						{ return tok(sym.DOUBLEVAL, new Double(yytext())); }
					case -20:
						break;
					case 20:
						{ return tok(sym.LE); }
					case -21:
						break;
					case 21:
						{ return tok(sym.NEQ); }
					case -22:
						break;
					case 22:
						{ return tok(sym.GE); }
					case -23:
						break;
					case 23:
						{ return tok(sym.AND); }
					case -24:
						break;
					case 24:
						{ return tok(sym.OROR); }
					case -25:
						break;
					case 25:
						{ yybegin(COMMENT);  }
					case -26:
						break;
					case 26:
						{ newline(); }
					case -27:
						break;
					case 27:
						{ return tok(sym.TIMEVAL, yytext()); }
					case -28:
						break;
					case 28:
						{ return tok(sym.DATEVAL, yytext()); }
					case -29:
						break;
					case 29:
						{ return tok(sym.TIMESTAMPVAL, yytext()); }
					case -30:
						break;
					case 30:
						{ }
					case -31:
						break;
					case 31:
						{ newline(); yybegin(YYINITIAL); }
					case -32:
						break;
					case 32:
						{ string += yytext(); }
					case -33:
						break;
					case 33:
						{ err("Illegal string character: <" + yytext() + ">"); }
					case -34:
						break;
					case 34:
						{ yybegin(YYINITIAL); return tok(sym.STRING, string); }
					case -35:
						break;
					case 36:
						{ err("Illegal character: <" + yytext() + ">"); }
					case -36:
						break;
					case 37:
						{ }
					case -37:
						break;
					case 38:
						{ string += yytext(); }
					case -38:
						break;
					case 40:
						{ err("Illegal character: <" + yytext() + ">"); }
					case -39:
						break;
					default:
						yy_error(YY_E_INTERNAL,false);
					case -1:
					}
					yy_initial = true;
					yy_state = yy_state_dtrans[yy_lexical_state];
					yy_next_state = YY_NO_STATE;
					yy_last_accept_state = YY_NO_STATE;
					yy_mark_start();
					yy_this_accept = yy_acpt[yy_state];
					if (YY_NOT_ACCEPT != yy_this_accept) {
						yy_last_accept_state = yy_state;
						yy_mark_end();
					}
				}
			}
		}
	}
}
