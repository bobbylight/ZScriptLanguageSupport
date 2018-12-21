/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;

import java.io.IOException;

import javax.swing.text.Position;

import org.fife.io.DocumentReader;
import org.fife.rsta.zscript.CodeCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice.Level;


/**
 * Parses ZScript source code.
 *
 * <pre>
 * ScriptFile        -> CompilerDirective* Member*
 * CompilerDirective -> "import" STRING
 * Member            -> (Function | Script | GlobalVar)*
 *
 * Function          -> ( PrimitiveType | "void" ) IDENTIFIER "(" Args? ")" CodeBlock
 * Args              -> Arg ( "," Arg )*
 * Arg               -> PrimitiveType IDENTIFIER
 *
 * VariableDec       -> "const"? PrimitiveType IDENTIFIER ( "=" VarValue )? ";"
 * VarValue          -> ( INT | FLOAT | BOOL | STRING )
 * </pre>
 *  
 * @author Robert Futrell
 * @version 1.0
 */
public class AstFactory implements TokenTypes {

	private Scanner scanner;
	private ZScriptParseResult result;
//private CodeCompletionProvider ccp;


	public AstFactory(RSyntaxDocument doc, Parser parser, CodeCompletionProvider ccp) {
		scanner = new Scanner(new DocumentReader(doc));
		result = new ZScriptParseResult(parser);
		//this.ccp = ccp;
	}


	public ZScriptParseResult parse() {

		RootNode root = new RootNode(scanner.createOffset(0));
		try {
			parseRoot(root);
		} catch (ParseException pe) {
			result.addNotice(pe.getToken(), pe.getMessage());
		} catch (IOException ioe) {
			result.addNotice(ioe.getMessage(), scanner.getLine());
		}

		ZScriptAst ast = new ZScriptAst();
		ast.setRootNode(root);
		result.setAst(ast);
		return result;

	}


	private CodeBlock parseCodeBlock(CodeBlockParent parent, Token openCurly)
			throws IOException {

		CodeBlock block = new CodeBlock(parent, scanner.createOffset(openCurly.getOffset()));
		parent.setCodeBlock(block);

		try {

			int blockDepth = 1;

			Token t = null;
			while (blockDepth>0) {

				t = scanner.yylexNonNull("Unexpected end of input");

				switch (t.getType()) {

					case SEPARATOR_RBRACE:
						blockDepth--;
						break;

					default:
						parseStatement(block, t);
						break;

				}

			}

			// "t" is now closing curly
			block.setEndOffset(scanner.createOffset(t.getOffset()));

		} finally {
			// TODO: Determine whether this makes any difference
			block.cacheStatementsWithChildBlocks();
		}

		return block;

	}


	private void parseDoWhile(CodeBlock block, Token doToken) throws IOException {

		DoWhileNode doWhile = new DoWhileNode(scanner.createOffset(doToken.getOffset()));
		block.addStatement(doWhile);

		// ZScript is cool with a single statement as do/while body; e.g.
		// do foo(); while bar();

		Token next = scanner.yylexNonNull("'{' or statement expected after 'do'");
		if (next.isType(SEPARATOR_LBRACE)) {
			parseCodeBlock(doWhile, next);
		}
		else {
			scanner.eatThroughNextSkippingBlocks(SEPARATOR_SEMICOLON);
		}

		scanner.yylexNonNull(KEYWORD_WHILE, "'while' expected");
		scanner.yylexNonNull(SEPARATOR_LPAREN, "'(' expected");
		scanner.eatThroughClosingParen();
		// The semicolon trailing a do-while loop is optional (!).
		if (scanner.yyPeekCheckType()==SEPARATOR_SEMICOLON) {
			scanner.yylex();
		}
		doWhile.setEndOffset(scanner.createOffset(next.getEndOffset()));

	}


