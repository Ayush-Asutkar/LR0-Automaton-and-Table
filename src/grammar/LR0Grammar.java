package grammar;

import model.Item;
import model.State;

import java.util.*;

public class LR0Grammar extends Grammar {
    /**
     * Map(State, Map(Symbol, State))
     */
    private final Map<State, Map<String, State>> transitions;
    private State initialState;

    public LR0Grammar() {
        super();
        this.transitions = new HashMap<>();
    }

    private Item findInitialItem() {
        String firstSymbol = super.getFirstSymbol();
        String newNameForFirstSymbol = super.findNewName(firstSymbol);

        List<String> rightOfItem = new ArrayList<>();
        rightOfItem.add(firstSymbol);

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

    private Map<State, Integer> indexingStates(){
        Map<State, Integer> result = new HashMap<>();
        int ind = 0;

        for(Map.Entry<State, Map<String, State>> mapElem: this.transitions.entrySet()) {
            State fromState = mapElem.getKey();
            if (!result.containsKey(fromState)) {
                result.put(fromState, ind++);
            }

            for (Map.Entry<String, State> mapValueElem: mapElem.getValue().entrySet()) {
                State toState = mapValueElem.getValue();
                if(!result.containsKey(toState)) {
                    result.put(toState, ind++);
                }
            }
        }

        System.out.println("Total States: " + result.size());
        System.out.println("Indexing of maps:");
        for(Map.Entry<State, Integer> mapElem: result.entrySet()) {
            System.out.println(mapElem.getValue() + " :-\n" + mapElem.getKey());
        }

        return result;
    }

    public void printTransitions() {
        Map<State, Integer> stateIndexing = this.indexingStates();

        for (Map.Entry<State, Map<String, State>> mapElem: this.transitions.entrySet()) {
            int fromState = stateIndexing.get(mapElem.getKey());
            Map<String, State> mapValues = mapElem.getValue();

            for(Map.Entry<String, State> transitionState: mapValues.entrySet()) {
                String transitionString = transitionState.getKey();
                int toState = stateIndexing.get(transitionState.getValue());

                System.out.println("From State: " + fromState);
                System.out.println("Transition String: " + transitionString);
                System.out.println("To State: " + toState);
                System.out.println();
            }
        }

        System.out.println();
    }

    //for testing
    public static void main(String[] args) {
        LR0Grammar grammar = new LR0Grammar();
        grammar.setFirstSymbol("E");
        grammar.addTerminalSymbol("+");
        grammar.addTerminalSymbol("*");
        grammar.addTerminalSymbol("(");
        grammar.addTerminalSymbol(")");
        grammar.addTerminalSymbol("id");

        grammar.addNonTerminalSymbol("E");
        grammar.addNonTerminalSymbol("T");
        grammar.addNonTerminalSymbol("F");

        grammar.addRule("E -> E + T | T");
        grammar.addRule("T -> T * F | F");
        grammar.addRule("F -> ( E ) | id");

        grammar.printGrammar();

        grammar.computeTransitions();
        grammar.printTransitions();
    }
}
