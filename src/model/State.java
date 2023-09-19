package model;

import grammar.Grammar;

import java.util.*;

public class State {
    private final Set<Item> items;

    public State(Set<Item> nonClosureItems, List<ProductionRule> productionRules) {
        this.items = new HashSet<>(nonClosureItems);

        Set<Item> closureItems = new HashSet<>();
        this.items.forEach((currItem) -> {
            Set<Item> currClosureItems = currItem.closure(productionRules);
            closureItems.addAll(currClosureItems);
        });
        this.items.addAll(closureItems);
    }

    public Set<Item> getItems() {
        return Collections.unmodifiableSet(items);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return Objects.equals(items, state.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("State:\n");
        this.items.forEach((item -> {
            stringBuilder.append(item).append("\n");
        }));

        return stringBuilder.toString();
    }

    //for testing
    public static void main(String[] args) {
        String DOTMARKER = String.valueOf('\u2022');

        Grammar grammar = new Grammar();
        grammar.addRule("E' -> E");
        grammar.addRule("E -> E + T | T");
        grammar.addRule("T -> T * F | F");
        grammar.addRule("F -> ( E ) | id");
        grammar.printGrammar();

//        List<String> right = new ArrayList<>();
//        right.add("E");
//        Item item = new Item("E'", right, Item.ItemType.NEW_ITEM);
//
//        System.out.println("item: " + item);
//
//        Set<Item> initialItemSet = new HashSet<>();
//        initialItemSet.add(item);
//
//        State state = new State(initialItemSet, grammar.getProductionRules());
//        System.out.println(state);

        List<String> right = new ArrayList<>();
        right.add("(");
        right.add(DOTMARKER);
        right.add("E");
        right.add(")");
        Item item = new Item("F", right, Item.ItemType.DERIVED_ITEM);

        System.out.println("item: " + item);

        Set<Item> initialItemSet = new HashSet<>();
        initialItemSet.add(item);

        State state1 = new State(initialItemSet, grammar.getProductionRules());
        System.out.println(state1);

        State state2 = new State(initialItemSet, grammar.getProductionRules());
        System.out.println(state2);

        System.out.println(state1 == state2);

        Set<State> states = new HashSet<>();
        states.add(state1);
        states.add(state2);
        System.out.println("states.size(): " + states.size());
    }
}
