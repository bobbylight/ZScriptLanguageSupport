/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;


/**
 * Scanner for ZScript.<p>
 *
 * @author Robert Futrell
 * @version 0.1
 */
%%

%class ZScriptScanner
%implements TokenTypes
%unicode
%line
%column
%char
%type Token


%{

	/**
	 * Whether comments should be returned as tokens.
	 */
	private boolean returnComments;

	/**
	 * Whether whitespace should be returned as tokens.
	 */
	private boolean returnWhitespace;


	private Token createToken(int type) {
		return createToken(type, false);
	}


	private Token createToken(int type, boolean invalid) {
		return new Token(type, yytext(), yyline, yycolumn, yychar, invalid);
	}


	/**
	 * Returns the current column into the current line.
	 *
	 * @return The current column.
	 */
	public int getColumn() {
		return yycolumn;
	}


	/**
	 * Returns the current line into the document.
	 *
	 * @return The current line.
	 */
	public int getLine() {
		return yyline;
	}


	/**
	 * Returns the current offset into the document.
	 *
	 * @return The offset.
	 */
	public int getOffset() {
		return yychar;
	}


	/**
	 * Returns whether comments are returned as tokens.
	 *
	 * @return Whether comments are returned as tokens.
	 * @see #getReturnWhitespace()
	 */
	public boolean getReturnComments() {
		return returnComments;
	}


	/**
	 * Returns whether whitespace is returned as tokens.
	 *
	 * @return Whether whitespace is returned as tokens.
	 * @see #getReturnComments()
	 */
	public boolean getReturnWhitespace() {
		return returnWhitespace;
	}


	/**
	 * Sets whether comments are returned as tokens.
	 *
	 * @param returnComments Whether comments should be returned as tokens.
	 * @see #getReturnComments()
	 * @see #setReturnWhitespace(boolean)
	 */
	public void setReturnComments(boolean returnComments) {
		this.returnComments = returnComments;
	}


	/**
	 * Sets whether whitespace is returned as tokens.
	 *
	 * @param returnWhitespace Whether whitespace should be returned as tokens.
	 * @see #getReturnWhitespace()
	 * @see #setReturnComments(boolean)
	 */
	public void setReturnWhitespace(boolean returnWhitespace) {
		this.returnWhitespace = returnWhitespace;
	}


%}

/* JLS 3.4 - Line Terminators */
LineTerminator						= (\r|\n|\r\n)
InputCharacter						= ([\\][u]+{HexDigit}{4}|[^\r\n])

/* JLS 3.6 - White Space */
WhiteSpace							= (([ \t\f]|{LineTerminator})+)

/* JLS 3.7 - Comments (made non-recursive for JFlex) */
Comment								= ({TraditionalComment}|{EndOfLineComment})
TraditionalComment					= ("/*" [^*] ~"*/" | "/*" "*"+ "/")
EndOfLineComment					= ("//" {CharactersInLine}?)
CharactersInLine					= ({InputCharacter}+)

/* JLS 3.8 - Identifiers (made non-recursive for JFlex) */
Identifier							= ({IdentifierChars}) /* but not Keyword, BooleanLiteral, NullLiteral */
IdentifierChars						= ({JavaLetter}{JavaLetterOrDigit}*)
JavaLetter							= ([:jletter:])
JavaLetterOrDigit					= ([:jletterdigit:])

/* JLS 3.10.1 - Integer Literals */
IntegerLiteral						= ({DecimalIntegerLiteral}|{HexIntegerLiteral}|{OctalIntegerLiteral})
DecimalIntegerLiteral				= ({DecimalNumeral}{IntegerTypeSuffix}?)
HexIntegerLiteral					= ({HexNumeral}{IntegerTypeSuffix}?)
OctalIntegerLiteral					= ({OctalNumeral}{IntegerTypeSuffix}?)
IntegerTypeSuffix					= ([lL])
DecimalNumeral						= ("0"|{NonZeroDigit}{Digits}?)
Digits								= ({Digit}+)
Digit								= ("0"|{NonZeroDigit})
NonZeroDigit						= ([1-9])
HexNumeral							= ("0"[xX]{HexDigits})
HexDigits							= ({HexDigit}+)
HexDigit							= ([0-9a-fA-F])
OctalNumeral						= ("0"{OctalDigits})
OctalDigits							= ({OctalDigit}+)
OctalDigit							= ([0-7])