	private boolean parseElse(CodeBlock block, Token elseToken, IfNode ifNode) throws IOException {

		ElseNode elseNode = new ElseNode(scanner.createOffset(elseToken.getOffset()), ifNode);
		ifNode.addElse(elseNode);
		block.addStatement(elseNode);
		boolean canChainAnother = false;

		Token next = scanner.yylexNonNull("Unexpected end of input");
		switch (next.getType()) {
			case KEYWORD_IF: // else if (...)

				elseNode.setConditional(true);
				// TODO: Share this code with parseIf()
				
				scanner.yylexNonNull(SEPARATOR_LPAREN, "'(' expected");

				// Expression
				scanner.eatThroughClosingParen();

				next = scanner.yylexNonNull("'{' or statement expected");
				if (next.getType()==SEPARATOR_LBRACE) {
					parseCodeBlock(elseNode, next);
				}
				else if (next.getType()!=SEPARATOR_SEMICOLON) {
					scanner.eatThroughNextSkippingBlocks(SEPARATOR_SEMICOLON);
				}

				canChainAnother = true;
				break;

			case SEPARATOR_LBRACE:
				parseCodeBlock(elseNode, next);
				break;

			default:
				// Just a single statement after the else
				scanner.eatThroughNextSkippingBlocksAndStuffInParens(SEPARATOR_SEMICOLON, -1);
				break;
		}

		Token last = scanner.getMostRecentToken();
		if (last!=null) {
			elseNode.setEndOffset(scanner.createOffset(last.getEndOffset()));
		}

		return canChainAnother;

	}


	private void parseFor(CodeBlock block, Token forToken) throws IOException {

		// I assume for-loops can only declare one variable, since you cannot
		// initialize multiple variables on a single line (or can you?)
		ForNode forNode = new ForNode(scanner.createOffset(forToken.getOffset()));
		block.addStatement(forNode);
		VariableDecNode forNodeVarDec = null;
		Token counter = null;

		scanner.yylexNonNull(SEPARATOR_LPAREN, "'(' expected");

		// counter variable initialization
		Token next = scanner.yylexNonNull("Unexpected end of input");
		switch (next.getType()) {
			case IDENTIFIER:
				counter = next;
				forNodeVarDec = block.getVariableDecByName(counter.getLexeme());
				if (forNodeVarDec==null) {
					result.addNotice(counter, "Undefined variable");
				}
				forNodeVarDec = null; // Previously defined, don't save in ForNode
				scanner.eatThroughNextSkippingBlocksAndStuffInParens(SEPARATOR_SEMICOLON, -1);
				break;
			default:
				if (next.isBasicType()) {
					Token type = next;
					counter = scanner.yylexNonNull(IDENTIFIER, "Counter variable expected");
					forNodeVarDec = new VariableDecNode(scanner.createOffset(type.getOffset()));
					forNodeVarDec.setType(type.getLexeme());
					forNodeVarDec.setName(counter.getLexeme());
					forNodeVarDec.setEndOffset(scanner.createOffset(counter.getEndOffset()));
					forNode.setVariableDeclaration(forNodeVarDec);
					scanner.eatThroughNextSkippingBlocksAndStuffInParens(SEPARATOR_SEMICOLON, -1);
				}
				else {
					// Unexpected token - bail
					result.addNotice(next, "Counter variable expected");
					scanner.eatThroughClosingParen();
					next = scanner.yylexNonNull("'{' or statement expected");
					if (next.getType()==SEPARATOR_LBRACE) {
						CodeBlock block2 = parseCodeBlock(forNode, next);
						forNode.setEndOffset(scanner.createOffset(block2.getEndOffset()));
					}
					return;
				}
				break;
		}

		// test expression
		// TODO: Validate?  Note param is optional
		boolean skipUpdateExpression = false;
		next = scanner.eatThroughNextSkippingBlocksAndStuffInParens(
				SEPARATOR_SEMICOLON, -1);
		if (next==null) {
			throw new IOException("Unexpected end of input");
		}
		else if (next.getType()==SEPARATOR_RPAREN) {
			result.addNotice(next, "Missing update expression of for-loop");
			skipUpdateExpression= true;
		}

		// update expression
		if (!skipUpdateExpression) {
			scanner.eatThroughClosingParen();
		}

		next = scanner.yylexNonNull("'{' or statement expected");
		if (next.getType()==SEPARATOR_LBRACE) {
			parseCodeBlock(forNode, next);
		}
		else {
			// Single-statement body of for-loop goes into the ether.
			scanner.eatThroughNextSkippingBlocksAndStuffInParens(SEPARATOR_SEMICOLON, -1);
		}

		Token last = scanner.getMostRecentToken();
		if (last!=null) {
			forNode.setEndOffset(scanner.createOffset(last.getEndOffset()));
		}

	}


