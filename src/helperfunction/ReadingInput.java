package helperfunction;

import grammar.LR0Grammar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class ReadingInput {
    public static LR0Grammar readAndCreateLR0Grammar(String path) throws IOException {
        File file = new File(path);

        BufferedReader br = new BufferedReader(new FileReader(file));

        LR0Grammar grammar = new LR0Grammar();

        String startSymbol = br.readLine();
        grammar.setFirstSymbol(startSymbol);

        String nonTerminalString = br.readLine();
//        System.out.println("terminalSymbols read from input: " + terminalSymbols);
        String[] nonTerminals = nonTerminalString.trim().split(" ");
        grammar.addAllNonTerminalSymbolFromIterator(Arrays.stream(nonTerminals).iterator());

        String terminalString = br.readLine();
//        System.out.println("Non Terminal symbols read from input: " + nonTerminalSymbols);
        String[] terminals = terminalString.trim().split(" ");
        grammar.addAllTerminalSymbolFromIterator(Arrays.stream(terminals).iterator());

        String input;
        while((input = br.readLine()) != null) {
            if(input.isEmpty()) {
                continue;
            }

            grammar.addRule(input);
        }

        return grammar;
    }
}
