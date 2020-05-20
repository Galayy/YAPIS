package com.yapis;

import com.yapis.gen.GrammarLexer;
import com.yapis.gen.GrammarParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Application {

    public static void main(String[] args) throws IOException {
        var file = new File("src/main/resources/test2.grammar");
        var testLexer = new GrammarLexer(new ANTLRInputStream(new FileReader(file)));
        var tokenStream = new CommonTokenStream(testLexer);
        var parser = new GrammarParser(tokenStream);
        parser.program();
    }

}
