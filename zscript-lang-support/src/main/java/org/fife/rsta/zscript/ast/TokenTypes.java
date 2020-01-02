/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;


public interface TokenTypes {

	int COMPILER_DIRECTIVE		= (0x01)<<16;
	int KEYWORD					= (0x02)<<16;
	int DATA_TYPE				= (0x04)<<16 | KEYWORD;
	int IDENTIFIER				= (0x08)<<16;
	int COMMENT					= (0x10)<<16;
	int WHITESPACE				= (0x20)<<16;
	int LITERAL					= (0x40)<<16;
	int SEPARATOR				= (0x80)<<16;
	int OPERATOR				= (0x100)<<16;
	int ASSIGNMENT_OPERATOR		= (0x200)<<16 | OPERATOR;

	/* Compiler directives */
	int CD_IMPORT				= COMPILER_DIRECTIVE|1;

	/* Keywords */
	int KEYWORD_GLOBAL			= KEYWORD|1;
	int KEYWORD_SCRIPT			= KEYWORD|2;
	int KEYWORD_LINK			= KEYWORD|3;
	int KEYWORD_SCREEN			= KEYWORD|4;
	int KEYWORD_GAME			= KEYWORD|5;
	int KEYWORD_CONST			= KEYWORD|6;
	int KEYWORD_ELSE			= KEYWORD|7;
	int KEYWORD_FOR				= KEYWORD|8;
	int KEYWORD_DO				= KEYWORD|9;
	int KEYWORD_IF				= KEYWORD|10;
	int KEYWORD_RETURN			= KEYWORD|11;
	int KEYWORD_VOID			= KEYWORD|12;
	int KEYWORD_WHILE			= KEYWORD|13;

	/* Data types */
	int DATA_TYPE_BOOL			= DATA_TYPE|1;
	int DATA_TYPE_EWEAPON		= DATA_TYPE|2;
	int DATA_TYPE_FFC			= DATA_TYPE|3;
	int DATA_TYPE_FLOAT			= DATA_TYPE|4;
	int DATA_TYPE_INT			= DATA_TYPE|5;
	int DATA_TYPE_ITEM			= DATA_TYPE|6;
	int DATA_TYPE_ITEMDATA		= DATA_TYPE|7;
	int DATA_TYPE_NPC			= DATA_TYPE|8;
	int DATA_TYPE_LWEAPON		= DATA_TYPE|9;

	/* Literals */
	int LITERAL_INT				= LITERAL|1;
	int LITERAL_FP				= LITERAL|2;
	int LITERAL_BOOLEAN			= LITERAL|3;
	int LITERAL_CHAR			= LITERAL|4;
	int LITERAL_STRING			= LITERAL|5;
	int LITERAL_NULL			= LITERAL|6;

	/* Separators */
	int SEPARATOR_LPAREN		= SEPARATOR|1;
	int SEPARATOR_RPAREN		= SEPARATOR|2;
	int SEPARATOR_LBRACE		= SEPARATOR|3;
	int SEPARATOR_RBRACE		= SEPARATOR|4;
	int SEPARATOR_LBRACKET		= SEPARATOR|5;
	int SEPARATOR_RBRACKET		= SEPARATOR|6;
	int SEPARATOR_SEMICOLON		= SEPARATOR|7;
	int SEPARATOR_COMMA			= SEPARATOR|8;
	int SEPARATOR_DOT			= SEPARATOR|9;

	/* Operators */
	int OPERATOR_EQUALS				= ASSIGNMENT_OPERATOR|1;
	int OPERATOR_GT					= OPERATOR|2;
	int OPERATOR_LT					= OPERATOR|3;
	int OPERATOR_LOGICAL_NOT		= OPERATOR|4;
	int OPERATOR_BITWISE_NOT		= OPERATOR|5;
	int OPERATOR_QUESTION			= OPERATOR|6;
	int OPERATOR_COLON				= OPERATOR|7;
	int OPERATOR_EQUALS_EQUALS		= OPERATOR|8;
	int OPERATOR_LTE				= OPERATOR|9;
	int OPERATOR_GTE				= OPERATOR|10;
	int OPERATOR_NE					= OPERATOR|11;
	int OPERATOR_LOGICAL_AND		= OPERATOR|12;
	int OPERATOR_LOGICAL_OR			= OPERATOR|13;
	int OPERATOR_INCREMENT			= OPERATOR|14;
	int OPERATOR_DECREMENT			= OPERATOR|15;
	int OPERATOR_PLUS				= OPERATOR|16;
	int OPERATOR_MINUS				= OPERATOR|17;
	int OPERATOR_TIMES				= OPERATOR|18;
	int OPERATOR_DIVIDE				= OPERATOR|19;
	int OPERATOR_BITWISE_AND		= OPERATOR|20;
	int OPERATOR_BITWISE_OR			= OPERATOR|21;
	int OPERATOR_BITWISE_XOR		= OPERATOR|22;
	int OPERATOR_MOD				= OPERATOR|23;
	int OPERATOR_LSHIFT				= OPERATOR|24;
	int OPERATOR_RSHIFT				= OPERATOR|25;
	int OPERATOR_MEMBER				= OPERATOR|26;
	int OPERATOR_PLUS_EQUALS		= ASSIGNMENT_OPERATOR|27;
	int OPERATOR_MINUS_EQUALS		= ASSIGNMENT_OPERATOR|28;
	int OPERATOR_TIMES_EQUALS		= ASSIGNMENT_OPERATOR|29;
	int OPERATOR_DIVIDE_EQUALS		= ASSIGNMENT_OPERATOR|30;
	int OPERATOR_BITWISE_AND_EQUALS	= ASSIGNMENT_OPERATOR|31;
	int OPERATOR_BITWISE_OR_EQUALS	= ASSIGNMENT_OPERATOR|32;
	int OPERATOR_BITWISE_XOR_EQUALS	= ASSIGNMENT_OPERATOR|33;
	int OPERATOR_MOD_EQUALS			= ASSIGNMENT_OPERATOR|34;

}