	/**
	 * <pre>
	 * Function -> ( PrimitiveType | "void" ) IDENTIFIER "(" Args? ")" CodeBlock
	 * Args      -> Arg ( "," Arg )*
	 * Arg       -> PrimitiveType IDENTIFIER
	 * </pre>
	 *
	 * @param type The return type token for the function.
	 * @param name The name of the function, or <code>null</code> if not yet
	 *        parsed.
	 */
	private void parseFunction(FunctionContainer parent, Token type, Token name)
			throws IOException {

		FunctionDecNode funcNode = new FunctionDecNode(scanner.createOffset(type.getOffset()));
		funcNode.setType(type.getLexeme());
		parent.addFunctionDec(funcNode);

		if (name==null) {
			name = scanner.yylexNonNull(IDENTIFIER, "Function name expected");
		}
		funcNode.setName(name.getLexeme());
		funcNode.setEndOffset(scanner.createOffset(name.getEndOffset()+1));

		scanner.yylexNonNull(SEPARATOR_LPAREN, "'(' expected");

		Token t = scanner.yylexNonNull("')' expected");
		while (t.getType()!=SEPARATOR_RPAREN) {

			if (!t.isBasicType()) {
				result.addNotice(t, "Argument type expected");
				// Skip to function start and bail
				scanner.eatThroughNext(SEPARATOR_LBRACE, false);
				break; // Jump to code block parsing
			}

			Token argName = scanner.yylexNonNull(IDENTIFIER, "Argument name expected");
			VariableDecNode argNode = new VariableDecNode(scanner.createOffset(argName.getOffset()));
			argNode.setName(argName.getLexeme());
			argNode.setType(t.getLexeme());
			argNode.setEndOffset(scanner.createOffset(argName.getEndOffset()+1)); // why +1???
			funcNode.addArgument(argNode);

			t = scanner.yylexNonNull(SEPARATOR_RPAREN, SEPARATOR_COMMA, "')' or ',' expected");
			if (t.getType()==SEPARATOR_COMMA) {
				Token comma = t;
				t = scanner.yylexNonNull("Unexpected end of file");
				if (t.isType(SEPARATOR_RPAREN)) {
					result.addNotice(comma, "Unnecessary comma");
				}
			}

		}

		Token openCurly = scanner.yylexNonNull(SEPARATOR_LBRACE, "'{' expected");
		parseCodeBlock(funcNode, openCurly);

	}


	private void parseFunctionOrVariable(MemberContainer parent,
			Token basicType) throws IOException {

		Token name = scanner.yylexNonNull(IDENTIFIER, "Function or variable name expected");

		int nextTokenType = scanner.yyPeekCheckType();
		switch (nextTokenType) {
			case SEPARATOR_LPAREN:
				parseFunction(parent, basicType, name);
				break;
			case OPERATOR_EQUALS:
			case SEPARATOR_SEMICOLON:
				parseVariable(parent, basicType, name, false);
				result.addNotice(name, "Script variables are deprecated.  Use file scope instead.",
						ParserNotice.Level.WARNING);
				break;
			default:
				break;
		}

	}


