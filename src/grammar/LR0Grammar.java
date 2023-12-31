package grammar;

import model.Item;
import model.LR0ParseTableElement;
import model.ProductionRule;
import model.State;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static constants.StringConstants.END_OF_LINE_SYMBOL;

public class LR0Grammar extends Grammar {
    /**
     * Map(State, Map(Symbol, State))
     */
    private final Map<State, Map<String, State>> transitions;
    private final List<Map<String, LR0ParseTableElement>> parseTable;
    private State initialState;
    private final Map<State, Integer> stateIndexing;

    public LR0Grammar() {
        super();
        this.transitions = new HashMap<>();
        this.stateIndexing = new HashMap<>();
        this.parseTable = new ArrayList<>();
    }

    private Item findInitialItem() {
        String firstSymbol = super.getFirstSymbol();
        String newNameForFirstSymbol = super.findNewName(firstSymbol);

        List<String> rightOfItem = new ArrayList<>();
        rightOfItem.add(firstSymbol);
        rightOfItem.add(END_OF_LINE_SYMBOL);

        return new Item(newNameForFirstSymbol, rightOfItem, Item.ItemType.NEW_ITEM);
    }

    private Set<Item> findInitialItemSet() {
        Item initalItem = this.findInitialItem();
        Set<Item> set = new HashSet<>();
        set.add(initalItem);
        return set;
    }

    private void addToTransitions(State from, String transitionString, State to) {
        if(!this.transitions.containsKey(from)) {
            Map<String, State> map = new HashMap<>();
            map.put(transitionString, to);
            this.transitions.put(from, map);
        } else {
            this.transitions.get(from).put(transitionString, to);
        }
    }

    private boolean checkStateAlreadyServed(State state) {
        return this.transitions.containsKey(state);
    }

    public void computeTransitions() {
        Set<Item> initialItemSet = this.findInitialItemSet();
        this.initialState = new State(initialItemSet, super.getProductionRules());

        Queue<State> newStatesCreated = new ArrayDeque<>();
        newStatesCreated.add(this.initialState);

        while(!newStatesCreated.isEmpty()) {
            State currState = newStatesCreated.poll();

            //check if the state is already served
            if(checkStateAlreadyServed(currState)) {
                continue;
            }

            Set<String> stringTransitionPossible = new HashSet<>();
            currState.getItems().forEach((item) -> {
                if(!item.isReductionItem()) {
                    stringTransitionPossible.add(item.getSymbolNextOfDotMarker());
                }
            });

            for(String transitionString: stringTransitionPossible) {
                Set<Item> nonClosureItemsForNewState = new HashSet<>();
                currState.getItems().forEach((item) -> {
                    if(!item.isReductionItem()) {
                        if(item.getSymbolNextOfDotMarker().equals(transitionString)) {
                            nonClosureItemsForNewState.add(item.moveDotMarkerAndReturnItem());
                        }
                    }
                });

                //create state based on the nonClosureItem computed
                State newState = new State(nonClosureItemsForNewState, super.getProductionRules());
                newStatesCreated.add(newState);

                this.addToTransitions(currState, transitionString, newState);
            }
        }
    }

    public void computeIndexingOfStates() {
        int ind = 0;

        this.stateIndexing.put(this.initialState, ind++);

        for (Map.Entry<State, Map<String, State>> mapElem : this.transitions.entrySet()) {
            State fromState = mapElem.getKey();
            if (!this.stateIndexing.containsKey(fromState)) {
                this.stateIndexing.put(fromState, ind++);
            }

            for (Map.Entry<String, State> mapValueElem : mapElem.getValue().entrySet()) {
                State toState = mapValueElem.getValue();
                if (!this.stateIndexing.containsKey(toState)) {
                    this.stateIndexing.put(toState, ind++);
                }
            }
        }
    }

    private State getStateFromIndex(int ind) {
        for(Map.Entry<State, Integer> mapElement: this.stateIndexing.entrySet()) {
            if(ind == mapElement.getValue()) {
                return mapElement.getKey();
            }
        }

        return null;
    }

    // for reducing
    private void addToParseTable(int stateNumber, ProductionRule productionRule, String transitionString) {
        this.parseTable.get(stateNumber).put(transitionString, new LR0ParseTableElement(LR0ParseTableElement.ElementType.REDUCE, productionRule));
    }

    // for shifting and goto
    private void addToParseTable(int stateNumber, int shiftStateNumber, String transitionString, LR0ParseTableElement.ElementType elementType) {
        this.parseTable.get(stateNumber).put(transitionString, new LR0ParseTableElement(elementType, shiftStateNumber));
    }

    //for accepting
    private void addToParseTable(int stateNumber, String transitionString) {
        this.parseTable.get(stateNumber).put(transitionString, new LR0ParseTableElement(LR0ParseTableElement.ElementType.ACCEPT));
    }

