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

# Building

This project depends on `RSyntaxTextArea` and its sister projects, but
while those projects have moved to [Gradle](https://gradle.org/) for building,
this project still uses [Ant](http://ant.apache.org/).  Thus you will need both
of these tools to build this project.

First, clone this project's repository, as well as those for all dependencies:

    mkdir zscript
    cd zscript
    git clone https://github.com/bobbylight/RSyntaxTextArea.git
    git clone https://github.com/bobbylight/AutoComplete.git
    git clone https://github.com/bobbylight/RSTAUI.git
    git clone https://github.com/bobbylight/SpellChecker.git
    git clone https://github.com/bobbylight/ZScriptLanguageSupport.git

Next, build all dependencies:

    cd ./RSyntaxTextArea
    ./gradlew build
    cd ../AutoComplete
    ./gradlew build
    cd ../RSTAUI
    ./gradlew build
    cd ../SpellChecker
    ./gradlew build

Finally, build and run this project's demo application:

    cd ../ZScriptLanguageSupport
    ant make-demo-win
    java -jar ./dist/zscript_language_support_demo.jar

The Ant targets you care about are:

* `make-demo-win` (default) - Builds the Windows demo
* `make-demo-mac` - Builds the demo for Macs (.app bundle)
* `make-rtext-plugin` - Creates the plugin jar for `RText`. You can just
   drop this into an RText install's `plugins/` folder to use it

# Sister Projects

* [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea) provides syntax highlighting, code folding, and many other features out-of-the-box.
* [AutoComplete](https://github.com/bobbylight/AutoComplete) - Adds code completion to RSyntaxTextArea (or any other JTextComponent).
* [RSTALanguageSupport](https://github.com/bobbylight/RSTALanguageSupport) - Code completion for RSTA for the following languages: Java, JavaScript, HTML, PHP, JSP, Perl, C, Unix Shell.  Built on both RSTA and AutoComplete.
* [SpellChecker](https://github.com/bobbylight/SpellChecker) - Adds squiggle-underline spell checking to RSyntaxTextArea.

# Getting Help

* Add an issue on GitHub
* Ask in the [project forum](http://fifesoft.com/forum/)
* Check this [PureZC thread](http://www.purezc.net/forums/index.php?showtopic=55636)
