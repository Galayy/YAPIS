# Java Compiler

The app is aimed at compiling to Java code from a language for work with strings (and ints:) ) written with Antlr4 plugin.

### Prerequisites

IDE with Antlr4 and Lombok plugins support.

### Usage

1. Generate parser with Antlr4 from `Grammar.g4` file using `Ctrl+Shift+G` command or navigate by prompts of your IDE :)

_Generated code could be foung in `src/main/java/com.yapis.gen` package._

2. Run `Application.java` in `main` method of this class you can specify the file to parse and compile.

_The file should be written according to rules described in `Grammar.g4`._

3. To check the result run `Output.java` which could be found in `src/main/java/com.yapis.compile`.

**Notes** 
* The description of the language could be found in the root directory in `Grammar.g4` file. 
* Examples of written language could be found in `src/main/resources`