    private boolean parseTableIsNonEmptyForStateAndTransitionString(int stateNumber, String transitionString) {
        return this.parseTable.get(stateNumber).containsKey(transitionString);
    }

    private void createEmptyParsingTable() {
        for(int i=0; i<this.stateIndexing.size(); i++) {
            Map<String, LR0ParseTableElement> map = new HashMap<>();
            this.parseTable.add(map);
        }
    }

    public void computeParsingTable() {
        if(this.transitions.isEmpty()) {
            System.out.println("Compute the transitions before running this function");
            System.exit(-1);
        }

        this.createEmptyParsingTable();

        for(int i=0; i<this.stateIndexing.size(); i++) {
            State fromState = getStateFromIndex(i);
            assert fromState != null;
            int fromStateInt = i;

            //check if the fromState is an accepting state
            if(fromState.isAcceptingState()) {
                //for all terminal symbols, accepting
                for(String terminal: super.getTerminalSymbols()) {
                    this.addToParseTable(fromStateInt, terminal);
                }

                continue;
            }


            //check if the fromState is a reducing state
            if(fromState.isReducingState()) {
                Set<Item> reducingItems = fromState.getItemsWhichAreReducingItems();
                if(reducingItems.size() > 1) {
                    //reduce - reduce conflict
                    System.out.println("Reduce - Reduce conflict: " + reducingItems);
                    System.exit(-1);
                }

                Iterator<Item> iterator = reducingItems.iterator();
                Item reducingItem = iterator.next();

                ProductionRule productionRuleForItem = reducingItem.getCorrespondingProductionRuleForReducingItem();

                //for all terminal symbols, reduction
                for(String terminal: super.getTerminalSymbols()) {
                    this.addToParseTable(fromStateInt, productionRuleForItem, terminal);
                }

                continue;
            }

            for(Map.Entry<String, State> mapValueElem: this.transitions.get(fromState).entrySet()) {
                String transitionString = mapValueElem.getKey();
                int toStateInt = this.stateIndexing.get(mapValueElem.getValue());

                if(this.parseTableIsNonEmptyForStateAndTransitionString(fromStateInt, transitionString)) {
                    //shift - reduce conflict
                    System.out.println("Shift - Reduce conflict: " + fromStateInt + " state and symbol " + transitionString);
                    System.exit(-1);
                }

                if(super.isTerminalSymbol(transitionString)) {
                    //shift
                    this.addToParseTable(fromStateInt, toStateInt, transitionString,LR0ParseTableElement.ElementType.SHIFT);
                } else if (super.isNonTerminalSymbol(transitionString)) {
                    //goto
                    this.addToParseTable(fromStateInt, toStateInt, transitionString, LR0ParseTableElement.ElementType.GOTO);
                }
            }
        }
    }

    public void printIndexingOfStates() {
        if(this.stateIndexing.isEmpty()) {
            System.out.println("Compute the indexing before running this function");
            System.exit(-1);
        }

        System.out.println("Total States: " + this.stateIndexing.size());
        System.out.println("Indexing of maps:");
        for(int i=0; i<this.stateIndexing.size(); i++) {
            System.out.println(i + " :-\n" + this.getStateFromIndex(i));
        }
    }

    public void printIndexingOfStatesToFile(String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        if(this.stateIndexing.isEmpty()) {
            writer.write("Compute the indexing before running this function");
            System.exit(-1);
        }

        writer.write("Total States: " + this.stateIndexing.size() + "\n");
        writer.write("Indexing of maps:\n");
        for(int i=0; i<this.stateIndexing.size(); i++) {
            writer.write(i + " :-\n" + this.getStateFromIndex(i) + "\n");
        }
        writer.close();
    }

    public void printTransitions() {
        if(this.stateIndexing.isEmpty()) {
            System.out.println("Compute the indexing before running this function");
            System.exit(-1);
        }

        System.out.println("\nFollowing are the transitions:\n");

        for (int fromStateInt=0; fromStateInt<this.stateIndexing.size(); fromStateInt++) {
            State fromState = this.getStateFromIndex(fromStateInt);
            Map<String, State> mapValues = this.transitions.get(fromState);

            if(mapValues == null) {
                continue;
            }

            for(Map.Entry<String, State> transitionState: mapValues.entrySet()) {
                String transitionString = transitionState.getKey();
                int toStateInt = this.stateIndexing.get(transitionState.getValue());

                System.out.println("From State: " + fromStateInt);
                System.out.println("Transition String: " + transitionString);
                System.out.println("To State: " + toStateInt);
                System.out.println();
            }
        }

        System.out.println();
    }