	private void parseFunctionVariableOrScript(RootNode root,
			Token basicType, Token constantToken) throws IOException {

		Token name = scanner.yylexNonNull(IDENTIFIER, KEYWORD_SCRIPT, "Function name, variable name, or 'script' expected");

		if (name.getType()==KEYWORD_SCRIPT) {

			if (constantToken!=null) {
				result.addNotice(constantToken, "Scripts cannot have modifier 'const'");
			}

			ScriptType scriptType = ScriptType.forTokenType(basicType.getType());
			if (scriptType == null) {
				result.addNotice(basicType, "Invalid script type", Level.ERROR);
			}

			name = scanner.yylexNonNull(IDENTIFIER, "Script name expected");
			ScriptNode script = new ScriptNode(scriptType,
					scanner.createOffset(name.getOffset()));
			script.setName(name.getLexeme());
			script.setType(basicType.getLexeme());
			script.setEndOffset(scanner.createOffset(script.getStartOffset()+name.getLength()));
			root.addScript(script);
			parseScript(root, script);
			return;

		}

		int nextTokenType = scanner.yyPeekCheckType();
		switch (nextTokenType) {
			case SEPARATOR_LPAREN:
				if (constantToken!=null) {
					result.addNotice(constantToken, "Functions cannot have modifier 'const'");
				}
				parseFunction(root, basicType, name);
				break;
			case OPERATOR_EQUALS:
			case SEPARATOR_SEMICOLON:
				parseVariable(root, basicType, name, constantToken!=null);
				break;
			default:
				break;
		}

	}


	private IfNode parseIf(CodeBlock block, Token ifToken) throws IOException {

		IfNode ifNode = new IfNode(scanner.createOffset(ifToken.getOffset()));
		block.addStatement(ifNode);

		scanner.yylexNonNull(SEPARATOR_LPAREN, "'(' expected");

		// Expression
		scanner.eatThroughClosingParen();

		Token next = scanner.yylexNonNull("'{' or statement expected");
		if (next.getType()==SEPARATOR_LBRACE) {
			parseCodeBlock(ifNode, next);
		}
		else if (next.getType()!=SEPARATOR_SEMICOLON) {
			scanner.eatThroughNextSkippingBlocks(SEPARATOR_SEMICOLON);
		}

		Token last = scanner.getMostRecentToken();
		if (last!=null) {
			ifNode.setEndOffset(scanner.createOffset(last.getEndOffset()));
		}

		return ifNode;
	}


	private ImportNode parseImport(Token importToken) throws IOException {

		ImportNode importNode = new ImportNode(scanner.createOffset(importToken.getColumn()));
		Token file = scanner.yylexNonNull(LITERAL_STRING, "File to import expected");
		String fileName = file.getLexeme();
		fileName = fileName.substring(1, fileName.length()-1);
		importNode.setImport(fileName);
		importNode.setEndOffset(scanner.createOffset(file.getEndOffset()+1));

		verifyNextTokenNotSemicolon();

		return importNode;

	}


	private void parseRoot(RootNode root) throws IOException {

		Token token = null;

		// Grab all imports (usually all grouped at the top)
		while ((token=scanner.yylex())!=null && token.isType(CD_IMPORT)) {
			ImportNode importNode = parseImport(token);
			root.addImport(importNode);
		}
		scanner.yyPushback(token);

		// Any permutation of global variables, functions, and scripts
		while ((token=scanner.yylex())!=null) {

			Token constantToken = null;
			int type = token.getType();
			switch (type) {

				case KEYWORD_GLOBAL:
					scanner.yylexNonNull(KEYWORD_SCRIPT, "'script' expected");
					Token name = scanner.yylexNonNull(IDENTIFIER, "Script name expected");
					ScriptNode script = new ScriptNode(ScriptType.GLOBAL,
							scanner.createOffset(name.getOffset()));
					script.setName(name.getLexeme());
					script.setEndOffset(scanner.createOffset(script.getStartOffset()+name.getLength()+1));
					script.setType("global");
					root.addScript(script);
					parseScript(root, script);
					break;

				case KEYWORD_VOID:
					parseFunction(root, token, null);
					break;

				case CD_IMPORT: // imports can be anywhere in the file!
					ImportNode importNode = parseImport(token);
					root.addImport(importNode);
					break;

				case KEYWORD_CONST:
					constantToken = token;
					token = scanner.yylexNonNull("Unexpected end of input");
					// Fall through

				default:
					// The yyPeekCheckType() condition will only be hit in error
					// conditions, e.g. "xxx script DoWork {", but is needed
					// for better error detection/recovery.
					if (token.isBasicType() ||
							scanner.yyPeekCheckType() == TokenTypes.KEYWORD_SCRIPT) {
						parseFunctionVariableOrScript(root, token, constantToken);
					}
					break;

			}

		}

	}


