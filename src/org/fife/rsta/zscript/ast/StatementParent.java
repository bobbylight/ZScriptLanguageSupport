package org.fife.rsta.zscript.ast;


public interface StatementParent extends Node {


	void addStatement(StatementNode statement);


	StatementNode getStatement(int index);


	int getStatementCount();


}