/* JLS 3.10.2 - Floating Point Literals */
/* TODO*/
FloatingPointLiteral				= ([0-9]+[\.][0-9]+[fF])

/* JLS 3.10.3 - Boolean Literals */
BooleanLiteral						= ("true"|"false")

/* JLS 3.10.4 - Character Literals */
CharacterLiteral					= ([\']({SingleCharacter}|{EscapeSequence})[\'])
SingleCharacter						= ([\\][u]+{HexDigit}{4}|[^\r\n\'\\])
InvalidCharLiteral					= ([\'][^\']*[\']?)

/* JLS 3.10.5 - String Literals */
StringLiteral						= ([\"]{StringCharacters}*[\"])
StringCharacters					= ({StringCharacter}+)
StringCharacter						= ([\\][u]+{HexDigit}{4}|[^\r\n\"\\]|{EscapeSequence})
//StringCharacter						= ([^\r\n\"\\]|{EscapeSequence})
InvalidStringLiteral				= ([\"][^\"]*[\"]?)

/* JLS 3.10.6 - Escape Sequences for Character and String Literals */
EscapeSequence						= ([\\][btnfr\"\'\\]|{OctalEscape})
OctalEscape							= ([\\]({OctalDigit}{OctalDigit}?|{ZeroToThree}{OctalDigit}{OctalDigit}))
OctalDigit							= ([0-7])
ZeroToThree							= ([0-3])

/* JLS 3.10.7 - The Null Literal */
NullLiteral							= ("null")


%%

<YYINITIAL> {

	{WhiteSpace}			{
								if (returnWhitespace) {
									return createToken(WHITESPACE);
								}
							}

	{Comment}				{
								if (returnComments) {
									return createToken(COMMENT);
								}
							}

	/* Compiler directives */
	"import"				{ return createToken(CD_IMPORT); }

	/* Keywords */
	"global"				{ return createToken(KEYWORD_GLOBAL); }
	"script"				{ return createToken(KEYWORD_SCRIPT); }
	"Link"					{ return createToken(KEYWORD_LINK); }
	"Screen"				{ return createToken(KEYWORD_SCREEN); }
	"Game"					{ return createToken(KEYWORD_GAME); }
	"const"					{ return createToken(KEYWORD_CONST); }
	"else"					{ return createToken(KEYWORD_ELSE); }
	"for"					{ return createToken(KEYWORD_FOR); }
	"do"					{ return createToken(KEYWORD_DO); }
	"if"					{ return createToken(KEYWORD_IF); }
	"return"				{ return createToken(KEYWORD_RETURN); }
	"void"					{ return createToken(KEYWORD_VOID); }
	"while"					{ return createToken(KEYWORD_WHILE); }

	/* Data types */
	"bool"					{ return createToken(DATA_TYPE_BOOL); }
	"eweapon"				{ return createToken(DATA_TYPE_EWEAPON); }
	"ffc"					{ return createToken(DATA_TYPE_FFC); }
	"float"					{ return createToken(DATA_TYPE_FLOAT); }
	"int"					{ return createToken(DATA_TYPE_INT); }
	"item"					{ return createToken(DATA_TYPE_ITEM); }
	"itemdata"				{ return createToken(DATA_TYPE_ITEMDATA); }
	"npc"					{ return createToken(DATA_TYPE_NPC); }
	"lweapon"				{ return createToken(DATA_TYPE_LWEAPON); }

	/* Literals */
	{IntegerLiteral}		{ return createToken(LITERAL_INT); }
	{FloatingPointLiteral}	{ return createToken(LITERAL_FP); }
	{BooleanLiteral}		{ return createToken(LITERAL_BOOLEAN); }
	{CharacterLiteral}		{ return createToken(LITERAL_CHAR); }
	{StringLiteral}			{ return createToken(LITERAL_STRING); }
	{NullLiteral}			{ return createToken(LITERAL_NULL); }
	{InvalidCharLiteral}	{ return createToken(LITERAL_CHAR, true); }
	{InvalidStringLiteral}	{ return createToken(LITERAL_STRING, true); }

	{Identifier}			{ return createToken(IDENTIFIER); }

	/* Separators  (JLS 3.11) */
	"("						{ return createToken(SEPARATOR_LPAREN); }
	")"						{ return createToken(SEPARATOR_RPAREN); }
	"{"						{ return createToken(SEPARATOR_LBRACE); }
	"}"						{ return createToken(SEPARATOR_RBRACE); }
	"["						{ return createToken(SEPARATOR_LBRACKET); }
	"]"						{ return createToken(SEPARATOR_RBRACKET); }
	";"						{ return createToken(SEPARATOR_SEMICOLON); }
	","						{ return createToken(SEPARATOR_COMMA); }
	"."						{ return createToken(SEPARATOR_DOT); }

	/* Operators (JLS 3.12) */
	"="						{ return createToken(OPERATOR_EQUALS); }
	">"						{ return createToken(OPERATOR_GT); }
	"<"						{ return createToken(OPERATOR_LT); }
	"!"						{ return createToken(OPERATOR_LOGICAL_NOT); }
	"~"						{ return createToken(OPERATOR_BITWISE_NOT); }
	"?"						{ return createToken(OPERATOR_QUESTION); }
	":"						{ return createToken(OPERATOR_COLON); }
	"=="					{ return createToken(OPERATOR_EQUALS_EQUALS); }
	"<="					{ return createToken(OPERATOR_LTE); }
	">="					{ return createToken(OPERATOR_GTE); }
	"!="					{ return createToken(OPERATOR_NE); }
	"&&"					{ return createToken(OPERATOR_LOGICAL_AND); }
	"||"					{ return createToken(OPERATOR_LOGICAL_OR); }
	"++"					{ return createToken(OPERATOR_INCREMENT); }
	"--"					{ return createToken(OPERATOR_DECREMENT); }
	"+"						{ return createToken(OPERATOR_PLUS); }
	"-"						{ return createToken(OPERATOR_MINUS); }
	"*"						{ return createToken(OPERATOR_TIMES); }
	"/"						{ return createToken(OPERATOR_DIVIDE); }
	"&"						{ return createToken(OPERATOR_BITWISE_AND); }
	"|"						{ return createToken(OPERATOR_BITWISE_OR); }
	"^"						{ return createToken(OPERATOR_BITWISE_XOR); }
	"%"						{ return createToken(OPERATOR_MOD); }
	"<<"					{ return createToken(OPERATOR_LSHIFT); }
	">>"					{ return createToken(OPERATOR_RSHIFT); }
	"->"					{ return createToken(OPERATOR_MEMBER); }
	"+="					{ return createToken(OPERATOR_PLUS_EQUALS); }
	"-="					{ return createToken(OPERATOR_MINUS_EQUALS); }
	"*="					{ return createToken(OPERATOR_TIMES_EQUALS); }
	"/="					{ return createToken(OPERATOR_DIVIDE_EQUALS); }
	"&="					{ return createToken(OPERATOR_BITWISE_AND_EQUALS); }
	"|="					{ return createToken(OPERATOR_BITWISE_OR_EQUALS); }
	"^="					{ return createToken(OPERATOR_BITWISE_XOR_EQUALS); }
	"%="					{ return createToken(OPERATOR_MOD_EQUALS); }

	/* Unhandled stuff. */
	.						{ return createToken(IDENTIFIER, true); }

}
