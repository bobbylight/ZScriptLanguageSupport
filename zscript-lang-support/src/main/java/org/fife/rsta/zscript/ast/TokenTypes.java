/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;


public interface TokenTypes {

	public static final int COMPILER_DIRECTIVE		= (0x01)<<16;
	public static final int KEYWORD					= (0x02)<<16;
	public static final int DATA_TYPE				= (0x04|KEYWORD)<<16;
	public static final int IDENTIFIER				= (0x08)<<16;
	public static final int COMMENT					= (0x10)<<16;
	public static final int WHITESPACE				= (0x20)<<16;
	public static final int LITERAL					= (0x40)<<16;
	public static final int SEPARATOR				= (0x80)<<16;
	public static final int OPERATOR				= (0x100)<<16;
	public static final int ASSIGNMENT_OPERATOR		= (0x200|OPERATOR)<<16;

	/* Compiler directives */
	public static final int CD_IMPORT				= COMPILER_DIRECTIVE|1;

	/* Keywords */
	public static final int KEYWORD_GLOBAL			= KEYWORD|1;
	public static final int KEYWORD_SCRIPT			= KEYWORD|2;
	public static final int KEYWORD_LINK			= KEYWORD|3;
	public static final int KEYWORD_SCREEN			= KEYWORD|4;
	public static final int KEYWORD_GAME			= KEYWORD|5;
	public static final int KEYWORD_CONST			= KEYWORD|6;
	public static final int KEYWORD_ELSE			= KEYWORD|7;
	public static final int KEYWORD_FOR				= KEYWORD|8;
	public static final int KEYWORD_DO				= KEYWORD|9;
	public static final int KEYWORD_IF				= KEYWORD|10;
	public static final int KEYWORD_RETURN			= KEYWORD|11;
	public static final int KEYWORD_VOID			= KEYWORD|12;
	public static final int KEYWORD_WHILE			= KEYWORD|13;

	/* Data types */
	public static final int DATA_TYPE_BOOL			= DATA_TYPE|1;
	public static final int DATA_TYPE_EWEAPON		= DATA_TYPE|2;
	public static final int DATA_TYPE_FFC			= DATA_TYPE|3;
	public static final int DATA_TYPE_FLOAT			= DATA_TYPE|4;
	public static final int DATA_TYPE_INT			= DATA_TYPE|5;
	public static final int DATA_TYPE_ITEM			= DATA_TYPE|6;
	public static final int DATA_TYPE_ITEMDATA		= DATA_TYPE|7;
	public static final int DATA_TYPE_NPC			= DATA_TYPE|8;
	public static final int DATA_TYPE_LWEAPON		= DATA_TYPE|9;

	/* Literals */
	public static final int LITERAL_INT				= LITERAL|1;
	public static final int LITERAL_FP				= LITERAL|2;
	public static final int LITERAL_BOOLEAN			= LITERAL|3;
	public static final int LITERAL_CHAR			= LITERAL|4;
	public static final int LITERAL_STRING			= LITERAL|5;
	public static final int LITERAL_NULL			= LITERAL|6;

	/* Separators */
	public static final int SEPARATOR_LPAREN		= SEPARATOR|1;
	public static final int SEPARATOR_RPAREN		= SEPARATOR|2;
	public static final int SEPARATOR_LBRACE		= SEPARATOR|3;
	public static final int SEPARATOR_RBRACE		= SEPARATOR|4;
	public static final int SEPARATOR_LBRACKET		= SEPARATOR|5;
	public static final int SEPARATOR_RBRACKET		= SEPARATOR|6;
	public static final int SEPARATOR_SEMICOLON		= SEPARATOR|7;
	public static final int SEPARATOR_COMMA			= SEPARATOR|8;
	public static final int SEPARATOR_DOT			= SEPARATOR|9;

	/* Operators */
	public static final int OPERATOR_EQUALS				= ASSIGNMENT_OPERATOR|1;
	public static final int OPERATOR_GT					= OPERATOR|2;
	public static final int OPERATOR_LT					= OPERATOR|3;
	public static final int OPERATOR_LOGICAL_NOT		= OPERATOR|4;
	public static final int OPERATOR_BITWISE_NOT		= OPERATOR|5;
	public static final int OPERATOR_QUESTION			= OPERATOR|6;
	public static final int OPERATOR_COLON				= OPERATOR|7;
	public static final int OPERATOR_EQUALS_EQUALS		= OPERATOR|8;
	public static final int OPERATOR_LTE				= OPERATOR|9;
	public static final int OPERATOR_GTE				= OPERATOR|10;
	public static final int OPERATOR_NE					= OPERATOR|11;
	public static final int OPERATOR_LOGICAL_AND		= OPERATOR|12;
	public static final int OPERATOR_LOGICAL_OR			= OPERATOR|13;
	public static final int OPERATOR_INCREMENT			= OPERATOR|14;
	public static final int OPERATOR_DECREMENT			= OPERATOR|15;
	public static final int OPERATOR_PLUS				= OPERATOR|16;
	public static final int OPERATOR_MINUS				= OPERATOR|17;
	public static final int OPERATOR_TIMES				= OPERATOR|18;
	public static final int OPERATOR_DIVIDE				= OPERATOR|19;
	public static final int OPERATOR_BITWISE_AND		= OPERATOR|20;
	public static final int OPERATOR_BITWISE_OR			= OPERATOR|21;
	public static final int OPERATOR_BITWISE_XOR		= OPERATOR|22;
	public static final int OPERATOR_MOD				= OPERATOR|23;
	public static final int OPERATOR_LSHIFT				= OPERATOR|24;
	public static final int OPERATOR_RSHIFT				= OPERATOR|25;
	public static final int OPERATOR_MEMBER				= OPERATOR|26;
	public static final int OPERATOR_PLUS_EQUALS		= ASSIGNMENT_OPERATOR|27;
	public static final int OPERATOR_MINUS_EQUALS		= ASSIGNMENT_OPERATOR|28;
	public static final int OPERATOR_TIMES_EQUALS		= ASSIGNMENT_OPERATOR|29;
	public static final int OPERATOR_DIVIDE_EQUALS		= ASSIGNMENT_OPERATOR|30;
	public static final int OPERATOR_BITWISE_AND_EQUALS	= ASSIGNMENT_OPERATOR|31;
	public static final int OPERATOR_BITWISE_OR_EQUALS	= ASSIGNMENT_OPERATOR|32;
	public static final int OPERATOR_BITWISE_XOR_EQUALS	= ASSIGNMENT_OPERATOR|33;
	public static final int OPERATOR_MOD_EQUALS			= ASSIGNMENT_OPERATOR|34;

}