    public void printTransitionsToFile(String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));

        if(this.stateIndexing.isEmpty()) {
            writer.write("Compute the indexing before running this function");
            System.exit(-1);
        }

        writer.write("Following are the transitions:\n");

        for (int fromStateInt=0; fromStateInt<this.stateIndexing.size(); fromStateInt++) {
            State fromState = this.getStateFromIndex(fromStateInt);
            Map<String, State> mapValues = this.transitions.get(fromState);

            if(mapValues == null) {
                continue;
            }

            for(Map.Entry<String, State> transitionState: mapValues.entrySet()) {
                String transitionString = transitionState.getKey();
                int toStateInt = this.stateIndexing.get(transitionState.getValue());

                writer.write("From State: " + fromStateInt + "\n");
                writer.write("Transition String: " + transitionString + "\n");
                writer.write("To State: " + toStateInt + "\n");
                writer.write("\n");
            }
        }
        writer.close();
    }

    private int findLengthOfMaxTableElement() {
        int maxLength = Integer.MIN_VALUE;

        for (Map<String, LR0ParseTableElement> map : this.parseTable) {
            for (LR0ParseTableElement element : map.values()) {
                maxLength = Math.max(maxLength, element.toString().length());
            }
        }
        return maxLength;
    }

    public void printParsingTable() {
        System.out.println("This is the parsing table: ");

        int lengthOfMaxTableElement = this.findLengthOfMaxTableElement() + 5;

        //print headers

        List<String> headerSymbols = new ArrayList<>(super.getTerminalSymbols());
        headerSymbols.addAll(super.getNonTerminalSymbols());

        for(String symbol: headerSymbols) {
            System.out.format("%" + lengthOfMaxTableElement + "s", symbol);
        }
        System.out.println();

        for(int i=0; i<this.parseTable.size(); i++) {
            Map<String, LR0ParseTableElement> map = this.parseTable.get(i);
            System.out.print(i);
            for(String symbol: headerSymbols) {
                System.out.format("%" + lengthOfMaxTableElement + "s", this.parseTable.get(i).get(symbol));
            }
            System.out.println();
        }
        System.out.println();
    }

    public void printParsingTableToFile(String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write("This is the parsing table:\n");

        int lengthOfMaxTableElement = this.findLengthOfMaxTableElement() + 5;

        //print headers

        List<String> headerSymbols = new ArrayList<>(super.getTerminalSymbols());
        headerSymbols.addAll(super.getNonTerminalSymbols());

        StringBuilder stringBuilder = new StringBuilder();
        for(String symbol: headerSymbols) {
            stringBuilder.append(String.format("%" + lengthOfMaxTableElement + "s", symbol));
        }
        stringBuilder.append("\n");

        for(int i=0; i<this.parseTable.size(); i++) {
            Map<String, LR0ParseTableElement> map = this.parseTable.get(i);
            stringBuilder.append(i);
            for(String symbol: headerSymbols) {
                stringBuilder.append(String.format("%" + lengthOfMaxTableElement + "s", this.parseTable.get(i).get(symbol)));
            }
            stringBuilder.append("\n");
        }
        writer.write(stringBuilder.toString());
        writer.close();
    }

    //for testing
    public static void main(String[] args) {
        LR0Grammar grammar = new LR0Grammar();
        grammar.setFirstSymbol("E");
        grammar.addTerminalSymbol("+");
        grammar.addTerminalSymbol("-");
        grammar.addTerminalSymbol("(");
        grammar.addTerminalSymbol(")");
        grammar.addTerminalSymbol("id");

        grammar.addNonTerminalSymbol("E");
        grammar.addNonTerminalSymbol("T");

        grammar.addRule("E -> E + T | E - T | T");
        grammar.addRule("T -> ( E ) | id");

//        grammar.setFirstSymbol("Goal");
//        grammar.addTerminalSymbol("+");
//        grammar.addTerminalSymbol("-");
//        grammar.addTerminalSymbol("/");
//        grammar.addTerminalSymbol("*");
//        grammar.addTerminalSymbol("id");
//        grammar.addTerminalSymbol("number");
//
//        grammar.addNonTerminalSymbol("Goal");
//        grammar.addNonTerminalSymbol("Expr");
//        grammar.addNonTerminalSymbol("Term");
//        grammar.addNonTerminalSymbol("Factor");
//
//        grammar.addRule("Goal -> Expr");
//        grammar.addRule("Expr -> Expr + Term | Expr - Term | Term");
//        grammar.addRule("Term -> Term * Factor | Term / Factor | Factor");
//        grammar.addRule("Factor -> number | id");

        grammar.printGrammar();

        grammar.computeTransitions();
        grammar.computeIndexingOfStates();
        grammar.printIndexingOfStates();
        grammar.printTransitions();

        grammar.computeParsingTable();
        grammar.printParsingTable();
    }
}
