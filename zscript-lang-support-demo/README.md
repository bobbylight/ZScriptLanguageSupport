# zscript-lang-support-demo
A simple demo application of `zscript-lang-support`.

## Building the demo for Windows
This assumes you have `launch4j` installed, see `gradle.properties` to
configure where that is installed:

```bash
# From the project root:
./gradlew clean build buildWindowsDemo --warning-mode all
./zscript-lang-support-demo/build/install/zscript-demo/zscript-demo.exe
```

## Building the demo for OS X or Linux
An `.app` bundle for OS X will come sooner or later.  In the mean time,
you'll just have to have Java 11 on your PATH:

```bash
# From the project root:
./gradlew clean build installDist --warning-mode all
java -jar zscript-lang-support-demo/build/install/zscript-demo/zscript-demo.jar
```

## OS-Specifics
For Windows, the `buildWindowsDemo` task generates a small wrapper
`zscript-demo.exe` executable via `launch4j`.  That executable simply
delegates to running `zscript-demo.jar`.

For all other operating systems, you must run the jar directly.  Future
work includes wrapping the demo in an `.app` bundle on OSX once again
(it used to be, but Java 11 made it tricky to do that).
