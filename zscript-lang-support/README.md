# zscript-lang-support
A "language support" library for `RSyntaxTextArea` projects also using 
[RSTALanguageSupport](https://github.com/bobbylight/RSTALanguageSupport)
that adds the following features for editing ZScript files:

* Syntax highlighting and code folding
* Syntax errors are squiggle-underlined
* Code completion is available for the source code, both stdlib functions and
  locally-defined functions, variables, etc.
* A tree view of the source code allows you to jump around the source easily
* `Ctrl+Shift+O` also allows you to navigate by source code constructs


## Usage
* Build the project
* Add the generated `build/libs/zscript-*.jar` file to your
  `RSyntaxTextArea`/`RSTALanguageSupport`-based project
* Register the language support like so:

```java
static void registerZScript() {

    // Set up general stuff for our new language.
    LanguageSupportFactory lsf = LanguageSupportFactory.get();
    lsf.addLanguageSupport("text/zscript", "org.fife.rsta.zscript.ZScriptLanguageSupport");
    TokenMakerFactory tmf = TokenMakerFactory.getDefaultInstance();
    ((AbstractTokenMakerFactory)tmf).putMapping("text/zscript", "org.fife.rsta.zscript.ZScriptTokenMaker");
    FoldParserManager fpm = FoldParserManager.get();
    fpm.addFoldParserMapping("text/zscript", new CurlyFoldParser(false, false));

    LanguageSupport ls = LanguageSupportFactory.get().getSupportFor("text/zscript");
    ZScriptLanguageSupport zsls = (ZScriptLanguageSupport)ls;
    zsls.setDocDisplayer(new DemoDocDisplayer());

}
```

* Enable editing of ZScript like so:

```java
RSyntaxTextArea textArea = new RSyntaxTextArea();
textArea.setCodeFoldingEnabled(true);
textArea.setSyntaxEditingStyle("text/zscript"); // Matches mapping key above
```

* Create a "source browser tree" that shows an outline of the code in the editor:


```java
ZScriptOutlineTree tree = new ZScriptOutlineTree();
tree.listenTo(textArea);
JScrollPane treeSP = new JScrollPane(tree);
```

And off you go.  The sister submodule `zscript-lang-support-demo` has a working example of
using this library.
