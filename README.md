This is a beta version of ZScript Language Support for `RSyntaxTextArea`.
It provides syntax highlighting, code completion, and code folding for ZScript.

Also included is an [RText](https://github.com/bobbylight/RText) plugin,
providing all of the above features, as well as other stuff, including:

* A dialog to search and download scripts directly from PureZC's script
  database, right in the UI.
* More to come!
   
ZScript is the scripting language for Zelda Classic, a game engine that allows
the development of custom 2D Zelda fan games.  For more information, see:

* http://www.zeldaclassic.com/
* http://www.purezc.net/

Since this is somewhat of a niche language, it is not included in the
`RSTALanguageSupport` parent project, but rather is a self-contained project
that depends on it.  It takes full advantage of `RSyntaxTextArea`'s code
completion and parsing API's, and serves as an excellent example of integrating
RSTA into an application with a custom or DSL.


# Submodules
There are three submodules:

## zscript-lang-support
A language support plugin for ZScript for `RSyntaxTextArea`.  This is probably
the submodule you care about.  This adds the following features for editing
zscript code to `RSyntaxTextArea`:

* Syntax highlighting and code folding
* Syntax errors are squiggle-underlined
* Code completion is available for the source code, both stdlib functions and
  locally-defined functions, variables, etc.
* A tree view of the source code allows you to jump around the source easily
* `Ctrl+Shift+O` also allows you to navigate by source code constructs

## zscript-lang-support-demo
This is a small demo application showing off the features in `zscript-language-support`.
A native build for Windows is included; one for OS X will come soon.

## zscript-rtext-plugin
A plugin for the `RText` editor, that adds the ZScript functionality above to that
application specifically.


# Building

This project depends on `RSyntaxTextArea` and its sister projects and uses
[Gradle](https://gradle.org/) for building.  JDK 11 or newer is also required,
as is [launch4j](https://sourceforge.net/projects/launch4j/files/launch4j-3/3.12/)
if you want to build the wrapper Windows executable.

To clone this repository, build the Windows demo application, and
run it:

```bash
git clone https://github.com/bobbylight/ZScriptLanguageSupport.git
cd ZScriptLanguageSupport
./gradlew clean build buildWindowsDemo --warning-mode all
./zscript-lang-support-demo/build/install/zscript-demo/zscript-demo.exe
```

To do the same thing on OS X or Linux:

```bash
git clone https://github.com/bobbylight/ZScriptLanguageSupport.git
cd ZScriptLanguageSupport
./gradlew clean build installDist --warning-mode all
java -jar zscript-language-support-demo/build/install/zscript-demo/zscript-demo.jar
```

## OS-Specifics
For Windows, the `buildWindowsDemo` task generates a small wrapper
`zscript-demo.exe` executable via `launch4j`.  That executable simply
delegates to running `zscript-demo.jar`.

For all other operating systems, you must run the jar directly.  Future
work includes wrapping the demo in an `.app` bundle on OSX once again
(it used to be, but Java 11 made it tricky to do that).

# Sister Projects

* [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea) provides syntax highlighting, code folding, and many other features out-of-the-box.
* [AutoComplete](https://github.com/bobbylight/AutoComplete) - Adds code completion to RSyntaxTextArea (or any other JTextComponent).
* [RSTALanguageSupport](https://github.com/bobbylight/RSTALanguageSupport) - Code completion for RSTA for the following languages: Java, JavaScript, HTML, PHP, JSP, Perl, C, Unix Shell.  Built on both RSTA and AutoComplete.
* [SpellChecker](https://github.com/bobbylight/SpellChecker) - Adds squiggle-underline spell checking to RSyntaxTextArea.

# Getting Help

* Add an issue on GitHub
* Check this [PureZC thread](http://www.purezc.net/forums/index.php?showtopic=55636)
