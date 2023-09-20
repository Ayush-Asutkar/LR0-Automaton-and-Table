import grammar.LR0Grammar;
import helperfunction.ReadingInput;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class Main {
    private static final Path homeDirectory = FileSystems.getDefault().getPath("").toAbsolutePath();

    private static void deleteOutputDirectory () throws IOException {
        Path path = Path.of(homeDirectory + "\\Output");
//        System.out.println("path: " + path);

        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        assert Files.exists(path);

    }

    private static void createOutputDirectory() throws IOException {
        try {
            deleteOutputDirectory();
        } catch (IOException e) {
//            System.out.println("Output file does not exist yet");
        }
        Path outputDirectoryPath = Path.of(homeDirectory + "\\Output");
        Files.createDirectory(outputDirectoryPath);
    }

    private static LR0Grammar takeLR0GrammarInput() throws IOException {
        String pathToInputGrammar = homeDirectory + "\\Input\\InputGrammar.txt";
        return ReadingInput.readAndCreateLR0Grammar(pathToInputGrammar);
    }

    private static void printGrammarWithNoteToFile(LR0Grammar grammar, String note) throws IOException {
        String pathToOutputDirectory = homeDirectory + "\\Output";
        grammar.printGrammarToFile(pathToOutputDirectory, note);
    }

    private static void printIndexingOfStatesToFile(LR0Grammar grammar) throws IOException {
        String pathToFile = homeDirectory + "\\Output\\IndexingOfStates.txt";
        grammar.printIndexingOfStatesToFile(pathToFile);
    }

    private static void printTransitionsToFile(LR0Grammar grammar) throws IOException {
        String pathToFile = homeDirectory + "\\Output\\Transitions.txt";
        grammar.printTransitionsToFile(pathToFile);
    }

    private static void printParsingTableToFile(LR0Grammar grammar) throws IOException {
        String pathToFile = homeDirectory + "\\Output\\ParsingTable.txt";
        grammar.printParsingTableToFile(pathToFile);
    }

    public static void main(String[] args) {

        //create empty output directory
        try {
            createOutputDirectory();
        } catch (IOException e) {
            System.out.println("Could not create output file");
            System.out.println(e.getMessage());
        }

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

        try {
            printGrammarWithNoteToFile(grammar, "Input Grammar");
        } catch (IOException e) {
            System.out.println("Could not write input grammar to output file");
            System.out.println(e.getMessage());
        }

        System.out.println("Computing Transitions");
        grammar.computeTransitions();

        System.out.println("Computing Indexing of States");
        grammar.computeIndexingOfStates();

        grammar.printIndexingOfStates();
        grammar.printTransitions();

        System.out.println("Computing parsing table");
        grammar.computeParsingTable();
        grammar.printParsingTable();

        try {
            printTransitionsToFile(grammar);
        } catch (IOException e) {
            System.out.println("Could not write transitions to output file");
            System.out.println(e.getMessage());
        }

        try {
            printIndexingOfStatesToFile(grammar);
        } catch (IOException e) {
            System.out.println("Could not write indexing of states to output file");
            System.out.println(e.getMessage());
        }

        try {
            printParsingTableToFile(grammar);
        } catch (IOException e) {
            System.out.println("Could not write parsing table to output file");
            System.out.println(e.getMessage());
        }
    }
}