	private void parseScript(RootNode root, ScriptNode script) throws IOException {

		Token token = null;
		while ((token=scanner.yylexNonNull("Unexpected end of input")).getType()!=SEPARATOR_LBRACE) {
			result.addNotice(token, "Unexpected token, expected '{'");
		}
		script.setBodyStart(scanner.createOffset(token.getOffset()));

		int blockDepth = 1;

		// A script is made up of 0 or more functions
		while (blockDepth>0 && (token=scanner.yylexNonNull("Unexpected end of input"))!=null) {

			int type = token.getType();
			switch (type) {

				case KEYWORD_VOID:
					parseFunction(script, token, null);
					break;

				case CD_IMPORT: // imports can be anywhere in the file!
					ImportNode importNode = parseImport(token);
					// TODO: Are imports actually scoped?  Imports being allowed
					// here implies that they are.  If so, we shouldn't just
					// add 'em all to the root node like this...
					root.addImport(importNode);
					break;

				case SEPARATOR_LBRACE:
					blockDepth++;
					break;

				case SEPARATOR_RBRACE:
					blockDepth--;
					break;

				default:
					if (token.isBasicType()) {
						parseFunctionOrVariable(script, token);
					}
					else {
						// Variables are deprecated, so don't mention them in the error
						result.addNotice(token, "Unexpected token; scripts can only contain functions");
					}
					break;

			}

		}

		Position endOffs = scanner.createOffset(token.getOffset());
		script.setBodyEnd(endOffs);
		script.setEndOffset(endOffs);

	}


	/**
	 * Not yet complete.
	 * 
	 * @param block
	 * @param t
	 * @throws IOException
	 */
	private void parseStatement(CodeBlock block, Token t) throws IOException {

		switch (t.getType()) {

			case KEYWORD_CONST:
				Token type = yylexDataType();
				block.addStatement(parseVariable(block, type, null, true));
				break;

			case KEYWORD_FOR:
				parseFor(block, t);
				break;

			case KEYWORD_DO:
				parseDoWhile(block, t);
				break;

			case KEYWORD_IF:
				IfNode ifNode = parseIf(block, t);
				if (scanner.yyPeekCheckType()==KEYWORD_ELSE) {
					boolean canChainAnother = true;
					while (canChainAnother && scanner.yyPeekCheckType()==KEYWORD_ELSE) {
						canChainAnother = parseElse(block, scanner.yylex(), ifNode);
					}
				}
				break;

			case KEYWORD_WHILE:
				parseWhile(block, t);
				break;

			/*
			 * Not sure this is valid in ZScript.
			case SEPARATOR_LBRACE:
				block.addStatement(parseCodeBlock(block, t));
				break;
			*/

			default:
				if (t.isBasicType()) {
					block.addStatement(parseVariable(block, t, null, false));
				}
				else {
					scanner.yyPushback(t);
					// Possibilities: 'var++;', 'var = "foo";', etc.
					parseUnhandledStatement(block);
				}
				break;

		}

	}


	/**
	 * These are statements we don't currently parse.  The only reason to do so
	 * would be for syntax checking purposes.
	 *
	 * @param parent
	 * @throws IOException
	 */
	private void parseUnhandledStatement(CodeBlock parent) throws IOException {

		// Possibilities: 'var++;', 'var = "foo";', etc.

		Token next = scanner.eatThroughNextSkippingBlocksAndStuffInParens(
				SEPARATOR_SEMICOLON, SEPARATOR_RBRACE);
		// Missed a semicolon
		if (next!=null && next.getType()==SEPARATOR_RBRACE) {
			result.addNotice(next, "Missing ';'");
			scanner.yyPushback(next);
		}

	}


