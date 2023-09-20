import grammar.LR0Grammar;
import helperfunction.ReadingInput;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class Main {
    private static final Path homeDirectory = FileSystems.getDefault().getPath("").toAbsolutePath();

    private static LR0Grammar takeLR0GrammarInput() throws IOException {
        String pathToInputGrammar = homeDirectory + "\\Input\\InputGrammar.txt";
        return ReadingInput.readAndCreateLR0Grammar(pathToInputGrammar);
    }

    public static void main(String[] args) {
        //take input the grammar
        LR0Grammar grammar = null;
        try {
            grammar = takeLR0GrammarInput();
        } catch (IOException e) {
            System.out.println("Unable to read from grammar file");
            System.out.println(e.getMessage());
        }

        assert grammar != null;

        System.out.println("Input Grammar: ");
        grammar.printGrammar();
    }
}