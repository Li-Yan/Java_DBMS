package Parse;
import ErrorMsg.ErrorMsg;

%% 
%notunix
%implements Lexer, java_cup.runtime.Scanner
%function nextToken
%type java_cup.runtime.Symbol
%char
%state COMMENT
%state STRING

ALPHA=[A-Za-z]
DIGIT=[0-9]
WHITESPACE=[\ \t\b\012\f]
NEWLINE = \r\n
ID = {ALPHA}({ALPHA}|{DIGIT}|_|#)*
INTEGER = "-"?{DIGIT}+
DOUBLE = "-"?(({DIGIT}*"."{DIGIT}+)|({DIGIT}+"."{DIGIT}*))
DATE = "'"{DIGIT}{DIGIT}{DIGIT}{DIGIT}"-"(0[1-9]|1[0-2])"-"(0[1-9]|1[0-9]|2[0-9]|30|31)"'"
TIME = "'"(0[0-9]|1[0-9]|2[0-3])":"[0-5][0-9]":"(([0-5][0-9])|([0-5][0-9]"."{DIGIT}+))"'"
TIMESTAMP = {DATE}" "{TIME}
STRINGTEXT = ({ALPHA}|{DIGIT}|["`~!@#$%^&*()_+-=[];,./\\\"{}|:<>? "])*

%{
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
%}

%eofval{
{	
	if (yy_state == STRING || yy_state == COMMENT)
		err("Unexpected EOF!");
	return tok(sym.EOF, null);
}
%eofval}       


%%

<YYINITIAL>	"."		{ return tok(sym.DOT); }
<YYINITIAL>	","		{ return tok(sym.COMMA); }
<YYINITIAL>	";"		{ return tok(sym.SEMICOLON); }
<YYINITIAL>	"("		{ return tok(sym.LPAREN); }
<YYINITIAL>	")"		{ return tok(sym.RPAREN); }
<YYINITIAL>	"*"		{ return tok(sym.STAR); }
<YYINITIAL>	"="		{ return tok(sym.EQ); }
<YYINITIAL>	"<>"	{ return tok(sym.NEQ); }
<YYINITIAL>	"<"		{ return tok(sym.LT); }
<YYINITIAL>	"<="	{ return tok(sym.LE); }
<YYINITIAL>	">"		{ return tok(sym.GT); }
<YYINITIAL>	">="	{ return tok(sym.GE); }
<YYINITIAL>	"&&"	{ return tok(sym.AND); }
<YYINITIAL>	"||"	{ return tok(sym.OROR); }
<YYINITIAL>	"+"		{ return tok(sym.PLUS); }
<YYINITIAL>	"-"		{ return tok(sym.MINUS); }
<YYINITIAL>	"/"		{ return tok(sym.DIVIDE); }

<YYINITIAL>	{NEWLINE}	{ newline(); }
<YYINITIAL>	{WHITESPACE}+	{ }
<YYINITIAL> {ID} 		{ 
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
<YYINITIAL> {INTEGER} 	{ return tok(sym.INTVAL, new Integer(yytext())); }
<YYINITIAL> {DOUBLE} 	{ return tok(sym.DOUBLEVAL, new Double(yytext())); } 
<YYINITIAL> {DATE} 		{ return tok(sym.DATEVAL, yytext()); }
<YYINITIAL> {TIME} 		{ return tok(sym.TIMEVAL, yytext()); }
<YYINITIAL> {TIMESTAMP} { return tok(sym.TIMESTAMPVAL, yytext()); }

<YYINITIAL>	"//"		{ yybegin(COMMENT);  }
<COMMENT>	{NEWLINE}	{ newline(); yybegin(YYINITIAL); }
<COMMENT>	{STRINGTEXT}	{ }

<YYINITIAL>	\'	{ yybegin(STRING); string = ""; }
<STRING>	{STRINGTEXT}	{ string += yytext(); }
<STRING>	\'	{ yybegin(YYINITIAL); return tok(sym.STRING, string); }
<STRING>	.	{ err("Illegal string character: <" + yytext() + ">"); }

<YYINITIAL>	.	{ err("Illegal character: <" + yytext() + ">"); }