	/**
	 * <pre>
	 * VariableDec -> "const"? PrimitiveType IDENTIFIER ( "=" VarValue )? ";"
	 * VarValue    -> ( INT | FLOAT | BOOL | STRING )
	 * </pre>
	 *
	 * @param type The type of the variable.
	 * @param name The name of the variable, or <code>null</code> if not yet
	 *        parsed.
	 */
	private VariableDecNode parseVariable(VariableContainer parent, Token type,
			Token name, boolean isConst) throws IOException {

		VariableDecNode varNode = new VariableDecNode(scanner.createOffset(type.getOffset()));
		varNode.setType(type.getLexeme());
		varNode.setConstant(isConst);

		if (name==null) {
			name = scanner.yylexNonNull(IDENTIFIER, "Variable name expected");
		}
		varNode.setName(name.getLexeme());
		varNode.setEndOffset(scanner.createOffset(name.getEndOffset()+1));

		ShadowedVarInfo shadowedVarInfo = parent.addVariableDec(varNode); // Requires name to be set
		if (shadowedVarInfo!=null) {
			String msg = "Variable declaration shadows prior variable " +
					shadowedVarInfo.getType() + " " + shadowedVarInfo.getName();
					// + " on line " + shadowed.getLine();
			result.addNotice(name, msg, ParserNotice.Level.WARNING);
		}

		Token next = scanner.yylexNonNull("Unexpected end of input");
		boolean isArray = false;
		if (next.getType()==SEPARATOR_LBRACKET) {
			isArray = true;
			varNode.incTypeArrayDepth();
			next = scanner.yylexNonNull(LITERAL_INT, SEPARATOR_RBRACKET, "Array size or closing ']' expected");
			if (next.getType()==LITERAL_INT) {
				scanner.yylexNonNull(SEPARATOR_RBRACKET, "Closing ']' expected");
			}
			next = scanner.yylexNonNull("';' or '=' expected");
		}

		switch (next.getType()) {

			case SEPARATOR_SEMICOLON:
				break;

			case OPERATOR_EQUALS:
				//if (arraySizeToken!=null) {
				//	result.addNotice(arraySizeToken, "Array size should not be given for initialized arrays");
				//}
				int nextType = scanner.yyPeekCheckType();
				switch (nextType) {
					case SEPARATOR_LBRACE:
						Token lbrace = scanner.yylex();
						if (!isArray) {
							result.addNotice(lbrace, "Array initializer used on non-array variable");
						}
						scanner.eatThroughNextSkippingBlocks(SEPARATOR_RBRACE);
						scanner.yylexNonNull(SEPARATOR_SEMICOLON, "';' expected");
						break;
					default:
						// String, function (?)
						scanner.eatThroughNext(SEPARATOR_SEMICOLON);
						break;
				}
				break;

			default:
				result.addNotice(next, "Expected ';' or '='");
				scanner.eatThroughNextSkippingBlocks(SEPARATOR_SEMICOLON);
				break;
		}

		return varNode;

	}


	private void parseWhile(CodeBlock block, Token whileToken) throws IOException {

		WhileNode whileNode = new WhileNode(scanner.createOffset(whileToken.getOffset()));
		block.addStatement(whileNode);

		scanner.yylexNonNull(SEPARATOR_LPAREN, "'(' expected");

		// Expression
		scanner.eatThroughClosingParen();

		Token next = scanner.yylexNonNull("'{' or statement expected");
		if (next.getType()==SEPARATOR_LBRACE) {
			parseCodeBlock(whileNode, next);
		}
		else if (next.getType()!=SEPARATOR_SEMICOLON) {
			scanner.eatThroughNextSkippingBlocks(SEPARATOR_SEMICOLON);
		}

		Token last = scanner.getMostRecentToken();
		if (last!=null) {
			whileNode.setEndOffset(scanner.createOffset(last.getEndOffset()));
		}

	}


	private void verifyNextTokenNotSemicolon() throws IOException {
		int type = scanner.yyPeekCheckType();
		if (type==SEPARATOR_SEMICOLON) {
			Token semicolon = scanner.yylex();
			result.addNotice(semicolon, "Unexpected semicolon");
		}
	}


	private Token yylexDataType() throws IOException {
		Token t = scanner.yylexNonNull("Unexpected end of input");
		if (t.isBasicType()) {
			return t;
		}
		if (t.isIdentifier()) {
			// Assume next token should be variable name
			Token next = scanner.yyPeekNonNull("Unexpected end of input");
			if (next.isIdentifier()) { // Assume mis-typed data type
				result.addNotice(t, "Data type expected");
				return t;
			}
		}
		throw new IOException("Variable declaration expected");
	}


}