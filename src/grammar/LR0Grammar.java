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
    public LR0Grammar(Map<State) {
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
}
